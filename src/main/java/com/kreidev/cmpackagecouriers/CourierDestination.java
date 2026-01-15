package com.kreidev.cmpackagecouriers;

import com.kreidev.cmpackagecouriers.plane.CardboardPlane;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

// Using interface injection pattern instead of behaviours so it's more generic
// and I don't have to deal with adding behaviours to extending classes
public interface CourierDestination {
    void cmpc$onReachedDestination(Level level, BlockPos pos, CardboardPlane plane);
    boolean cmpc$hasSpace(Level level, BlockPos pos);
}
