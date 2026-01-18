package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
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

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player
                && this.getUseDuration(stack) - timeLeft >= 10
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
    public int getUseDuration(ItemStack stack) {
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

        ItemStack packageItem = getPackage(stack);
        return CardboardPlaneManager.addPlane(level, pos, yaw, pitch, packageItem, isPreOpened(stack));
    }

    public static ItemStack withPackage(ItemStack box) {
        ItemStack plane = CardboardPlaneReg.CARDBOARD_PLANE_ITEM.asStack();
        setPackage(plane, box);
        return plane;
    }

    public static void setPackage(ItemStack plane, ItemStack box) {
        if (plane.getItem() instanceof CardboardPlaneItem && box.getItem() instanceof PackageItem) {
            CompoundTag nbtStack = new CompoundTag();
            box.save(nbtStack);
            plane.getOrCreateTag().put("PlanePackage", nbtStack);
        }
    }

    public static ItemStack getPackage(ItemStack plane) {
        if (plane.getItem() instanceof CardboardPlaneItem) {
            CompoundTag nbt = plane.getOrCreateTag();
            if (nbt.contains("PlanePackage", Tag.TAG_COMPOUND)) {
                CompoundTag storedItemTag = nbt.getCompound("PlanePackage");
                return ItemStack.of(storedItemTag);
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isPreOpened(ItemStack plane) {
        if (plane.getItem() instanceof CardboardPlaneItem) {
            CompoundTag nbt = plane.getOrCreateTag();
            if (nbt.contains("PreOpened")) {
                return nbt.getBoolean("PreOpened");
            }
        }
        return false;
    }

    public static void setPreOpened(ItemStack plane, boolean preopened) {
        if (plane.getItem() instanceof CardboardPlaneItem) {
            plane.getOrCreateTag().putBoolean("PreOpened", preopened);
        }
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ItemStack box = getPackage(stack);
        if (isPreOpened(stack)) {
            tooltipComponents.add(Component.translatable("tooltip.cmpackagecouriers.cardboard_plane.preopened").withStyle(ChatFormatting.AQUA));
        }
        box.getItem().appendHoverText(box, level, tooltipComponents, tooltipFlag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new CardboardPlaneItemRenderer()));
    }
}
