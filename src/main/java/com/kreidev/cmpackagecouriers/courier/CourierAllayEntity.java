package com.kreidev.cmpackagecouriers.courier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;

public class CourierAllayEntity extends Allay {

    public CourierAllayEntity(EntityType<? extends Allay> entityType, Level level) {
        super(entityType, level);

        this.getPersistentData().putBoolean("TrainHat", true);
    }
}
