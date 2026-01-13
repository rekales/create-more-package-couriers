package com.kreidev.cmpackagecouriers.plane;

import com.mojang.serialization.Codec;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Supplier;

import static com.kreidev.cmpackagecouriers.PackageCouriers.*;

@SuppressWarnings("unused")
public class CardboardPlaneReg {

    public static final EntityEntry<CardboardPlaneEntity> CARDBOARD_PLANE_ENTITY = REGISTRATE
            .entity("cardboard_plane", CardboardPlaneEntity::createEmpty, MobCategory.MISC)
            .properties(p -> p
                    .sized(0.5f, 0.5f)
                    .eyeHeight(0.25f)
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

    public static final Supplier<DataComponentType<ItemContainerContents>> PLANE_PACKAGE = DATA_COMPONENTS
            .registerComponentType("plane_package", builder -> builder
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC));
    public static final Supplier<DataComponentType<Boolean>> PRE_OPENED = DATA_COMPONENTS
            .registerComponentType("plane_preopened", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CardboardPlaneReg::clientInit);
        NeoForge.EVENT_BUS.addListener(CardboardPlaneSavedData::onServerStarting);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        CardboardPlaneEntityRenderer.init();
        CardboardPlaneItemRenderer.init();
        // Somethings wrong with registrate that makes me wanna commit seppuku
        EntityRenderers.register(
                CARDBOARD_PLANE_ENTITY.get(),
                CardboardPlaneEntityRenderer::new
        );
    }}
