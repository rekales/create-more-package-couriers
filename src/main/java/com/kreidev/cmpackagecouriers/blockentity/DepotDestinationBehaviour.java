package com.kreidev.cmpackagecouriers.blockentity;

import com.kreidev.cmpackagecouriers.PackageCarrier;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

import javax.annotation.Nullable;

public class DepotDestinationBehaviour extends PackageDestinationBehaviour {

    public static final BehaviourType<DepotDestinationBehaviour> TYPE = new BehaviourType<>();

    private String signAddress = "";

    public DepotDestinationBehaviour(DepotBlockEntity be) {
        super(be);
        this.setLazyTickRate(50);
    }

    @Override
    public void lazyTick() {
        String address = getSignAddress(this.blockEntity.getLevel(), this.blockEntity.getBlockPos());
        this.signAddress = address == null ? "" : address;
        // TODO: cardboard plane add as target to global tracker
    }

    @Override
    public String getAddress() {
        return this.signAddress;
    }

    @Override
    public boolean hasSpace() {
        return ((DepotBlockEntity) this.blockEntity).getHeldItem().is(Items.AIR);
    }

    @Override
    public boolean tryInsertPackage(PackageCarrier carrier) {
        if (!this.hasSpace()) return false;
        DepotBlockEntity depot = (DepotBlockEntity) this.blockEntity;
        depot.setHeldItem(carrier.getPackage());
        depot.notifyUpdate();
        carrier.setPackage(ItemStack.EMPTY);
        return true;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    // Copied from PackagerBlockEntity
    private static @Nullable String getSignAddress(Level level, BlockPos pos) {
        for (Direction side : Iterate.directions) {
            String address = getSign(level, pos, side);
            if (address == null || address.isBlank())
                continue;
            return address;
        }
        return null;
    }

    private static String getSign(Level level, BlockPos pos, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos.relative(side));
        if (!(blockEntity instanceof SignBlockEntity sign))
            return null;
        for (boolean front : Iterate.trueAndFalse) {
            SignText text = sign.getText(front);
            String address = "";
            for (Component component : text.getMessages(false)) {
                String string = component.getString();
                if (!string.isBlank())
                    address += string.trim() + " ";
            }
            if (!address.isBlank())
                return address.trim();
        }
        return null;
    }
}
