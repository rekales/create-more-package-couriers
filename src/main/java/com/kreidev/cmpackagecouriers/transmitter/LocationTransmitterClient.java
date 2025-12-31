package com.kreidev.cmpackagecouriers.transmitter;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static com.kreidev.cmpackagecouriers.PackageCouriers.resLoc;

@EventBusSubscriber(modid = PackageCouriers.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LocationTransmitterClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    LocationTransmitterReg.LOCATION_TRANSMITTER.get(),
                    resLoc("enabled"),
                    (stack, level, entity, seed) -> LocationTransmitterItem.isEnabled(stack) ? 1.0f : 0.0f
            );
        });
    }
}
