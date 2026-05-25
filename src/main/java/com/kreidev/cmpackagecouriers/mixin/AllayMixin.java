package com.kreidev.cmpackagecouriers.mixin;

import com.kreidev.cmpackagecouriers.courier.CourierAllayEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (value = Allay.class, remap = false)
public abstract class AllayMixin extends LivingEntity {

    private AllayMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    // Bypass pre-hurt logic
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Allay self = (Allay) (Object) this;
        if (self instanceof CourierAllayEntity) {
            boolean result = super.hurt(source, amount);
            cir.setReturnValue(result);
            cir.cancel();
        }
    }
}
