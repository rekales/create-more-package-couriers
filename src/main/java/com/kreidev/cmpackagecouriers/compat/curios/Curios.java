package com.kreidev.cmpackagecouriers.compat.curios;

import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterReg;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Curios integration that only loads when Curios is present.
 * This class handles the capability registration for location transmitters.
 */
public class Curios {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(Curios::onClientSetup);
        modEventBus.addListener(Curios::registerCapabilities);
        modEventBus.addListener(Curios::gatherData);
    }

    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
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
}