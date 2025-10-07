package com.krei.cmpackagecouriers.transmitter;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.CustomData;

import java.util.function.UnaryOperator;

import static com.krei.cmpackagecouriers.PackageCouriers.DATA_COMPONENTS;
import static com.krei.cmpackagecouriers.PackageCouriers.REGISTRATE;

public class LocationTransmitterReg {

    public static final ItemEntry<LocationTransmitterItem> LOCATION_TRANSMITTER =
            REGISTRATE.item("location_transmitter", LocationTransmitterItem::new)
                    .register();

    public static final DataComponentType<CustomData> TRANSMITTER_ENABLED = register(
            "transmitter_enabled",
            builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC)
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    public static void register() {
        // This method is called to ensure static initialization
    }
}
