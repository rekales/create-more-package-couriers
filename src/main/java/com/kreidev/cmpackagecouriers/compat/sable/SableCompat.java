package com.kreidev.cmpackagecouriers.compat.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public final class SableCompat {
    private SableCompat() {}

    public static BlockPos tickingPosition(Level level, BlockPos plotPos) {
        return BlockPos.containing(SableCompanion.INSTANCE.projectOutOfSubLevel(level, Vec3.atCenterOf(plotPos)));
    }

    public static @Nullable UUID ownerAt(Level level, Vec3 plotPos) {
        var owner = SableCompanion.INSTANCE.getContaining(level, plotPos);
        return owner == null ? null : owner.getUniqueId();
    }

    public static @Nullable Vec3 waypoint(Level level, Vec3 plotPos, UUID expectedOwner) {
        if (!expectedOwner.equals(ownerAt(level, plotPos))) return null;
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, plotPos);
    }
}
