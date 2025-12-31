package com.kreidev.cmpackagecouriers;

import com.kreidev.cmpackagecouriers.compat.Mods;
import com.kreidev.cmpackagecouriers.compat.curios.Curios;
import com.kreidev.cmpackagecouriers.compat.supplementaries.SupplementariesCompat;
import com.kreidev.cmpackagecouriers.nuplane.*;
import com.kreidev.cmpackagecouriers.plane.*;
import com.kreidev.cmpackagecouriers.ponder.PonderScenes;
import com.kreidev.cmpackagecouriers.stock_ticker.PortableStockTickerReg;
import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterReg;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(PackageCouriers.MOD_ID)
public class PackageCouriers {
    public static final String MOD_ID = "cmpackagecouriers";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate
            .create(MOD_ID)
            .defaultCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey());

    static {
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public static final EntityEntry<CardboardPlaneEntity> CARDBOARD_PLANE_ENTITY = REGISTRATE
        .entity("cardboard_plane", CardboardPlaneEntity::createEmpty, MobCategory.MISC)
        .properties(p -> p
                .sized(0.5f, 0.5f)
                .eyeHeight(0.25f)
                .clientTrackingRange(80)
                .updateInterval(1))
//        .renderer(() -> DeliveryPlaneRenderer::new)
        .register();

    public static final EntityEntry<CardboardPlaneNuEntity> CARDBOARD_PLANE_NU_ENTITY = REGISTRATE
            .entity("cardboard_nu_plane", CardboardPlaneNuEntity::createEmpty, MobCategory.MISC)
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

    public static final ItemEntry<NuPlaneSpawner> CARDBOARD_PLANE_SPAWNER = REGISTRATE
            .item("nu_plane_spawner", NuPlaneSpawner::new)
            .register();

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final Supplier<DataComponentType<ItemContainerContents>> PLANE_PACKAGE = DATA_COMPONENTS
            .registerComponentType("plane_package", builder -> builder
                            .persistent(ItemContainerContents.CODEC)
                            .networkSynchronized(ItemContainerContents.STREAM_CODEC));
    public static final Supplier<DataComponentType<Boolean>> PRE_OPENED = DATA_COMPONENTS
            .registerComponentType("plane_preopened", builder -> builder
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL));

    public PackageCouriers(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        if (!Mods.CREATE_MOBILE_PACKAGES.isLoaded())
            PortableStockTickerReg.register();
        LocationTransmitterReg.register();
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        REGISTRATE.registerEventListeners(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        modEventBus.addListener(PackageCouriers::clientInit);

        Mods.CURIOS.executeIfInstalled(() -> () -> Curios.init(modEventBus));
        Mods.SUPPLEMENTARIES.executeIfInstalled(() -> SupplementariesCompat::init);

        CardboardPlaneEntity.init();
        modEventBus.addListener(ServerConfig::onLoad);
        modEventBus.addListener(ServerConfig::onReload);
        modEventBus.addListener(PackageCouriers::registerPackets);
        NeoForge.EVENT_BUS.addListener(CardboardPlaneSavedData::onServerStarting);
        // Event Handler Class: AddressMarkerHandler
        // Event Handler Class: CardboardPlaneManager
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        CardboardPlaneEntityRenderer.init();
        CardboardPlaneItemRenderer.init();
        PonderIndex.addPlugin(new PonderScenes());
        // Somethings wrong with registrate that makes me wanna commit seppuku
        EntityRenderers.register(
                CARDBOARD_PLANE_ENTITY.get(),
                CardboardPlaneEntityRenderer::new
        );
        EntityRenderers.register(
                CARDBOARD_PLANE_NU_ENTITY.get(),
                CardboardPlaneNuEntityRenderer::new
        );

    }

    public static void registerPackets(RegisterPayloadHandlersEvent event) {
//        event.registrar("1").executesOn(HandlerThread.MAIN)
//                .playToClient(
//                        CardboardPlanePacket.TYPE,
//                        CardboardPlanePacket.STREAM_CODEC,
//                        CardboardPlaneManagerClient::planePacketHandler
//                );

    }

    public static ResourceLocation resLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    // TODO: Replace depot sign based targeting with a new sign block

}
