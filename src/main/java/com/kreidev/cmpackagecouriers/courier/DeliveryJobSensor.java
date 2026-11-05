package com.kreidev.cmpackagecouriers.courier;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DeliveryJobSensor <T extends LivingEntity & CourierMob> extends Sensor<T> {

    @Override
    protected void doTick(ServerLevel level, T entity) {
        Brain<?> brain = entity.getBrain();
        if (brain.hasMemoryValue(DeliveryNetworkReg.DELIVERY_JOB_MEM.get())) return;

        DeliveryNetwork network = entity.getDeliveryNetwork();
        DeliveryNetwork.Job job = network.getJob(entity);
        if (job == null) return;
        brain.setMemory(DeliveryNetworkReg.DELIVERY_JOB_MEM.get(), job);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(DeliveryNetworkReg.DELIVERY_JOB_MEM.get());
    }
}
