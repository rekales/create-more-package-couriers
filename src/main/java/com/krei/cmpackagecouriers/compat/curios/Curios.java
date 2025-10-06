package com.krei.cmpackagecouriers.compat.curios;

import com.krei.cmpackagecouriers.PackageCouriers;
import com.krei.cmpackagecouriers.transmitter.LocationTransmitterReg;

import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Curios integration that only loads when Curios is present.
 * This class handles the capability registration for location transmitters.
 */
public class Curios {

    public static void init(IEventBus modEventBus) {
        PackageCouriers.LOGGER.info("Initializing Curios integration for cmpackagecouriers");
        modEventBus.addListener(Curios::onClientSetup);
        modEventBus.addListener(Curios::registerCapabilities);
        modEventBus.addListener(Curios::gatherData);
    }

    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        PackageCouriers.LOGGER.info("Registering Curios capabilities for Location Transmitter");
        event.registerItem(
            top.theillusivec4.curios.api.CuriosCapability.ITEM,
            (stack, context) -> new ICurio() {

                @Override
                public ItemStack getStack() {
                    return stack;
                }

                @Override
                public void curioTick(SlotContext slotContext) {
                    // The transmitter doesn't need to tick, but this method can be used for any periodic logic
                    // For example, you could add visual effects or update transmission status here
                }
            },
            LocationTransmitterReg.LOCATION_TRANSMITTER.get());
    }

    public static void gatherData(GatherDataEvent event) {
        PackageCouriers.LOGGER.info("Adding Curios data provider to data generation");
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
        PackageCouriers.LOGGER.info("Setting up Curios client-side renderers");
        // Register Curios renderers during client setup
        CuriosRenderers.register();
    }
}