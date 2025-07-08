package com.krei.cmpackagecouriers.mixin;

import com.krei.cmpackagecouriers.EjectorLaunchEffect;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EjectorBlockEntity.class, remap = false)
public abstract class EjectorBlockEntityMixin {

    @Inject(method = "addToLaunchedItems", at = @At("HEAD"), cancellable = true)
    private void addToLaunchedItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof EjectorLaunchEffect ejectable) {
            EjectorBlockEntity be = (EjectorBlockEntity) (Object) this;
            if (ejectable.onEject(stack, be.getLevel(), be.getBlockPos()))
                cir.setReturnValue(false);
        }
    }
}
