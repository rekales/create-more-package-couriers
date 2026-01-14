package com.kreidev.cmpackagecouriers.sign;

import com.kreidev.cmpackagecouriers.PlaneDestination;
import com.kreidev.cmpackagecouriers.Utils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.world.level.block.WallSignBlock.FACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AddressSignBlockEntity extends SignBlockEntity implements MenuProvider {
    public AddressSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public String getAddress() {
        SignText text = this.getText(true);
        return text.getMessages(false)[0].getString().trim();
    }

    public void setAddress(String address) {
        SignText text = this.getText(true);
        text = text.setMessage(0, Component.literal(address));
        this.setText(text, true);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AddressSignBlockEntity be) {
        SignBlockEntity.tick(level, pos, state, be);
        if (level.isClientSide()) return;

        BlockPos targetPos = pos.relative(state.getValue(FACING).getOpposite());
        if (level.getBlockState(targetPos).getBlock() instanceof PlaneDestination
                && Utils.isChunkTicking(level, new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ()))) {
            String address = be.getAddress();
            if (!address.isBlank()) {
                AddressSignHandler.addOrUpdateTarget(level, targetPos, address.trim());
            }
        }

        // TODO: postboxes as offline delivery target
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return AddressSignMenu.create(containerId, playerInventory, this);
    }
}
