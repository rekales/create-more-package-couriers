package com.krei.cmpackagecouriers.plane;

import com.krei.cmpackagecouriers.PackageCouriers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

// Maybe create a middle abstract class called HomingEntity
public class DeliveryPlaneEntity extends Entity {

    @Nullable private UUID targetEntityUUID;
    @Nullable protected Entity targetEntityCached = null;
    @Nonnull protected Vec3 targetPos;
    @Nonnull protected ResourceKey<Level> targetPosLevel;

    protected float speed = 0;
    protected float turnSpeed = 0; // 1 is instant

    public DeliveryPlaneEntity(EntityType<? extends DeliveryPlaneEntity> entityType, Level level, Entity targetEntity) {
        super(entityType, level);
        this.setTarget(targetEntity);
        this.targetPos = this.targetEntityCached.position();
        this.targetPosLevel = this.targetEntityCached.level().dimension();
    }

    public DeliveryPlaneEntity(EntityType<? extends DeliveryPlaneEntity> entityType, Level level, BlockPos targetBlock, Level targetLevel) {
        super(entityType, level);
        this.targetPos = Vec3.atCenterOf(targetBlock);
        this.targetPosLevel = targetLevel.dimension();
    }

    public void setTarget(@Nullable Entity targetEntity) {
        if (targetEntity != null) {
            this.targetEntityUUID = targetEntity.getUUID();
            this.targetEntityCached = targetEntity;
        }
    }

    public void setTarget(BlockPos targetBlock, Level level) {
        this.targetPos = Vec3.atCenterOf(targetBlock);
        this.targetPosLevel = level.dimension();
    }

    @Nullable
    public Entity getTargetEntity() {
        if (this.targetEntityCached != null) {
            return this.targetEntityCached;
        } else if (this.targetEntityUUID != null && this.level() instanceof ServerLevel serverLevel) {
            this.targetEntityCached = serverLevel.getEntity(this.targetEntityUUID);
            return this.targetEntityCached;
        }
        return null;
    }

    public Vec3 getTargetPosition() {
        return targetPos;
    }

    public ResourceKey<Level> getTargetLevel() {
        return targetPosLevel;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Target")) {
            this.targetEntityUUID = compoundTag.getUUID("Target");
            this.targetEntityCached = ((ServerLevel)this.level()).getEntity(targetEntityUUID);
        } else if (compoundTag.hasUUID("Position")) {
            // Read position and level instead
        } else {
            // Break entity because invalid
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.targetEntityUUID != null) {
            compoundTag.putUUID("target", this.targetEntityUUID);
        } else {
            // Save position and level instead
        }
    }

    // Is this actually necessary?
    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof DeliveryPlaneEntity deliveryPlaneEntity) {
            this.targetEntityCached = deliveryPlaneEntity.targetEntityCached;
            this.targetEntityUUID = deliveryPlaneEntity.targetEntityUUID;
        }
    }


    // Maybe temp until I figure out how to make constructors properly
    public static DeliveryPlaneEntity createEmpty(EntityType<? extends DeliveryPlaneEntity> type, Level level) {
        for (Player p : level.players()) {
            PackageCouriers.LOGGER.debug("Added player: {}", p.toString());
            return new DeliveryPlaneEntity(type, level, p);
        }
        throw new NullPointerException("Can't find players to target");
    }

    @Override
    public void tick() {
        super.tick();
        Entity targetEntity = this.getTargetEntity();
        if (targetEntity != null) {
            targetPos = targetEntity.position();
            targetPosLevel = targetEntity.level().dimension();
        }

        if (tickCount%20 == 0) {
            PackageCouriers.LOGGER.debug(this.getYRot()+"");
            PackageCouriers.LOGGER.debug(this.getXRot()+"");
        }
        this.lookAt(targetPos);
    }

    public void lookAt(Vec3 targetPos) {
        Vec3 direction = targetPos.subtract(this.position());

        double dx = direction.x;
        double dy = direction.y;
        double dz = direction.z;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float)(Math.atan2(dz, dx) * (180F / Math.PI)) - 90F;
        float pitch = (float)(-(Math.atan2(dy, horizontalDist) * (180F / Math.PI)));

//        this.setYRot(Mth.lerp(0.1f, this.getYRot(), yaw));
//        this.setXRot(Mth.lerp(0.1f, this.getXRot(), pitch));
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
}
