package com.kreidev.cmpackagecouriers.courier.entity;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.kreidev.cmpackagecouriers.PackageDestination;
import com.kreidev.cmpackagecouriers.courier.CourierMob;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;
import com.kreidev.cmpackagecouriers.courier.CourierNetworkReg;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

public class DeliveryJobBehavior <T extends Mob & CourierMob> extends Behavior<T> {

    private static final int TICKS_BEFORE_RETRY = 100;  // TODO: config?

    private final float speedModifier;
    private final float playerSpeedModifier;

    private int transferTicks = 0;

    public DeliveryJobBehavior(float speed, float playerDeliverySpeed) {
        super(Map.of(
                CourierNetworkReg.DELIVERY_JOB_MEM.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ), 2000);
        this.speedModifier = speed;
        this.playerSpeedModifier = playerDeliverySpeed;
    }

    @SuppressWarnings("unused")
    public DeliveryJobBehavior(float speed) {
        this(speed, speed);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, T mob, long gameTime) {
        DeliveryNetwork network = mob.getDeliveryNetwork();
        if (network == null) return false;

        Optional<DeliveryNetwork.Job> opt = mob.getBrain().getMemory(CourierNetworkReg.DELIVERY_JOB_MEM.get());
        if (opt.isEmpty()) return false;
        DeliveryNetwork.Job job = opt.get();
        if (mob.getPackage().isEmpty() && job.from() == null) return false;

        if (mob.tickCount % 20 == 0 && job.to().getType() == PackageDestination.Type.PLAYER) {
            if (!network.isNearbyNetwork(job.to().getPos())) {
                return mob.position().closerThan(job.to().getPos(), 4);
            }
        }

        return true;
    }

    @Override
    protected void stop(ServerLevel level, T mob, long gameTime) {
        mob.getBrain().eraseMemory(CourierNetworkReg.DELIVERY_JOB_MEM.get());
    }

    @SuppressWarnings("DataFlowIssue")  // Already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, T mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        DeliveryNetwork.Job job = brain.getMemory(CourierNetworkReg.DELIVERY_JOB_MEM.get()).orElseThrow();
        float speed = job.to().getType() == PackageDestination.Type.PLAYER ? this.playerSpeedModifier : this.speedModifier;

        if (mob.getPackage().isEmpty()) {
            BehaviorUtils.setWalkAndLookTargetMemories(mob, job.from(), speed, 1);
            if (mob.position().closerThan(job.from().getCenter(), 1.5F)) {
                // NOTE: update when sources gets expanded
                if (level.getBlockEntity(job.from()) instanceof DepotBlockEntity depot) {
                    ItemStack box = depot.getHeldItem();
                    if (!PackageItem.isPackage(box)) {
                        brain.eraseMemory(CourierNetworkReg.DELIVERY_JOB_MEM.get());
                    }
                    mob.setPackage(box);
                    depot.setHeldItem(ItemStack.EMPTY);
                    depot.notifyUpdate();
                }
            }
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(mob, job.to().getPositionTracker(), speed, 1);
            if (mob.position().closerThan(job.to().getPos(), 1.5F)) {
                if (job.to().tryInsertPackage(level, mob)) {
                    brain.eraseMemory(CourierNetworkReg.DELIVERY_JOB_MEM.get());
                } else {
                    PackageCouriers.LOGGER.debug("failed to transfer: " + this.transferTicks);
                    if (this.transferTicks++ > TICKS_BEFORE_RETRY) {
                        brain.eraseMemory(CourierNetworkReg.DELIVERY_JOB_MEM.get());
                        PackageCouriers.LOGGER.debug("stopped trying");
                    }
                }
            }
        }
    }
}
