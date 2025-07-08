package com.krei.cmpackagecouriers.mixin;

import com.krei.cmpackagecouriers.PackageCouriers;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// For dev environment only because f3+b is cooked
// TODO: Remove on release
@Mixin(value = KeyboardHandler.class, remap = false)
public abstract class ShowHitboxesDebugMixin {

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private static void handleDebugKeys(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == 88) {
            PackageCouriers.LOGGER.debug("text");
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag = !minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
            minecraft.getEntityRenderDispatcher().setRenderHitBoxes(flag);
            Component.translatableEscape(flag ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
            cir.setReturnValue(true);
        }
    }
}
