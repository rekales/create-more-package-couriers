package com.kreidev.cmpackagecouriers.transmitter;

import com.kreidev.cmpackagecouriers.CourierTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LocationTransmitterItem extends Item implements ICurioItem {

    public LocationTransmitterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, tooltipContext, tooltipComponents, tooltipFlag);

        Component statusText;
        if (isEnabled(stack)) {
            statusText = Component.translatable("item.cmpackagecouriers.location_transmitter.enabled")
                    .withStyle(ChatFormatting.GREEN);
        } else {
            statusText = Component.translatable("item.cmpackagecouriers.location_transmitter.disabled")
                    .withStyle(ChatFormatting.RED);
        }

        tooltipComponents.add(Component.translatable("item.cmpackagecouriers.location_transmitter.status")
                .append(": ").append(statusText));

        tooltipComponents.add(Component.translatable("item.cmpackagecouriers.location_transmitter.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        boolean currentState = isEnabled(stack);
        setEnabled(stack, !currentState);

        Component message;
        if (!currentState) {
            message = Component.translatable("item.cmpackagecouriers.location_transmitter.activated")
                    .withStyle(ChatFormatting.GREEN);
        } else {
            message = Component.translatable("item.cmpackagecouriers.location_transmitter.deactivated")
                    .withStyle(ChatFormatting.RED);
        }

        player.displayClientMessage(message, true);
        // Attempt to nudge the client to refresh the inventory/tool visuals immediately.
        if (player instanceof ServerPlayer serverPlayer) {
            // broadcastChanges will send container/inventory updates to the client; this
            // helps ensure item data components are synced promptly.
            serverPlayer.inventoryMenu.broadcastChanges();
        }
        
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()) return;
        if (isEnabled(stack)) {
            CourierTarget.addOrUpdateTarget(new CourierTarget(entity.getName().getString(), entity));
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        ICurioItem.super.curioTick(slotContext, stack);
        Entity entity = slotContext.entity();
        if (!entity.level().isClientSide()) return;
        if (isEnabled(stack)) {
            CourierTarget.addOrUpdateTarget(new CourierTarget(entity.getName().getString(), entity));
        }
    }

    public static boolean isEnabled(ItemStack stack) {
        return stack.getOrDefault(LocationTransmitterReg.TRANSMITTER_ENABLED, false);
    }

    public static void setEnabled(ItemStack stack, boolean enabled) {
        if (stack.getItem() instanceof LocationTransmitterItem) {
            stack.set(LocationTransmitterReg.TRANSMITTER_ENABLED, enabled);
        }
    }
}
