package com.kreidev.cmpackagecouriers.blockentity;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

import javax.annotation.Nullable;

public class SignAddressableBehaviour extends BlockEntityBehaviour {

    public static final BehaviourType<SignAddressableBehaviour> TYPE = new BehaviourType<>();

    private String signAddress = "";
    private final boolean planeTargetable;

    public SignAddressableBehaviour(SmartBlockEntity be, boolean planeTargetable) {
        super(be);
        this.planeTargetable = planeTargetable;
        this.setLazyTickRate(50);
    }

    public SignAddressableBehaviour(SmartBlockEntity be) {
        this(be, true);
    }

    @Override
    public void lazyTick() {
        String address = getSignAddress(this.blockEntity.getLevel(), this.blockEntity.getBlockPos());
        this.signAddress = address == null ? "" : address;

        if (!this.planeTargetable) return;
        // TODO: cardboard plane add as target to global tracker
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public String getAddressFilter() {
        return this.signAddress;
    }

    // Copied from PackagerBlockEntity
    public static @Nullable String getSignAddress(Level level, BlockPos pos) {
        for (Direction side : Iterate.directions) {
            String address = getSign(level, pos, side);
            if (address == null || address.isBlank())
                continue;
            return address;
        }
        return null;
    }

    public static String getSign(Level level, BlockPos pos, Direction side) {
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
