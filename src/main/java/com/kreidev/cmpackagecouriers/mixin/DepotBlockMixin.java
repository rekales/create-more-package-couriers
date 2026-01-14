package com.kreidev.cmpackagecouriers.mixin;

import com.kreidev.cmpackagecouriers.plane.CardboardPlane;
import com.kreidev.cmpackagecouriers.PlaneDestination;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = DepotBlock.class, remap = false)
public class DepotBlockMixin implements PlaneDestination {

    @Override
    public void cmpc$onReachedDestination(Level level, BlockPos pos, CardboardPlane plane) {
        if (level.getBlockEntity(pos) instanceof DepotBlockEntity depot) {
            depot.setHeldItem(plane.getPackage());
            depot.notifyUpdate();
        }
    }

    @Override
    public boolean cmpc$hasSpace(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DepotBlockEntity depot) {
            return depot.getHeldItem().is(Items.AIR);
        }
        return false;
    }
}
