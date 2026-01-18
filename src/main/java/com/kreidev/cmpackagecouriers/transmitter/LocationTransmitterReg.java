package com.kreidev.cmpackagecouriers.transmitter;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.kreidev.cmpackagecouriers.PackageCouriers.REGISTRATE;

@SuppressWarnings("unused")
public class LocationTransmitterReg {

    public static final ItemEntry<LocationTransmitterItem> LOCATION_TRANSMITTER = REGISTRATE
            .item("location_transmitter", LocationTransmitterItem::new)
            .register();

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(LocationTransmitterReg::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        ItemProperties.register(
                LocationTransmitterReg.LOCATION_TRANSMITTER.get(),
                ResourceLocation.fromNamespaceAndPath(PackageCouriers.MOD_ID, "enabled"),
                (stack, level, entity, seed) -> LocationTransmitterItem.isEnabled(stack) ? 1.0f : 0.0f
        );
    }
}
