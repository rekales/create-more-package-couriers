package com.kreidev.cmpackagecouriers.plane;

import com.simibubi.create.content.logistics.box.PackageEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CardboardPlanePartsItem extends Item {
    public CardboardPlanePartsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof PackageEntity entity) {
            player.getInventory().placeItemBackInInventory(CardboardPlaneItem.withPackage(entity.box));
            stack.shrink(1);
            entity.remove(Entity.RemovalReason.DISCARDED);
            return InteractionResult.SUCCESS;
        } else {
            return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        }
    }
}
