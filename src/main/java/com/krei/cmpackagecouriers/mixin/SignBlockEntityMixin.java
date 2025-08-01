package com.krei.cmpackagecouriers.mixin;

import com.krei.cmpackagecouriers.marker.AddressMarkerHandler;
import com.krei.cmpackagecouriers.plane.CardboardPlaneEntity;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.world.level.block.WallSignBlock.FACING;

@Mixin(value = SignBlockEntity.class, remap = false)
public abstract class SignBlockEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private static void tick(Level level, BlockPos pos, BlockState state, SignBlockEntity sign, CallbackInfo ci) {
        if (!level.isClientSide() && level.getGameTime()%5 != 2) {
            BlockPos targetPos;
            if (state.getBlock() instanceof WallSignBlock)
                targetPos = pos.relative(state.getValue(FACING).getOpposite());
            else
                return;

            if (level.getBlockState(targetPos).getBlock() instanceof DepotBlock
                    && CardboardPlaneEntity.isChunkTicking(level, new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ()))) {
                for (boolean front : Iterate.trueAndFalse) {
                    SignText text = sign.getText(front);
                    StringBuilder address = new StringBuilder();
                    for (Component component : text.getMessages(false)) {
                        String string = component.getString();
                        if (!string.isBlank())
                            address.append(string.trim()).append(" ");
                    }
                    if (!address.toString().isBlank()) {
                        AddressMarkerHandler.addOrUpdateTarget(level, targetPos, address.toString().trim());
                        return;
                    }
                }
            }
        }
    }
}
