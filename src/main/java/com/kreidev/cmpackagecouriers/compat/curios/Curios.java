package com.kreidev.cmpackagecouriers.compat.curios;

import com.kreidev.cmpackagecouriers.stock_ticker.PortableStockTickerReg;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

/**
 * Curios integration that only loads when Curios is present.
 * This class handles the capability registration for location transmitters.
 */
public class Curios {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(Curios::onClientSetup);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        CuriosRenderers.register();
    }

    private static Optional<Map<String, ICurioStacksHandler>> resolveCuriosMap(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(ICuriosItemHandler::getCurios);
    }

    public static ItemStack findPortableStockTickerCurios(LivingEntity entity) {
        return resolveCuriosMap(entity).map(curiosMap -> {
            for (ICurioStacksHandler stacksHandler : curiosMap.values()) {
                // Search all the curio slots for PST existing
                int slots = stacksHandler.getSlots();
                for (int slot = 0; slot < slots; slot++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(slot);
                    if (PortableStockTickerReg.PORTABLE_STOCK_TICKER.isIn(stack)) {
                        return stack;
                    }
                }
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }
}