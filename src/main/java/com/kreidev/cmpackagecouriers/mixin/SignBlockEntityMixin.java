package com.kreidev.cmpackagecouriers.mixin;

import com.kreidev.cmpackagecouriers.Utils;
import com.kreidev.cmpackagecouriers.CourierDestination;
import com.kreidev.cmpackagecouriers.CourierTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.kreidev.cmpackagecouriers.compat.Mods;
import com.kreidev.cmpackagecouriers.compat.sable.SableCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.world.level.block.WallSignBlock.FACING;

@Mixin(value = SignBlockEntity.class, remap = false)
public abstract class SignBlockEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private static void tick(Level level, BlockPos pos, BlockState state, SignBlockEntity sign, CallbackInfo ci) {
        if (level.isClientSide()
                || !(state.getBlock() instanceof WallSignBlock)
                || level.getGameTime()%5 != 1) {  // do every 5 ticks
            return;
        }

        BlockPos targetPos = pos.relative(state.getValue(FACING).getOpposite());
        if (level.getBlockState(targetPos).getBlock() instanceof CourierDestination
                && Utils.isChunkTicking(level, Mods.SABLE.isLoaded() ? SableCompat.tickingPosition(level, targetPos) : targetPos)) {
            String address = Utils.getSignAddress(sign);
            if (!address.isBlank()) {
                CourierTarget.addOrUpdateTarget(new CourierTarget(address, level, targetPos));
            }
        }
    }
}
