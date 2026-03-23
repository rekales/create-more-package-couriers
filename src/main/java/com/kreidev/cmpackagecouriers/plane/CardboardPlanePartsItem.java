package com.kreidev.cmpackagecouriers.plane;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CardboardPlanePartsItem extends Item {

    public CardboardPlanePartsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (mainHand.is(CardboardPlaneReg.CARDBOARD_PLANE_PARTS_ITEM) && offHand.getItem() instanceof PackageItem) {
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(mainHand);
        }
        return InteractionResultHolder.fail(mainHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return stack;

        ItemStack box = player.getItemInHand(InteractionHand.OFF_HAND);
        player.setItemInHand(InteractionHand.OFF_HAND, CardboardPlaneItem.withPackage(box));

        stack.shrink(1);
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 20;
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
