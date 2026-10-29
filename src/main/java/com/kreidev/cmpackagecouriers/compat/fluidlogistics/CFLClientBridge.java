package com.kreidev.cmpackagecouriers.compat.fluidlogistics;

import com.simibubi.create.foundation.utility.CreateLang;
import com.yision.fluidlogistics.api.packager.PackageResourceDisplay;
import com.yision.fluidlogistics.api.packager.PackageResources;
import com.yision.fluidlogistics.api.packager.client.PackageResourceClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CFLClientBridge {

    private CFLClientBridge() {
    }

    public static boolean tryRenderStockKeeperAmount(GuiGraphics graphics, ItemStack stack, int amount) {
        return CFLBridge.safeCall(false,
                () -> PackageResourceClient.tryRenderStockKeeperAmount(graphics, stack, amount));
    }

    public static List<Component> tooltipLines(ItemStack stack, int amount, boolean recipeHovered,
                                               boolean orderHovered) {
        return CFLBridge.safeCall(List.of(), () -> {
            PackageResourceDisplay.TooltipContext context = recipeHovered
                    ? PackageResourceDisplay.TooltipContext.STOCK_KEEPER_CRAFTABLE
                    : orderHovered
                    ? PackageResourceDisplay.TooltipContext.STOCK_KEEPER_ORDER
                    : PackageResourceDisplay.TooltipContext.STOCK_KEEPER_INVENTORY;
            ArrayList<Component> lines = PackageResources.tooltipOf(
                            stack, amount, Minecraft.getInstance().options.advancedItemTooltips, context)
                    .map(ArrayList::new)
                    .orElseGet(ArrayList::new);
            if (recipeHovered && !lines.isEmpty()) {
                lines.set(0, CreateLang.translateDirect("gui.stock_keeper.craft", lines.getFirst().copy()));
            }
            return lines;
        });
    }
}
