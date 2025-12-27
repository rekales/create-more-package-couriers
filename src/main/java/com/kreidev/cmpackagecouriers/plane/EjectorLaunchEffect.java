package com.kreidev.cmpackagecouriers.plane;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


// NOTE: Might need a better name
public interface EjectorLaunchEffect {
    /**
     * @return false if the item should be ejected normally
     */
    boolean onEject(ItemStack stack, Level level, BlockPos pos, float yaw);
}
