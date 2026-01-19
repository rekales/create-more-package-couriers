package com.kreidev.cmpackagecouriers.compat.curios;

import com.kreidev.cmpackagecouriers.CourierTarget;
import com.kreidev.cmpackagecouriers.stock_ticker.PortableStockTickerReg;
import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterItem;
import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterReg;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

/**
 * Curios integration that only loads when Curios is present.
 * This class handles the capability registration for location transmitters.
 */
public class CuriosCompat {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(CuriosCompat::onClientSetup);
        modEventBus.addListener(CuriosCompat::onCommonSetup);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        CuriosRenderers.register();
    }

    private static void onCommonSetup(final FMLCommonSetupEvent event) {
        CuriosApi.registerCurio(LocationTransmitterReg.LOCATION_TRANSMITTER.get(),
                new ICurioItem() {
                    @Override
                    public void curioTick(SlotContext slotContext, ItemStack stack) {
                        Entity entity = slotContext.entity();
                        if (!entity.level().isClientSide()) return;
                        if (LocationTransmitterItem.isEnabled(stack)) {
                            CourierTarget.addOrUpdateTarget(new CourierTarget(entity.getName().getString(), entity));
                        }
                    }
                }
        );
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