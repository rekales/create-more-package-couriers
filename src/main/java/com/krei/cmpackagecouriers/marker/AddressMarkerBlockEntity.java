package com.krei.cmpackagecouriers.marker;

import com.krei.cmpackagecouriers.PackageCouriers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AddressMarkerBlockEntity extends SignBlockEntity {

    public AddressMarkerBlockEntity(BlockEntityType type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

//    public AddressMarkerBlockEntity(BlockPos pos, BlockState state) {
//        super(PackageCouriers.ADDRESS_MARKER_BLOCK_ENTITY.get(), pos, state);
//    }

}
