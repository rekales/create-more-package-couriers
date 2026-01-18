package com.kreidev.cmpackagecouriers.transmitter;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Supplier;

import static com.kreidev.cmpackagecouriers.PackageCouriers.DATA_COMPONENTS;
import static com.kreidev.cmpackagecouriers.PackageCouriers.REGISTRATE;

@SuppressWarnings("unused")
public class LocationTransmitterReg {

    public static final ItemEntry<LocationTransmitterItem> LOCATION_TRANSMITTER = REGISTRATE
            .item("location_transmitter", LocationTransmitterItem::new)
            .register();

    public static final Supplier<DataComponentType<Boolean>> TRANSMITTER_ENABLED = DATA_COMPONENTS
            .registerComponentType("transmitter_enabled", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

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
