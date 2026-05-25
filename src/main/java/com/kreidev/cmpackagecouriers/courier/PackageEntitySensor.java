package com.kreidev.cmpackagecouriers.courier;

import com.google.common.collect.ImmutableSet;
import com.simibubi.create.content.logistics.box.PackageEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Almost exact copy of NearestLivingEntitySensor.class
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PackageEntitySensor extends Sensor<LivingEntity> {

    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        AABB aabb = entity.getBoundingBox().inflate(this.radiusXZ(), this.radiusY(), this.radiusXZ());
        List<PackageEntity> list = level.getEntitiesOfClass(PackageEntity.class, aabb, p_26717_ -> p_26717_ != entity && p_26717_.isAlive());
        list.sort(Comparator.comparingDouble(entity::distanceToSqr));
        Brain<?> brain = entity.getBrain();
        if (!list.isEmpty()) {
            brain.setMemory(CourierAllayReg.NEAREST_PACKAGE_ENTITY.get(), list.getFirst());
        } else {
            brain.setMemory(CourierAllayReg.NEAREST_PACKAGE_ENTITY.get(), Optional.empty());
        }
    }

    protected int radiusXZ() {
        return 16;
    }

    protected int radiusY() {
        return 16;
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(CourierAllayReg.NEAREST_PACKAGE_ENTITY.get());
    }
}
