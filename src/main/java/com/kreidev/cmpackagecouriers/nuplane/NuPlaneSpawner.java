package com.kreidev.cmpackagecouriers.nuplane;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NuPlaneSpawner extends Item {

    public NuPlaneSpawner(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide()) {
            Vec3 pos = player.getEyePosition();
            Vec3 deltaMovement = player.getLookAngle().normalize().scale(0.4f);
            CardboardPlaneManager.addPlane(level, pos, deltaMovement);
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }
}
