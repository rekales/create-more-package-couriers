package com.kreidev.cmpackagecouriers.courier;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

import static com.kreidev.cmpackagecouriers.PackageCouriers.*;

public class CourierAllayReg {

    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister
            .create(BuiltInRegistries.MEMORY_MODULE_TYPE, PackageCouriers.MOD_ID);

    private static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister
            .create(BuiltInRegistries.SENSOR_TYPE, PackageCouriers.MOD_ID);

    public static final Supplier<MemoryModuleType<PackageEntity>> NEAREST_PACKAGE_ENTITY = MEMORY_MODULE_TYPES
            .register("nearest_package", () -> new MemoryModuleType<>(Optional.empty()));
    public static final Supplier<MemoryModuleType<BlockPos>> TARGET_DEPOT = MEMORY_MODULE_TYPES
            .register("target_depot", () -> new MemoryModuleType<>(Optional.empty()));

    public static final Supplier<SensorType<PackageEntitySensor>> NEAREST_PACKAGE_SENSOR = SENSOR_TYPES
            .register("nearest_package", () -> new SensorType<>(PackageEntitySensor::new));
    public static final Supplier<SensorType<NearestDepotSensor>> NEAREST_DEPOT_SENSOR = SENSOR_TYPES
            .register("nearest_depot", () -> new SensorType<>(NearestDepotSensor::new));

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
        MEMORY_MODULE_TYPES.register(modEventBus);
        SENSOR_TYPES.register(modEventBus);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        CourierAllayRenderer.init();
        EntityRenderers.register(COURIER_ALLAY_ENTITY.get(), CourierAllayRenderer::new);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(COURIER_ALLAY_ENTITY.get(), Allay.createAttributes().build());
    }

}
