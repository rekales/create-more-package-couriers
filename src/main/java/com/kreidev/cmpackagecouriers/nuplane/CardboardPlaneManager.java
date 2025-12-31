package com.kreidev.cmpackagecouriers.nuplane;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid= PackageCouriers.MOD_ID)
public class CardboardPlaneManager {

    public static final int LIFESPAN_TICKS = 100;

    public static List<Pair<CardboardPlane, CardboardPlaneNuEntity>> pairedPlanes = new ArrayList<>();

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        for (Iterator<Pair<CardboardPlane, CardboardPlaneNuEntity>> iterator = pairedPlanes.iterator(); iterator.hasNext();) {
            Pair<CardboardPlane, CardboardPlaneNuEntity> pair = iterator.next();
            CardboardPlane plane = pair.getFirst();
            CardboardPlaneNuEntity entity = pair.getSecond();

            plane.tick(event.getServer());

            if (plane.getTickCount() > LIFESPAN_TICKS) {
                if (entity != null) entity.remove(Entity.RemovalReason.DISCARDED);
                iterator.remove();
                PackageCouriers.LOGGER.debug("timed-out plane");
                continue;
            }

            ServerLevel level = server.getLevel(Level.OVERWORLD);

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

    public static void addPlane(Level level, Vec3 pos, Vec3 deltaMovement) {
        CardboardPlane plane = new CardboardPlane(level, new BlockPos(0,-50,0));
        plane.setPos(pos);
        plane.setRot(deltaMovement);
        pairedPlanes.add(Pair.of(plane, null));
        PackageCouriers.LOGGER.debug("added plane");
    }

    public static boolean isChunkTicking(Level level, Vec3 pos) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos blockPos = new BlockPos((int) pos.x(),(int)  pos.y(),(int)  pos.z());
            return serverLevel.getChunkSource().chunkMap.getDistanceManager()
                    .inEntityTickingRange(ChunkPos.asLong(blockPos));
        }
        return false;
    }

}
