package com.kreidev.cmpackagecouriers.courier;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DeliveryJobBehavior <T extends Mob & CourierMob & InventoryCarrier> extends Behavior<T> {

    // TODO: put as constructor arg
    public static final float SPEED_MOD = 1.75F;

    public DeliveryJobBehavior() {
        super(Map.of(
                DeliveryNetworkReg.DELIVERY_JOB_MEM.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T owner) {
        // TODO: check if still holding a package. If so, deliver to the nearest default address first.
        return super.checkExtraStartConditions(level, owner);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, T entity, long gameTime) {
        Optional<DeliveryNetwork.Job> job = entity.getBrain().getMemory(DeliveryNetworkReg.DELIVERY_JOB_MEM.get());
        return job.isPresent();
    }

    @Override
    protected void start(ServerLevel level, T entity, long gameTime) {
        Optional<DeliveryNetwork.Job> job = entity.getBrain().getMemory(DeliveryNetworkReg.DELIVERY_JOB_MEM.get());
        if (job.isEmpty()) throw new IllegalStateException();
        ItemStack stack = entity.getInventory().getItem(0);
        if (!stack.isEmpty()) {
            ItemEntity itementity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack);
            level.addFreshEntity(itementity);
            entity.getInventory().setItem(0, ItemStack.EMPTY);
        }
//        BehaviorUtils.setWalkAndLookTargetMemories(entity, job.get().from(), SPEED_MOD, 3);
    }

    @Override
    protected void tick(ServerLevel level, T owner, long gameTime) {
        Brain<?> brain = owner.getBrain();
        DeliveryNetwork.Job job = brain.getMemory(DeliveryNetworkReg.DELIVERY_JOB_MEM.get()).orElseThrow();
        Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);

        ItemStack box = owner.getInventory().getItem(0);
        if (box.isEmpty()) {
            if (walkTarget.isEmpty()) {
//                BehaviorUtils.setWalkAndLookTargetMemories(owner, job.from(), SPEED_MOD, 3);
                return;
            }
            BlockPos targetPos = walkTarget.get().getTarget().currentBlockPosition();
            if (targetPos.getCenter().closerThan(owner.getEyePosition(), 3.0)) {
                if (level.getBlockEntity(targetPos) instanceof DepotBlockEntity be
                        && PackageItem.isPackage(be.getHeldItem())) {
                    owner.getInventory().setItem(0, be.getHeldItem());
                    be.clearContent();
//                    BehaviorUtils.setWalkAndLookTargetMemories(owner, job.to(), SPEED_MOD, 3);
                } else {
                    this.stopJob(owner);
                    return;
                }
            }
        } else {
            if (walkTarget.isEmpty()) {
//                BehaviorUtils.setWalkAndLookTargetMemories(owner, job.to(), SPEED_MOD, 0);
                return;
            }
            BlockPos targetPos = walkTarget.get().getTarget().currentBlockPosition();
            if (targetPos.getCenter().closerThan(owner.getEyePosition(), 3.0)) {
                if (!(level.getBlockEntity(targetPos) instanceof DepotBlockEntity be)) {
                    this.stopJob(owner);
                    return;
                }
                if (be.getHeldItem().isEmpty()) {
                    be.setHeldItem(owner.getInventory().getItem(0));
                    owner.getInventory().setItem(0, ItemStack.EMPTY);
                    this.stopJob(owner);
                }
            }
        }
    }

    protected void stopJob(T owner) {
        owner.getBrain().eraseMemory(DeliveryNetworkReg.DELIVERY_JOB_MEM.get());
    }
}
