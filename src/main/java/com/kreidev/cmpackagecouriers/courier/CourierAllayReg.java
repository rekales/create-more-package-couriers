package com.kreidev.cmpackagecouriers.courier;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.allay.Allay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import static com.kreidev.cmpackagecouriers.PackageCouriers.*;

public class CourierAllayReg {

    public static final EntityEntry<CourierAllayEntity> COURIER_ALLAY_ENTITY = REGISTRATE
            .entity("courier_allay", CourierAllayEntity::new, MobCategory.CREATURE)
            .properties(p -> p
                    .sized(0.35F, 0.6F)
                    .eyeHeight(0.36F)
                    .ridingOffset(0.04F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
            )
//            .attributes(Allay::createAttributes)
//            .renderer(() -> CourierAllayRenderer::new)
            .register();

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CourierAllayReg::clientInit);
        modEventBus.addListener(CourierAllayReg::registerAttributes);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        CourierAllayRenderer.init();
        EntityRenderers.register(COURIER_ALLAY_ENTITY.get(), CourierAllayRenderer::new);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(COURIER_ALLAY_ENTITY.get(), Allay.createAttributes().build());
    }

}
