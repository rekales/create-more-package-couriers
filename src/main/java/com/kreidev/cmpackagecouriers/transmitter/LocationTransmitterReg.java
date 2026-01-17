package com.kreidev.cmpackagecouriers.transmitter;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraftforge.eventbus.api.IEventBus;

import static com.kreidev.cmpackagecouriers.PackageCouriers.REGISTRATE;

@SuppressWarnings("unused")
public class LocationTransmitterReg {

    public static final ItemEntry<LocationTransmitterItem> LOCATION_TRANSMITTER = REGISTRATE
            .item("location_transmitter", LocationTransmitterItem::new)
            .register();

    public static void register(IEventBus modEventBus) {

    }
}
