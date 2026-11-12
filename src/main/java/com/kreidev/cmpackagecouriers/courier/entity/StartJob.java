package com.kreidev.cmpackagecouriers.courier.entity;

import com.kreidev.cmpackagecouriers.PackageDestination;
import com.kreidev.cmpackagecouriers.courier.CourierMob;
import com.kreidev.cmpackagecouriers.courier.CourierNetworkReg;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.item.ItemStack;

public class StartJob {

    public static <T extends Mob & CourierMob> BehaviorControl<T> fromNetwork() {
        return BehaviorBuilder.create(builder -> builder.group(builder.absent(CourierNetworkReg.DELIVERY_JOB_MEM.get()))
                .apply(builder, jobAcc -> (level, mob, gameTime) -> {
                    DeliveryNetwork network = mob.getDeliveryNetwork();
                    if (network == null) return false;
                    DeliveryNetwork.Job job = network.getJob(mob);
                    if (job == null) return false;
                    jobAcc.set(job);
                    return true;
                })
        );
    }

    public static <T extends Mob & CourierMob> BehaviorControl<T> fromHeldPackage() {
        return BehaviorBuilder.create(builder -> builder.group(builder.absent(CourierNetworkReg.DELIVERY_JOB_MEM.get()))
                .apply(builder, jobAcc -> (level, mob, gameTime) -> {
                    DeliveryNetwork network = mob.getDeliveryNetwork();
                    if (network == null) return false;
                    ItemStack box = mob.getPackage();
                    if (box.isEmpty()) return false;
                    PackageDestination dest = network.findDestination(level, box);
                    if (dest == null) {
                        mob.dropPackage(level);
                    } else {
                        jobAcc.set(new DeliveryNetwork.Job(null, dest));
                    }
                    return true;
                })
        );
    }
}
