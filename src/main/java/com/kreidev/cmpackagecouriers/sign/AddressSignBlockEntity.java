package com.kreidev.cmpackagecouriers.sign;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

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

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return AddressSignMenu.create(containerId, playerInventory, this);
    }
}
