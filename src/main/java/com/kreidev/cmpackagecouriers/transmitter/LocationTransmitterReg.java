package com.kreidev.cmpackagecouriers.transmitter;

import com.mojang.serialization.Codec;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;

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

    }
}
