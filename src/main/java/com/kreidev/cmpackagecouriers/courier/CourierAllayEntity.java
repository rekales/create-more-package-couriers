package com.kreidev.cmpackagecouriers.courier;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CourierAllayEntity extends Allay implements CourierMob {

    protected final DeliveryNetwork network;

    protected static final ImmutableList<SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(
            DeliveryNetworkReg.NEAREST_PACKAGE_SENSOR.get(),
            DeliveryNetworkReg.NEAREST_DEPOT_SENSOR.get(),
            DeliveryNetworkReg.NEAREST_DEPOT_SENSOR.get(),
            SensorType.HURT_BY
    );

    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.PATH,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.IS_PANICKING,
            DeliveryNetworkReg.NEAREST_PACKAGE_ENTITY.get(),
            DeliveryNetworkReg.TARGET_DEPOT.get(),
            DeliveryNetworkReg.DELIVERY_JOB_MEM.get()
    );

    public CourierAllayEntity(EntityType<? extends Allay> entityType, Level level) {
        super(entityType, level);
        this.network = null;

        this.getPersistentData().putBoolean("TrainHat", true);
    }

    @Override
    protected Brain.Provider<Allay> brainProvider() {
        return Brain.provider(CourierAllayEntity.MEMORY_TYPES, CourierAllayEntity.SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CourierAllayAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public DeliveryNetwork getDeliveryNetwork() {
        return this.network;
    }

    @Override
    public @Nullable DeliveryNetwork.Job getCurrentJob() {
        return this.getBrain().getMemory(DeliveryNetworkReg.DELIVERY_JOB_MEM.get()).orElse(null);
    }
}
