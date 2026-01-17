package com.kreidev.cmpackagecouriers.compat.curios;

import com.kreidev.cmpackagecouriers.stock_ticker.PortableStockTickerReg;
import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterReg;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
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
//        modEventBus.addListener(Curios::registerCapabilities);
        modEventBus.addListener(Curios::gatherData);
    }

//    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
//        event.registerItem(
//            top.theillusivec4.curios.api.CuriosCapability.ITEM,
//            (stack, context) -> new ICurio() {
//
//                @Override
//                public ItemStack getStack() {
//                    return stack;
//                }
//
//                @Override
//                public void curioTick(SlotContext slotContext) {
//                    // The transmitter doesn't need to tick, but this method can be used for any periodic logic
//                    // For example, you could add visual effects or update transmission status here
//                }
//            },
//            LocationTransmitterReg.LOCATION_TRANSMITTER.get());
//    }

    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
            event.includeServer(),
            new CuriosDataGenerator(
                event.getGenerator().getPackOutput(),
                event.getLookupProvider(),
                event.getExistingFileHelper()
            )
        );
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        // Register Curios renderers during client setup
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