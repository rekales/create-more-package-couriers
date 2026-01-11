package com.kreidev.cmpackagecouriers.mixin;

import com.kreidev.cmpackagecouriers.plane.CardboardPlaneItem;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SawBlockEntity.class, remap = false)
public class SawBlockEntityMixin {

    @Shadow public ProcessingInventory inventory;

    @Inject(method = "applyRecipe", at = @At("HEAD"), cancellable = true)
    private void applyRecipe(CallbackInfo ci) {
        ItemStack input = inventory.getStackInSlot(0);
        if (input.getItem() instanceof CardboardPlaneItem) {
            inventory.clear();
            inventory.setStackInSlot(1, CardboardPlaneItem.getPackage(input));
            ci.cancel();
        }
    }
}
