package com.kreidev.cmpackagecouriers.sign;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AddressSignBlock extends WallSignBlock {

    public static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
            ImmutableMap.of(
                    Direction.NORTH,
                    Block.box(1.0, 6.0, 15.0, 15.0, 11.0, 16.0),
                    Direction.SOUTH,
                    Block.box(1.0, 6.0, 0.0, 15.0, 11.0, 1.0),
                    Direction.EAST,
                    Block.box(0.0, 6.0, 1.0, 1.0, 11.0, 15.0),
                    Direction.WEST,
                    Block.box(15.0, 6.0, 1.0, 16.0, 11.0, 15.0)
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

    @Override
    public void openTextEdit(Player player, SignBlockEntity signEntity, boolean isFrontText) {
        signEntity.setAllowedPlayerEditor(player.getUUID());
        if (signEntity instanceof AddressSignBlockEntity be) {
            player.openMenu(be, be.getBlockPos());
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, AddressSignReg.ADDRESS_SIGN_BLOCK_ENTITY.get(), AddressSignBlockEntity::tick);
    }
}
