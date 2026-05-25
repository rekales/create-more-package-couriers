package com.kreidev.cmpackagecouriers.courier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;

public class CourierAllayAi {

    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_DEPOSIT_TARGET = 2.25F;
    private static final float SPEED_MULTIPLIER_WHEN_RETRIEVING_ITEM = 1.75F;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.5F;
    private static final int CLOSE_ENOUGH_TO_TARGET = 4;
    private static final int TOO_FAR_FROM_TARGET = 16;
    private static final int MAX_LOOK_DISTANCE = 6;
    private static final int MIN_WAIT_DURATION = 30;
    private static final int MAX_WAIT_DURATION = 60;
    private static final int TIME_TO_FORGET_NOTEBLOCK = 600;
    private static final int DISTANCE_TO_WANTED_ITEM = 32;
    private static final int GIVE_ITEM_TIMEOUT_DURATION = 20;

    protected static Brain<?> makeBrain(Brain<Allay> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Allay> brain) {
        brain.addActivity(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new Swim(0.8F),
                        new AnimalPanic<>(2.5F),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink()
                )
        );
    }

    private static void initIdleActivity(Brain<Allay> brain) {
        brain.addActivityWithConditions(
                Activity.IDLE,
                ImmutableList.of(
//                        Pair.of(0, GoToWantedItem.create(p_218428_ -> true, 1.75F, true, 32)),
//                        Pair.of(1, new GoAndGiveItemsToTarget<>(AllayAi::getItemDepositPosition, 2.25F, 20)),
                        Pair.of(2, StayCloseToTarget.create(CourierAllayAi::getDepotPosition, entity -> true, 4, 8, 1.5F)),
                        Pair.of(3, SetEntityLookTargetSometimes.create(6.0F, UniformInt.of(30, 60))),
                        Pair.of(4, new RunOne<>(ImmutableList.of(
                                        Pair.of(RandomStroll.fly(1.0F), 2),
                                        Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
                                        Pair.of(new DoNothing(30, 60), 1)
                        )))
                ),
                ImmutableSet.of()
        );
    }

    private static Optional<PositionTracker> getDepotPosition(LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<BlockPos> optional = brain.getMemory(CourierAllayReg.TARGET_DEPOT.get());
        return optional.map(BlockPosTracker::new);
    }

//    public static BehaviorControl<LivingEntity> goToPackage(float speed) {
//        return BehaviorBuilder.create()
//    }

}
