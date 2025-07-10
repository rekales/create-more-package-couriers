package com.krei.cmpackagecouriers;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

// NOTE: Simple hand thrown planes will require a different entity to simplify implementation
public class DeliveryPlaneProjectile extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData
            .defineId(DeliveryPlaneProjectile.class, EntityDataSerializers.ITEM_STACK);
    @Nullable private UUID targetEntityUUID = null;
    @Nullable protected Entity targetEntityCached = null;
    @Nullable protected Vec3 targetPos = null;
    @Nullable protected ResourceKey<Level> targetPosLevel = null;

    protected double speed = 0.8;
    protected double curveAmount = Math.toRadians(3);  // angle change per tick

    public DeliveryPlaneProjectile(EntityType<? extends DeliveryPlaneProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public DeliveryPlaneProjectile(Level level) {
        super(PackageCouriers.DELIVERY_PLANE_ENTITY.get(), level);
    }

    public DeliveryPlaneProjectile(Level level, ItemStack packageItem) {
        super(PackageCouriers.DELIVERY_PLANE_ENTITY.get(), level);
        this.setPackage(packageItem);
    }

    public DeliveryPlaneProjectile(Level level, BlockPos targetBlock, Level targetLevel) {
        super(PackageCouriers.DELIVERY_PLANE_ENTITY.get(), level);
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
        this.targetEntityCached = null;
        this.targetEntityUUID = null;
    }

    public static DeliveryPlaneProjectile createEmpty(EntityType<? extends DeliveryPlaneProjectile> entityType, Level level) {
        return new DeliveryPlaneProjectile(entityType, level);
    }

    // NOTE: Might require own implementation for noclip and to avoid unnecessary nbt data
    @Override
    public void tick() {
        super.tick();

        if (targetEntityUUID != null && targetEntityCached == null) {
            if (this.level() instanceof ServerLevel serverLevel) {
                targetEntityCached = serverLevel.getEntity(targetEntityUUID);
                PackageCouriers.LOGGER.debug("cachedEntity");
            }
        }

        if (targetEntityCached != null) {
            targetPos = targetEntityCached.position();
            targetPosLevel = targetEntityCached.level().dimension();
        }

        if (targetPos == null)
            return;

        // TODO: Check if in the right dimension

        if (targetPos.closerThan(this.position(), 1)) {
            onReachedTarget();
            remove(RemovalReason.DISCARDED);
            return;
        }

        if (level().isClientSide())
            return;

        if (tickCount < 10)  // fly in a straight line for a bit after launch
            return;

        Vec3 vecFrom = this.getDeltaMovement().normalize();
        Vec3 vecTo = targetPos.subtract(this.position()).normalize();

        this.setDeltaMovement(vecFrom.lerp(vecTo, 0.15).normalize().scale(this.speed));

        // TODO: constant curve instead of lerp
//        // Rotate point towards target
//        Vec3 vecFrom = this.getDeltaMovement().normalize();
//        Vec3 vecTo = targetPos.subtract(this.position()).normalize();
//        double angle = Math.acos(Mth.clamp(vecFrom.dot(vecTo), -1.0, 1.0));
//
//         PackageCouriers.LOGGER.debug(Math.toDegrees(angleBetween(vecFrom, vecTo))+"");
//        PackageCouriers.LOGGER.debug(Math.toDegrees(angle)+"d");
//
//        if (angle < 1e-6 || angle <= this.curveAmount) {   // Already aligned
//            this.setDeltaMovement(vecFrom.scale(this.speed));
//        } else {
//            Vec3 axis = vecFrom.cross(vecTo).normalize();  // Compute axis of rotation (cross product)
//            if (axis.lengthSqr() < 1e-6)   // Vectors are nearly parallel or antiparallel
//                axis = getAnyPerpendicular(vecFrom);
//
//            // Rodrigues' rotation formula
//            Vec3 rotated = vecFrom
//                    .scale(Math.cos(this.curveAmount))
//                    .add(axis.cross(vecFrom).scale(Math.sin(this.curveAmount)))
//                    .add(axis.scale(axis.dot(vecFrom)).scale(1 - Math.cos(this.curveAmount)));
//
//            this.setDeltaMovement(rotated.scale(this.speed));
//        }
        // PackageCouriers.LOGGER.debug(this.getDeltaMovement()+"");
    }

//    // Helper for fallback axis
//    private static Vec3 getAnyPerpendicular(Vec3 v) {
//        return Math.abs(v.x) < 0.9 ? new Vec3(1, 0, 0).cross(v).normalize() : new Vec3(0, 1, 0).cross(v).normalize();
//    }
//
//    public static double angleBetween(Vec3 a, Vec3 b) {
//        double dot = a.dot(b);
//        double lenA = a.length();
//        double lenB = b.length();
//
//        // Clamp to avoid NaN from floating point error
//        double cosTheta = Mth.clamp(dot / (lenA * lenB), -1.0, 1.0);
//
//        return Math.acos(cosTheta); // in radians
//    }

    protected void onReachedTarget() {
        if (targetEntityCached != null  // Assumes entity is cached
                && targetEntityCached instanceof Player player) {
            if (!level().isClientSide()) {
                PackageCouriers.LOGGER.debug(this.getPackage().toString());
                player.getInventory().placeItemBackInInventory(this.getPackage());
            }
            this.level().explode(this, null, AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,
                    this.position().x(), this.position().y(), this.position().z(), 0.1F, false,
                    Level.ExplosionInteraction.NONE, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE,
                    SoundEvents.WIND_CHARGE_BURST);
        } else {
            // Position Target
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Target")) {
            this.targetEntityUUID = compoundTag.getUUID("Target");
        } else if (compoundTag.hasUUID("Position")) {
            // Read position and level instead
        } else {
            // Break entity because invalid
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.targetEntityUUID != null) {
            compoundTag.putUUID("target", this.targetEntityUUID);
        } else {
            // Save position and level instead
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getPackage() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setPackage(ItemStack stack) {
        if (stack.getItem() instanceof PackageItem)
            this.getEntityData().set(DATA_ITEM, stack);
    }

    public void setCurveAmount(double curveAmountRadians) {
        this.curveAmount = curveAmountRadians;
    }

    public void setCurveAmountDegrees(double curveAmountDegrees) {
        this.curveAmount = Math.toRadians(curveAmountDegrees);
    }

    public double getCurveAmount() {
        return curveAmount;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return this.speed;
    }

    public static void init() {}

}
