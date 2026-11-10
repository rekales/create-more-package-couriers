package com.kreidev.cmpackagecouriers.courier.entity;

import com.google.common.collect.ImmutableMap;
import com.kreidev.cmpackagecouriers.courier.CourierMob;
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
import java.util.Optional;
import java.util.Set;

// TODO: config feature
@ParametersAreNonnullByDefault
public class FollowPlayerInNetwork <T extends Mob & CourierMob> extends Behavior<T> {

    private Vec3 anchor = Vec3.ZERO;

    public FollowPlayerInNetwork() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ), 400, 600);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T mob) {
        DeliveryNetwork network = mob.getDeliveryNetwork();
        if (network == null) return false;

        // Bypass and just start
        if (network.getFollowingPlayer(mob) != null) return true;

        if (level.getGameTime() % 10 != 2) return false;
        Optional<Player> player = network.getUnfollowedPlayersNearby().stream().findAny();
        if (player.isEmpty()) return false;

        // start logic in here to avoid double fetching DeliveryNetwork#getUnassignedPlayersNearby
        this.anchor = mob.position();
        network.setFollowingPlayer(player.get(), mob);
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, T mob, long gameTime) {
        DeliveryNetwork network = mob.getDeliveryNetwork();
        if (network == null) return false;

        Player following = network.getFollowingPlayer(mob);
        if (following == null || following.isDeadOrDying() || following.isRemoved()) return false;

        Set<Player> players = network.getNearbyPlayers();
        if (!players.contains(following)) return false;

        return following.position().closerThan(this.anchor, 16);
    }

    @Override
    protected void stop(ServerLevel level, T mob, long gameTime) {
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        DeliveryNetwork network = mob.getDeliveryNetwork();
        if (network == null) return;
        network.stopFollowingPlayer(mob);
    }

    @SuppressWarnings("DataFlowIssue")  // Already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, T mob, long gameTime) {
        DeliveryNetwork network = mob.getDeliveryNetwork();
        Player following = network.getFollowingPlayer(mob);
        mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(following, 1.0F, 6));
    }
}
