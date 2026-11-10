package com.kreidev.cmpackagecouriers.courier.entity;

import com.kreidev.cmpackagecouriers.courier.CourierMob;
import com.kreidev.cmpackagecouriers.courier.DeliveryNetworkReg;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class FetchJobFromNetwork {

    public static <T extends Mob & CourierMob> BehaviorControl<T> create() {
        return BehaviorBuilder.create(builder -> builder.group(builder.absent(DeliveryNetworkReg.DELIVERY_JOB_MEM.get()))
                .apply(builder, jobAcc -> (level, mob, gameTime) -> {
                    DeliveryNetwork network = mob.getDeliveryNetwork();
                    if (network == null) return false;
                    DeliveryNetwork.Job job = network.getJob(mob);
                    if (job == null) return false;
                    jobAcc.set(job);
                    return false;
                })
        );
    }
}
