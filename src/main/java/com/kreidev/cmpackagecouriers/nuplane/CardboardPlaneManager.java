package com.kreidev.cmpackagecouriers.nuplane;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.kreidev.cmpackagecouriers.ServerConfig;
import com.kreidev.cmpackagecouriers.compat.Mods;
import com.kreidev.cmpackagecouriers.compat.curios.CuriosCompat;
import com.kreidev.cmpackagecouriers.marker.AddressMarkerHandler;
import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterItem;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
@EventBusSubscriber(modid= PackageCouriers.MOD_ID)
public class CardboardPlaneManager {

    public static final int LIFESPAN_TICKS = 400;

    public static List<Pair<CardboardPlane, CardboardPlaneNuEntity>> pairedPlanes = new ArrayList<>();

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        for (Iterator<Pair<CardboardPlane, CardboardPlaneNuEntity>> iterator = pairedPlanes.iterator(); iterator.hasNext();) {
            Pair<CardboardPlane, CardboardPlaneNuEntity> pair = iterator.next();
            CardboardPlane plane = pair.getFirst();
            CardboardPlaneNuEntity entity = pair.getSecond();

            plane.tick(event.getServer());

            if (plane.hasTeleported()) {
                if (entity != null) entity.remove(Entity.RemovalReason.DISCARDED);
                pair.setSecond(null);
                plane.setTeleported(false);
                PackageCouriers.LOGGER.debug("plane teleported");
            }

            if (plane.getTickCount() > LIFESPAN_TICKS) {
                plane.onReachedTarget(server);
                if (entity != null) entity.remove(Entity.RemovalReason.DISCARDED);
                iterator.remove();
                PackageCouriers.LOGGER.debug("timed-out plane");
                continue;
            }

            if (plane.hasReachedTarget()) {
                plane.onReachedTarget(server);
                if (entity != null) entity.remove(Entity.RemovalReason.DISCARDED);
                iterator.remove();
                PackageCouriers.LOGGER.debug("destination reached");
                continue;
            }

            ServerLevel level = server.getLevel(plane.getCurrentDim());
            if (isChunkTicking(level, plane.getPos())) {
                if (entity == null) {
                    entity = new CardboardPlaneNuEntity(level, plane);
                    pair.setSecond(entity);
                    level.addFreshEntity(entity);
                    PackageCouriers.LOGGER.debug("spawned plane entity");
                }
            } else if (entity != null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                pair.setSecond(null);
                PackageCouriers.LOGGER.debug("removed plane entity");
            }
        }
    }

    /**
     * @return false if plane failed to launch
     */
    public static boolean addPlane(Level level, Vec3 pos, float yaw, float pitch, ItemStack box) {
        return addPlane(level, pos, yaw, pitch, box, false);
    }

    /**
     * @return false if plane failed to launch
     */
    public static boolean addPlane(Level currentLevel, Vec3 pos, float yaw, float pitch, ItemStack box, boolean unpack) {
        if (!PackageItem.isPackage(box)) return false;
        if (currentLevel.isClientSide()) return false;
        MinecraftServer server = currentLevel.getServer();
        if (server == null) return false;

        CardboardPlane plane = null;

        String address = PackageItem.getAddress(box);
        ServerPlayer serverPlayer = server.getPlayerList().getPlayerByName(address);
        if (serverPlayer != null && ServerConfig.planePlayerTargets) {
            if (!ServerConfig.locationTransmitterNeeded || hasEnabledLocationTransmitter(serverPlayer)) {
                plane = new CardboardPlane(currentLevel, serverPlayer, box);
            }
        } else {
            AddressMarkerHandler.MarkerTarget target = AddressMarkerHandler.getMarkerTarget(address);
            if (target != null && hasSpace(target.level, target.pos) && ServerConfig.planeLocationTargets) {
                plane = new CardboardPlane(currentLevel, target.level, target.pos, box);
                // TODO: MarkerTarget getters pattern
            }
        }

        if (plane == null) return false;
        plane.setPos(pos);
        plane.setRot(pitch, yaw);
        plane.setUnpack(unpack);
        pairedPlanes.add(Pair.of(plane, null));
        PackageCouriers.LOGGER.debug("added plane");
        return true;
    }

    public static boolean isChunkTicking(Level level, Vec3 pos) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos blockPos = new BlockPos((int) pos.x(),(int)  pos.y(),(int)  pos.z());
            return serverLevel.getChunkSource().chunkMap.getDistanceManager()
                    .inEntityTickingRange(ChunkPos.asLong(blockPos));
        }
        return false;
    }

    /**
     * Checks if the player has an enabled location transmitter in their inventory or Curios slots.
     * @param player The player to check
     * @return true if the player has an enabled location transmitter, false otherwise
     */
    public static boolean hasEnabledLocationTransmitter(ServerPlayer player) {
        // Check regular inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof LocationTransmitterItem && LocationTransmitterItem.isEnabled(stack)) {
                return true;
            }
        }

        // Check Curios slots if Curios is loaded
        if (Mods.CURIOS.isLoaded() && CuriosCompat.isCuriosLoaded()) {
            return CuriosCompat.hasEnabledLocationTransmitterInCurios(player);
        }

        return false;
    }

    public static boolean hasSpace(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DepotBlockEntity depotBlockEntity) {
            return depotBlockEntity.getHeldItem().isEmpty();
        }
        // TODO: add behaviors on belts and shit and check behaviours if exists
        // Other target types here, maybe using an injected interface for them instead of this.

        return true;
    }

}
