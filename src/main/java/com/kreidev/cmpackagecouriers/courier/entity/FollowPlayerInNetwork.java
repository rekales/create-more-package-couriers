package com.kreidev.cmpackagecouriers.courier.entity;

import com.google.common.collect.ImmutableMap;
import com.kreidev.cmpackagecouriers.courier.CourierMob;
import com.kreidev.cmpackagecouriers.courier.CourierNetworkReg;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Set;

// TODO: consider making it a "proper" behavior
// TODO: config feature
@ParametersAreNonnullByDefault
public class FollowPlayerInNetwork <T extends Mob & CourierMob> extends Behavior<T> {

    private Vec3 anchor = Vec3.ZERO;

    public FollowPlayerInNetwork(Map<MemoryModuleType<?>, MemoryStatus> entryCondition) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                CourierNetworkReg.PLAYER_SERVING_MEM.get(), MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected void tick(ServerLevel level, T mob, long gameTime) {
        DeliveryNetwork network = mob.getDeliveryNetwork();
        if (network == null) return;

        Player following = mob.getBrain().getMemory(CourierNetworkReg.PLAYER_SERVING_MEM.get()).orElse(null);
        if (following != null) {
            if (following.position().closerThan(this.anchor, 16)) {
                mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(following, 1.0F, 6));
            } else {
                mob.getBrain().eraseMemory(CourierNetworkReg.PLAYER_SERVING_MEM.get());
                mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                return;
            }
        }

        if (gameTime % 20 != 2) return;
        Set<Player> players = network.getUnassignedPlayersNearby();
        if (following != null) {
            if (!players.contains(following)) {
                mob.getBrain().eraseMemory(CourierNetworkReg.PLAYER_SERVING_MEM.get());
                mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            }
        } else {
            if (!players.isEmpty()) {
                mob.getBrain().setMemory(CourierNetworkReg.PLAYER_SERVING_MEM.get(), players.stream().findAny());
                this.anchor = mob.position();
            }
        }
    }
}
