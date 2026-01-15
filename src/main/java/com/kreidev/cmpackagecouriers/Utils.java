package com.kreidev.cmpackagecouriers;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
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

    public static String getSignAddress(SignBlockEntity sign) {
        for (boolean front : Iterate.trueAndFalse) {
            SignText text = sign.getText(front);
            String address = "";
            for (Component component : text.getMessages(false)) {
                String string = component.getString();
                if (!string.isBlank())
                    address += string.trim() + " ";
            }
            if (!address.isBlank())
                return address.trim();
        }
        return "";
    }
}
