package com.krei.cmpackagecouriers;

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

import javax.annotation.Nullable;
import java.util.UUID;

// Maybe create a middle abstract class called HomingEntity
public class DeliveryPlaneEntity extends Entity {
    @Nullable
    private UUID targetEntityUUID;
    @Nullable
    protected Entity targetEntityCached = null;
    protected Vec3 targetPos = Vec3.ZERO;
    protected ResourceKey<Level> targetPosLevel = Level.OVERWORLD;

    // unknown if there's already fields for these
    protected float speed = 0;
//    protected Vec3 direction = Vec3.ZERO;

    public DeliveryPlaneEntity(EntityType<? extends DeliveryPlaneEntity> entityType, Level level) {
        super(entityType, level);
    }

    // Maybe temp until I figure out how to make constructors properly
    public static DeliveryPlaneEntity createEmpty(EntityType<? extends DeliveryPlaneEntity> type, Level level) {
        return new DeliveryPlaneEntity(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (targetEntityCached != null) {
            targetPos = targetEntityCached.position();
            targetPosLevel = targetEntityCached.level().dimension();
        } else if (targetEntityUUID != null) {
            targetEntityCached = ((ServerLevel)this.level()).getEntity(targetEntityUUID);
            if (targetEntityCached != null) {
                targetPos = targetEntityCached.position();
                targetPosLevel = targetEntityCached.level().dimension();
            } else
                targetEntityUUID = null;
        }


        if (targetEntityCached == null && targetEntityUUID == null && !level().players().isEmpty()) {
            for (Player p : level().players()) {
                setTarget(p);
                PackageCouriers.LOGGER.debug("Added player: " + p.toString());
                break;
            }
        }


//        PackageCouriers.LOGGER.debug(this.getYRot()+"");
//        PackageCouriers.LOGGER.debug(this.getXRot()+"");
        this.lookAt(targetPos);
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

    public void lookAt(Vec3 targetPos) {
        Vec3 direction = targetPos.subtract(this.position());

        double dx = direction.x;
        double dy = direction.y;
        double dz = direction.z;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float)(Math.atan2(dz, dx) * (180F / Math.PI)) - 90F;
        float pitch = (float)(-(Math.atan2(dy, horizontalDist) * (180F / Math.PI)));

        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public static DeliveryPlaneEntity summonWithData(ServerLevel level, BlockPos pos, Player target) {
        DeliveryPlaneEntity entity = new DeliveryPlaneEntity(PackageCouriers.DELIVERY_PLANE_ENTITY.get(), level);
        entity.setTarget(target);
        entity.setPos(Vec3.atCenterOf(pos));
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Target")) {
            this.targetEntityUUID = compoundTag.getUUID("Target");
            this.targetEntityCached = ((ServerLevel)this.level()).getEntity(targetEntityUUID);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.targetEntityUUID != null) {
            compoundTag.putUUID("target", this.targetEntityUUID);
        }
    }
}
