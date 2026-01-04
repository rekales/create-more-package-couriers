package com.kreidev.cmpackagecouriers.sign;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AddressSignMenu extends MenuBase<AddressSignBlockEntity> {

    protected AddressSignMenu(MenuType<?> type, int id, Inventory inv, AddressSignBlockEntity be) {
        super(type, id, inv, be);
    }

    protected AddressSignMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public static AddressSignMenu create(int id, Inventory inv, AddressSignBlockEntity be) {
        return new AddressSignMenu(AddressSignReg.ADDRESS_SIGN_MENU.get(), id, inv, be);
    }

    @Override
    protected @Nullable AddressSignBlockEntity createOnClient(RegistryFriendlyByteBuf extraData) {
        BlockPos blockPos = extraData.readBlockPos();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        if (level.getBlockEntity(blockPos) instanceof AddressSignBlockEntity be) return be;
        return null;
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(8, 165);
    }

    @Override
    protected void initAndReadInventory(AddressSignBlockEntity contentHolder) {}

    @Override
    protected void saveData(AddressSignBlockEntity contentHolder) {}

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}