package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

// NOTE: Might need to remove projectileItem interface
// NOTE: Using a compass with target in an item frame or placard to set a coordinate address
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CardboardPlaneItem extends Item implements EjectorLaunchEffect {

    public CardboardPlaneItem(Properties p) {
        super(p.stacksTo(1));
    }

    // TODO: throwing angles are a bit off, double check later.
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player
                && this.getUseDuration(stack, entityLiving) - timeLeft >= 10
                && !level.isClientSide()) {

            Vec3 pos = player.getEyePosition().subtract(0, 0.1, 0);
            boolean wasFired = this.fire(stack, level, pos, player.getYRot(), player.getXRot());
            if (wasFired) {
                stack.shrink(1);
            } else {
                player.displayClientMessage(Component.translatable(PackageCouriers.MOD_ID + ".message.no_address"), true);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isCrouching()) {
            ItemStack box = CardboardPlaneItem.getPackage(player.getItemInHand(hand));
            player.getItemInHand(hand).shrink(1);
            player.getInventory().placeItemBackInInventory(box);
            player.getInventory().placeItemBackInInventory(CardboardPlaneReg.CARDBOARD_PLANE_PARTS_ITEM.asStack());
        } else {
            player.startUsingItem(hand);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean onEject(ItemStack stack, Level level, BlockPos pos, Direction facing) {
        float yaw = switch (facing) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST  -> 90f;
            default    -> -90f;
        };
        return fire(stack, level, Vec3.atCenterOf(pos.above()), yaw, -37.5F);
    }

    public boolean fire(ItemStack stack, Level level, Vec3 pos, float yaw, float pitch) {
        if (level.isClientSide())
            return false;

        PackageCouriers.LOGGER.debug(yaw+" yaw");
        PackageCouriers.LOGGER.debug(pitch+" pitch");

        ItemStack packageItem = getPackage(stack);
        boolean unpack = stack.getOrDefault(CardboardPlaneReg.PRE_OPENED, false);
        return CardboardPlaneManager.addPlane(level, pos, yaw, pitch, packageItem, unpack);
    }

    public static ItemStack withPackage(ItemStack box) {
        ItemStack plane = CardboardPlaneReg.CARDBOARD_PLANE_ITEM.asStack();
        setPackage(plane, box);
        return plane;
    }

    public static void setPackage(ItemStack plane, ItemStack box) {
        if (box.getItem() instanceof PackageItem) {
            ItemContainerContents container = ItemContainerContents.fromItems(NonNullList.of(ItemStack.EMPTY, box.copy()));
            plane.set(CardboardPlaneReg.PLANE_PACKAGE, container);
        }
    }

    public static ItemStack getPackage(ItemStack plane) {
        ItemContainerContents container = plane.get(CardboardPlaneReg.PLANE_PACKAGE);
        if (container == null)
            return ItemStack.EMPTY;
        return container.getStackInSlot(0);
    }

    public static void setPreOpened(ItemStack plane, boolean preopened) {
        if (plane.getItem() instanceof CardboardPlaneItem
                && plane.get(CardboardPlaneReg.PLANE_PACKAGE) instanceof ItemContainerContents container
                && PackageItem.isPackage(container.getStackInSlot(0))) {
            plane.set(CardboardPlaneReg.PRE_OPENED, preopened);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ItemStack box = getPackage(stack);
        if (stack.getOrDefault(CardboardPlaneReg.PRE_OPENED, false))
            tooltipComponents.add(Component.translatable("tooltip.cmpackagecouriers.cardboard_plane.preopened")
                    .withStyle(ChatFormatting.AQUA));
        box.getItem().appendHoverText(box, context, tooltipComponents, tooltipFlag);
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new CardboardPlaneItemRenderer()));
    }
}
