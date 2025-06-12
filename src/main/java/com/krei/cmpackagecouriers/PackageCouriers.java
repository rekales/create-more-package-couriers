package com.krei.cmpackagecouriers;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(PackageCouriers.MODID)
public class PackageCouriers {
    public static final String MODID = "cmpackagecouriers";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate
            .create(MODID)
            .defaultCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey());

    public static final EntityEntry<DeliveryPlaneEntity> DELIVERY_PLANE_ENTITY = REGISTRATE
            .entity("delivery_plane", DeliveryPlaneEntity::createEmpty, MobCategory.MISC)
            .properties(p -> p
                    .sized(0.5f, 0.5f)
                    .eyeHeight(0.25f)
                    .noSave() // Temp
                    .updateInterval(1))
            .renderer(() -> ArrowRendererDuplicate::new)
            .register();

    protected static final PartialModel CASING = PartialModel.of(Create.asResource("block/andesite_casing"));

    public PackageCouriers(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE.registerEventListeners(modEventBus);
    }
}
