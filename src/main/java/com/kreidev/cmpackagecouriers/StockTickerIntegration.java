package com.kreidev.cmpackagecouriers;

import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.kreidev.cmpackagecouriers.PackageCouriers.MOD_ID;

// copied from DadudeGaming/Create.Mobile.packages.Unofficial under MIT License
@Mod.EventBusSubscriber(modid = MOD_ID)
public class StockTickerIntegration {

    private static void rewriteAddressIfNeeded(Player player, ItemStack heldItem) {
        if (heldItem.getItem() instanceof ShoppingListItem) {
            String currentAddress = ShoppingListItem.getAddress(heldItem);
            if (currentAddress.toLowerCase().contains("<>")) {
                String playerIdentifier = player.getDisplayName().getString();
                String newAddress = currentAddress.replaceAll("<>", "<" + playerIdentifier + ">");
                ShoppingListItem.saveList(heldItem, ShoppingListItem.getList(heldItem), newAddress);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getLevel().isClientSide()) return;
        if (!ServerConfig.shopAddressReplacement) return;

        Entity target = event.getTarget();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);

        if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) return;

        BlockPos stockTickerPos = StockTickerInteractionHandler.getStockTickerPosition(target);
        if (stockTickerPos != null) {
            rewriteAddressIfNeeded(player, heldItem);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!ServerConfig.shopAddressReplacement) return;

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        BlockPos pos = event.getPos();

        if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) return;

        if (event.getLevel().getBlockState(pos).getBlock() instanceof BlazeBurnerBlock) {
            rewriteAddressIfNeeded(player, heldItem);
        }
    }
}