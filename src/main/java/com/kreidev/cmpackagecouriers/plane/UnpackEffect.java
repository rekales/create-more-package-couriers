package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.CourierTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface UnpackEffect {

    ItemStack apply(Level level, ItemStack stack, Vec3 planePos, CourierTarget target);
}
