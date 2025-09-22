package com.krei.cmpackagecouriers.compat.cmpackagepipebomb;

import net.minecraft.world.item.ItemStack;
import com.krei.cmpackagepipebomb.PackageSpawn;
import net.minecraft.world.level.Level;

public class PackagePipebombCompat {
    public static boolean isRigged(ItemStack itemStack) {
        return itemStack.getItem() instanceof PackageSpawn;
    }

    public static void spawnRigged(ItemStack itemStack, Level level, double x, double y, double z) {
        if (itemStack.getItem() instanceof PackageSpawn packageSpawn) {
            packageSpawn.spawnEntity(level, x, y, z);
        }
    }
}