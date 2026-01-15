package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.*;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.createmod.catnip.data.Pair;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;

@SuppressWarnings({"UnusedReturnValue", "ConstantValue", "DataFlowIssue", "unused"})
@EventBusSubscriber(modid= PackageCouriers.MOD_ID)
public class CardboardPlaneManager {

    public static final int LIFESPAN_TICKS = 400;

    public static CardboardPlaneSavedData INSTANCE;

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
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

        String address = PackageItem.getAddress(box);

        // Special "@Address" interaction
        int atIndex = address.indexOf('@');
        if (atIndex != -1) {
            address = address.substring(atIndex + 1);
        }

        CourierTarget target = CourierTarget.getActiveTarget(address);
        if (target.getType() == CourierTarget.Type.BLOCK && ServerConfig.planeLocationTargets
                || target.getType() == CourierTarget.Type.ENTITY && ServerConfig.planePlayerTargets) {
            CardboardPlane plane = new CardboardPlane(currentLevel, target, box);
            plane.setPos(pos);
            plane.setRot(pitch, yaw);
            plane.setUnpack(unpack);
            INSTANCE.pairedPlanes.add(Pair.of(plane, null));
            PackageCouriers.LOGGER.debug("added plane");
            return true;
        }
        return false;
    }
}
