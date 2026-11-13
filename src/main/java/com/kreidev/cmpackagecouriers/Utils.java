package com.kreidev.cmpackagecouriers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Utils {

    public static boolean isChunkTicking(Level level, Vec3 pos) {
        return isChunkTicking(level, new BlockPos((int) pos.x(),(int)  pos.y(),(int)  pos.z()));
    }

    public static boolean isChunkTicking(Level level, BlockPos blockPos) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getChunkSource().chunkMap.getDistanceManager()
                    .inEntityTickingRange(ChunkPos.asLong(blockPos));
        }
        return false;
    }
}
