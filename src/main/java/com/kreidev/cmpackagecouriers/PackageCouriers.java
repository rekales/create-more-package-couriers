package com.kreidev.cmpackagecouriers;

import com.kreidev.cmpackagecouriers.compat.Mods;
import com.kreidev.cmpackagecouriers.compat.create_factory_logistics.FactoryLogisticsCompat;
import com.kreidev.cmpackagecouriers.compat.curios.Curios;
import com.kreidev.cmpackagecouriers.compat.supplementaries.SupplementariesCompat;
import com.kreidev.cmpackagecouriers.plane.*;
import com.kreidev.cmpackagecouriers.ponder.PonderScenes;
import com.kreidev.cmpackagecouriers.sign.AddressSignReg;
import com.kreidev.cmpackagecouriers.stock_ticker.PortableStockTickerReg;
import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterReg;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@SuppressWarnings("unused")
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

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    public PackageCouriers(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        if (!Mods.CREATE_MOBILE_PACKAGES.isLoaded())
            PortableStockTickerReg.register();
        LocationTransmitterReg.register(modEventBus);
        CardboardPlaneReg.register(modEventBus);
        AddressSignReg.register(modEventBus);

        REGISTRATE.registerEventListeners(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        modEventBus.addListener(PackageCouriers::clientInit);

        Mods.CURIOS.executeIfInstalled(() -> () -> Curios.init(modEventBus));
        Mods.SUPPLEMENTARIES.executeIfInstalled(() -> SupplementariesCompat::init);
        Mods.CRATE_FACTORY_LOGISTICS.executeIfInstalled(() -> FactoryLogisticsCompat::init);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modEventBus.addListener(ServerConfig::onLoad);
        modEventBus.addListener(ServerConfig::onReload);

        // Event Handler Class: AddressMarkerHandler
        // Event Handler Class: CardboardPlaneManager
        // Event Handler Class: StockTickerIntegration
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new PonderScenes());
    }

    public static ResourceLocation resLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

}