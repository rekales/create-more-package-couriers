package com.kreidev.cmpackagecouriers.courier.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kreidev.cmpackagecouriers.courier.CourierNetworkReg;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class CourierAllayAi {

    private static final float IDLE_MOVE_SPEED = 1.0F;
    private static final float JOB_MOVE_SPEED = 1.75F;
    private static final float PLAYER_DELIVERY_MOVE_SPEED = 2.25F;

    private static final ImmutableList<SensorType<? extends Sensor<? super Mob>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.HURT_BY
    );

    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.PATH,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.IS_PANICKING,
            CourierNetworkReg.DELIVERY_JOB_MEM.get()
    );

    protected static Brain<?> makeBrain(Brain<CourierAllayEntity> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initWorkActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<CourierAllayEntity> brain) {
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

    private static void initIdleActivity(Brain<CourierAllayEntity> brain) {
        /*
            1. if network has job and no current job then get the job
            2. if has job and no package, then go to job.from
            3. if has job and has package, then go to job.to
            4. if has no job but has package, try to create job with null job.from, otherwise drop package.
            5. if has no job and player nearby then go close to player.
                - set anchor point first and if player goes to far from the anchor point, stop.
            6. if has no job and no player nearby, roam within network
         */

        brain.addActivityWithConditions(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, FetchJobFromNetwork.create()),
                        Pair.of(1, new FollowPlayerInNetwork<>()),
                        Pair.of(2, new RunOne<>(ImmutableList.of(
                                        Pair.of(RandomStroll.fly(IDLE_MOVE_SPEED), 2),
                                        Pair.of(SetWalkTargetFromLookTarget.create(IDLE_MOVE_SPEED, 3), 2),
                                        Pair.of(RandomStrollWithinNetwork.create(IDLE_MOVE_SPEED, 4), 1),
                                        Pair.of(new DoNothing(30, 60), 3)
                        )))
                ),
                ImmutableSet.of()
        );
    }

    private static void initWorkActivity(Brain<CourierAllayEntity> brain) {
        brain.addActivityWithConditions(Activity.WORK, ImmutableList.of(
                Pair.of(1, new DeliveryJobBehavior<>(JOB_MOVE_SPEED, PLAYER_DELIVERY_MOVE_SPEED))
        ), ImmutableSet.of(Pair.of(CourierNetworkReg.DELIVERY_JOB_MEM.get(), MemoryStatus.VALUE_PRESENT)));
    }

    static <T extends Mob> Brain.Provider<T> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static void updateActivity(Mob mob) {
        mob.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.WORK, Activity.IDLE));
    }
}
