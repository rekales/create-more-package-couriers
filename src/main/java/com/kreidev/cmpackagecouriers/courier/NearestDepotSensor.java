package com.kreidev.cmpackagecouriers.courier;

import com.google.common.collect.ImmutableSet;
import com.kreidev.cmpackagecouriers.CourierTarget;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NearestDepotSensor extends Sensor<LivingEntity> {

    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        BlockPos closest = CourierTarget.activeTargets.keySet().stream()
                .filter(target -> target.getType() == CourierTarget.Type.BLOCK)
                .filter(target -> target.getDim() == entity.level().dimension())
                .map(CourierTarget::getPos)
                .filter(pos -> level.getBlockEntity(BlockPos.containing(pos)) instanceof DepotBlockEntity)
                .filter(pos -> pos.closerThan(entity.position(), 32))
                .min(Comparator.comparingDouble(pos -> pos.distanceToSqr(entity.position())))
                .map(BlockPos::containing)
                .orElse(null);

        Brain<?> brain = entity.getBrain();
        brain.setMemory(CourierAllayReg.TARGET_DEPOT.get(), closest);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(CourierAllayReg.TARGET_DEPOT.get());
    }
}
