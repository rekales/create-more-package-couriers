package com.krei.cmpackagecouriers.events;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.BlockPos;

import static com.krei.cmpackagecouriers.PackageCouriers.MODID;

import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
// copied from DadudeGaming/Create.Mobile.packages.Unofficial under MIT License
@EventBusSubscriber(modid = MODID)
public class StockTickerIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static void rewriteAddressIfNeeded(Player player, ItemStack heldItem) {
        if (heldItem.getItem() instanceof ShoppingListItem) {
            String currentAddress = ShoppingListItem.getAddress(heldItem);
            if (currentAddress.toLowerCase().contains("@player")) {
                String playerIdentifier = player.getDisplayName().getString();
                String newAddress = currentAddress.replaceAll("(?i)@player", "@" + playerIdentifier);
                ShoppingListItem.saveList(heldItem, ShoppingListItem.getList(heldItem), newAddress);
            }
        }
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity target = event.getTarget();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);

        if (player == null || target == null || player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return;
        }

        BlockPos stockTickerPos = StockTickerInteractionHandler.getStockTickerPosition(target);
        if (stockTickerPos != null) {
            rewriteAddressIfNeeded(player, heldItem);
        }
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onRightClickBlock(RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        BlockPos pos = event.getPos();

        if (player == null || player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return;
        }

        // Check if the block is a Blaze Burner
        if (event.getLevel().getBlockState(pos).getBlock() instanceof BlazeBurnerBlock) {
            rewriteAddressIfNeeded(player, heldItem);
        }
    }
}