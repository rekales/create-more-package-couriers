package com.kreidev.cmpackagecouriers.mixin;

import com.kreidev.cmpackagecouriers.CourierDestination;
import com.kreidev.cmpackagecouriers.plane.CardboardPlane;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = BeltBlock.class, remap = false)
public class BeltBlockMixin implements CourierDestination {

    @Override
    public void cmpc$onReachedDestination(Level level, BlockPos pos, CardboardPlane plane) {
        BeltBlockEntity belt = BeltHelper.getSegmentBE(level, pos);
        if (belt == null) return;
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, belt.getBlockPos(), null);
        if (handler == null) return;

        handler.insertItem(0, plane.getPackage(), false);
    }

    @Override
    public boolean cmpc$hasSpace(Level level, BlockPos pos) {
        BeltBlockEntity belt = BeltHelper.getSegmentBE(level, pos);
        if (belt == null) return false;

        TransportedItemStack tStack = belt.getInventory().getStackAtOffset(belt.index);
        return tStack == null;
    }
}
