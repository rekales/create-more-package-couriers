package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.*;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// TODO: merge manager and saved data
@SuppressWarnings({"UnusedReturnValue", "ConstantValue", "DataFlowIssue", "unused"})
@EventBusSubscriber(modid= PackageCouriers.MOD_ID)
public class CardboardPlaneManager {

    public static final int LIFESPAN_TICKS = 400;

    public static CardboardPlaneSavedData INSTANCE;

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Pre event) {
        if (INSTANCE == null) return;
        if (!INSTANCE.pairedPlanes.isEmpty()) INSTANCE.setDirty();

        MinecraftServer server = event.getServer();

        for (Iterator<Pair<CardboardPlane, CardboardPlaneEntity>> iterator = INSTANCE.pairedPlanes.iterator(); iterator.hasNext();) {
            Pair<CardboardPlane, CardboardPlaneEntity> pair = iterator.next();
            CardboardPlane plane = pair.getFirst();
            CardboardPlaneEntity entity = pair.getSecond();

            plane.tick(server);

            if (plane.isForRemoval()) {
                if (entity != null) entity.remove(Entity.RemovalReason.DISCARDED);
                iterator.remove();
                continue;
            }

            if (plane.hasTeleported()) {
                if (entity != null) entity.remove(Entity.RemovalReason.DISCARDED);
                pair.setSecond(null);
                plane.setTeleported(false);
            }

            // Adding a plane entity for rendering
            ServerLevel level = server.getLevel(plane.getCurrentDim());
            if (Utils.isChunkTicking(level, plane.getPos())) {
                if (entity == null) {
                    entity = new CardboardPlaneEntity(level, plane);
                    pair.setSecond(entity);
                    level.addFreshEntity(entity);
                }
            } else if (entity != null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                pair.setSecond(null);
            }
        }
    }

    /**
     * @return false if plane failed to launch
     */
    public static boolean addPlane(ServerLevel level, Vec3 pos, float yaw, float pitch, ItemStack box) {
        return addPlane(level, pos, yaw, pitch, box, false);
    }

    /**
     * @return false if plane failed to launch
     */
    public static boolean addPlane(ServerLevel level, Vec3 pos, float yaw, float pitch, ItemStack box, boolean unpack) {
        if (!PackageItem.isPackage(box)) return false;
        if (level.isClientSide()) return false;
        MinecraftServer server = level.getServer();
        if (server == null) return false;

        String address = PackageItem.getAddress(box);
        // TODO: ponder info if disabled cross dim transport
        PackageDestination dest = findDestination(level, address, ServerConfig.planeCrossDimTransport,
                ServerConfig.planePlayerTargets, ServerConfig.planeLocationTargets);
        if (dest == null) return false;

        ServerLevel targetLevel = server.getLevel(dest.getDim());
        if (targetLevel == null) return false;

        CardboardPlane plane = new CardboardPlane(level, dest, box);
        plane.setPos(pos);
        plane.setRot(pitch, yaw);
        plane.setUnpack(unpack);
        INSTANCE.pairedPlanes.add(Pair.of(plane, null));
        return true;
    }

    private static @Nullable PackageDestination findDestination(ServerLevel level, String address, boolean crossDim,
                                                                boolean playerTargets, boolean locationTargets) {
        // Special "<Address>" interaction
        int start = address.indexOf('<');
        int end = address.indexOf('>');
        if (start != -1 && end != -1 && end > start) {
            address = address.substring(start + 1, end);
        }

        // Special "@Address" interaction
        boolean playerOnly = false;
        int atIndex = address.indexOf('@');
        if (atIndex != -1) {
            address = address.substring(atIndex + 1);
            playerOnly = true;  // TODO: ponder info about explicit @address player targets
        }

        Map<PackageDestination, String> destinations = GlobalDestinationManager.findDestinations(level, address);
        PackageDestination ret = null;

        if (playerOnly || !locationTargets) {
            destinations.entrySet().removeIf(entry -> entry.getKey().getType() != PackageDestination.Type.PLAYER);
        }
        if (!locationTargets) {
            destinations.entrySet().removeIf(entry -> entry.getKey().getType() == PackageDestination.Type.PLAYER);
        }
        if (!crossDim) {
            destinations.entrySet().removeIf(entry -> entry.getKey().getDim() != level.dimension());
        }

        return destinations.entrySet().stream()
                .min(Comparator
                        .comparingInt((Map.Entry<PackageDestination, String> e) -> e.getKey().getType() == PackageDestination.Type.PLAYER ? 0 : 1)
                        .thenComparingInt(e -> -PackageDestination.getAddressPriority(e.getValue())))
                .map(Map.Entry::getKey).orElse(null);
    }

}
