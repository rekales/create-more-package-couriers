package com.kreidev.cmpackagecouriers.sign;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class AddressSignBlock extends WallSignBlock {

    public static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
            ImmutableMap.of(
                    Direction.NORTH,
                    Block.box(1.0, 5.0, 15.0, 15.0, 10.0, 16.0),
                    Direction.SOUTH,
                    Block.box(1.0, 5.0, 0.0, 15.0, 10.0, 1.0),
                    Direction.EAST,
                    Block.box(0.0, 5.0, 1.0, 1.0, 10.0, 15.0),
                    Direction.WEST,
                    Block.box(15.0, 5.0, 1.0, 16.0, 10.0, 15.0)
            )
    );

    protected AddressSignBlock(Properties properties) {
        super(WoodType.OAK, properties);
    }

    @Override
    public String getDescriptionId() {
        return "";
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AddressSignBlockEntity(AddressSignReg.ADDRESS_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABBS.get(state.getValue(FACING));
    }
}
