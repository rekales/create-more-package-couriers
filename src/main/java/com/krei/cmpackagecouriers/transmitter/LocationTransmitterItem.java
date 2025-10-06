package com.krei.cmpackagecouriers.transmitter;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.krei.cmpackagecouriers.transmitter.LocationTransmitterReg.TRANSMITTER_ENABLED;

public class LocationTransmitterItem extends Item {

    public LocationTransmitterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return isEnabled(stack);
    }

    public static boolean isEnabled(ItemStack stack) {
        return stack.getOrDefault(TRANSMITTER_ENABLED, CustomData.EMPTY).copyTag().getBoolean("Enabled");
    }

    public static void toggleState(ItemStack stack) {
        boolean currentState = isEnabled(stack);
        CompoundTag tag = stack.getOrDefault(TRANSMITTER_ENABLED, CustomData.EMPTY).copyTag();
        tag.putBoolean("Enabled", !currentState);
        stack.set(TRANSMITTER_ENABLED, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext tooltipContext,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, tooltipContext, tooltipComponents, tooltipFlag);

        boolean enabled = isEnabled(stack);
        Component statusText = Component.translatable(
                enabled ? "item.cmpackagecouriers.location_transmitter.enabled" : "item.cmpackagecouriers.location_transmitter.disabled"
        ).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
        
        tooltipComponents.add(Component.translatable("item.cmpackagecouriers.location_transmitter.status")
                .append(": ").append(statusText));
        
        tooltipComponents.add(Component.translatable("item.cmpackagecouriers.location_transmitter.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            boolean currentState = isEnabled(stack);
            toggleState(stack);
            
            Component message = Component.translatable(
                    !currentState ? "item.cmpackagecouriers.location_transmitter.activated" : "item.cmpackagecouriers.location_transmitter.deactivated"
            ).withStyle(!currentState ? ChatFormatting.GREEN : ChatFormatting.RED);
            
            player.displayClientMessage(message, true);
        }
        
        return InteractionResultHolder.success(stack);
    }
}
