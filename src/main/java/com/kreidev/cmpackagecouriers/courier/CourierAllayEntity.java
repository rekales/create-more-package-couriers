package com.kreidev.cmpackagecouriers.courier;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CourierAllayEntity extends Allay {

    protected static final ImmutableList<SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(
            CourierAllayReg.NEAREST_PACKAGE_SENSOR.get(),
            CourierAllayReg.NEAREST_DEPOT_SENSOR.get(),
//            SensorType.NEAREST_PLAYERS,
            SensorType.HURT_BY
    );

    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.PATH,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.IS_PANICKING,
            CourierAllayReg.NEAREST_PACKAGE_ENTITY.get(),
            CourierAllayReg.TARGET_DEPOT.get()
    );

    public CourierAllayEntity(EntityType<? extends Allay> entityType, Level level) {
        super(entityType, level);

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
}
