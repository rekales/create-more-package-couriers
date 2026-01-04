package com.kreidev.cmpackagecouriers.sign;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AddressSignBlockEntity extends SignBlockEntity {
    public AddressSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

//    public AddressSignBlockEntity(BlockPos pos, BlockState blockState) {
//        super(AddressSignReg.ADDRESS_SIGN_BLOCK_ENTITY.get(), pos, blockState);
//    }
}
