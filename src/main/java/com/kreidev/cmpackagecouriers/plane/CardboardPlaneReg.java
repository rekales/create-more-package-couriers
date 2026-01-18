package com.kreidev.cmpackagecouriers.plane;

import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.kreidev.cmpackagecouriers.PackageCouriers.*;

@SuppressWarnings("unused")
public class CardboardPlaneReg {

    public static final EntityEntry<CardboardPlaneEntity> CARDBOARD_PLANE_ENTITY = REGISTRATE
            .entity("cardboard_plane", CardboardPlaneEntity::createEmpty, MobCategory.MISC)
            .properties(p -> p
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(80)
                    .noSave()
                    .updateInterval(1))
//        .renderer(() -> DeliveryPlaneRenderer::new)
            .register();

    public static final ItemEntry<CardboardPlaneItem> CARDBOARD_PLANE_ITEM = REGISTRATE
            .item("cardboard_plane", CardboardPlaneItem::new)
            .model((ctx, prov) -> {}) // Skip model generation - uses custom renderer
            .register();

    public static final ItemEntry<CardboardPlanePartsItem> CARDBOARD_PLANE_PARTS_ITEM = REGISTRATE
            .item("cardboard_plane_parts", CardboardPlanePartsItem::new)
            .model((ctx, prov) -> {}) // Skip model generation - uses custom renderer
            .register();

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CardboardPlaneReg::clientInit);
        MinecraftForge.EVENT_BUS.addListener(CardboardPlaneSavedData::onServerStarting);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        CardboardPlaneEntityRenderer.init();
        CardboardPlaneItemRenderer.init();
        // Somethings wrong with registrate that makes me wanna commit seppuku
        EntityRenderers.register(
                CARDBOARD_PLANE_ENTITY.get(),
                CardboardPlaneEntityRenderer::new
        );
    }
}
