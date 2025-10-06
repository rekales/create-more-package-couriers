package com.krei.cmpackagecouriers.stock_ticker;

import com.krei.cmpackagecouriers.PackageCouriers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = PackageCouriers.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LocationTransmitterClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    PortableStockTickerReg.LOCATION_TRANSMITTER.get(),
                    ResourceLocation.fromNamespaceAndPath(PackageCouriers.MODID, "enabled"),
                    (stack, level, entity, seed) -> LocationTransmitterItem.isEnabled(stack) ? 1.0f : 0.0f
            );
        });
    }
}
