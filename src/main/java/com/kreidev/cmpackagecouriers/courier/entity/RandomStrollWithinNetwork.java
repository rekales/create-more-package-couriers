package com.kreidev.cmpackagecouriers.courier.entity;

import com.kreidev.cmpackagecouriers.courier.CourierMob;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.List;

public class RandomStrollWithinNetwork {

    public static <T extends Mob & CourierMob> BehaviorControl<T> create(float speedModifier, int closeEnoughDist) {
        return BehaviorBuilder.create(builder -> builder.group(builder.absent(MemoryModuleType.WALK_TARGET))
                .apply(builder, walkAcc -> (level, mob, gameTime) -> {
                    DeliveryNetwork network = mob.getDeliveryNetwork();
                    if (network == null) return false;

                    List<BlockPos> nodes = network.getAllNodes();
                    if (nodes.isEmpty()) return false;

                    BlockPos node = nodes.get(mob.getRandom().nextInt(nodes.size()));
                    for (int i = 0 ; i < 5; i++) {
                        if (mob.position().closerThan(node.getCenter(), 48)) break;
                        node = nodes.get(mob.getRandom().nextInt(nodes.size()));
                    }
                    walkAcc.setWithExpiry(
                            new WalkTarget(node, speedModifier, closeEnoughDist),
                            (int) (30 * mob.position().distanceTo(node.getCenter()))
                    );
                    return true;
                })
        );
    }
}