package com.kreidev.cmpackagecouriers.plane;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

// Entity for rendering purposes
// Use CardboardPlaneManager for adding and removing planes
public class CardboardPlaneEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData
            .defineId(CardboardPlaneEntity.class, EntityDataSerializers.ITEM_STACK);

    // NOTE: only relevant in server side
    public CardboardPlane plane;

    public float newDeltaYaw = 0;
    public float oldDeltaYaw = 0;

    public CardboardPlaneEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    public CardboardPlaneEntity(Level level, CardboardPlane plane) {
        super(CardboardPlaneReg.CARDBOARD_PLANE_ENTITY.get(), level);
        this.plane = plane;
        this.setPos(plane.getPos());
        this.setDeltaMovement(plane.getDeltaMovement());

        // updateRotation but no lerp
        Vec3 vec3 = this.getDeltaMovement();
        double d0 = vec3.horizontalDistance();
        this.setXRot((float)(Mth.atan2(vec3.y, d0) * 180.0F / (float)Math.PI));
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
    }

    // For ponder use
    public CardboardPlaneEntity(Level level) {
        super(CardboardPlaneReg.CARDBOARD_PLANE_ENTITY.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        this.updateRotation();
        this.setPos(this.position().add(this.getDeltaMovement()));
        this.oldDeltaYaw = this.newDeltaYaw;
        this.newDeltaYaw = this.yRotO-this.getYRot();

        if (level().isClientSide()) {
            if (this.tickCount%3 == 0) {
                Vec3 lookAngle = this.getLookAngle().scale(0.5);
                this.level().addParticle(
                        ParticleTypes.FIREWORK,
                        this.getX() + lookAngle.x(),
                        this.getY() + lookAngle.y(),
                        this.getZ() + lookAngle.z(),
                        this.random.nextGaussian() * 0.05,
                        -this.getDeltaMovement().y * 0.5,
                        this.random.nextGaussian() * 0.05
                );
            }
        } else {
            this.setDeltaMovement(plane.getDeltaMovement());

            if (!plane.getPackage().equals(this.getPackage())) {
                this.setPackage(plane.getPackage());
            }

            if (tickCount%20 == 1) {  // Synchronize pos just in case
                this.setPos(plane.getPos());
            }
        }
    }

    // Copied from Projectile, so that I don't have to extend that class
    protected void updateRotation() {
        Vec3 vec3 = this.getDeltaMovement();
        double d0 = vec3.horizontalDistance();
        this.setXRot(lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d0) * 180.0F / (float)Math.PI)));
        this.setYRot(lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI)));
    }

    public ItemStack getPackage() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setPackage(ItemStack stack) {
        if (stack.getItem() instanceof PackageItem)
            this.getEntityData().set(DATA_ITEM, stack);
    }

    public void setSpeed(double speed) {

    }

    public void shootFromRotation(float t1, float t2, float t3, float t4, float t5) {

    }

    // TODO: update ponder entity methods

    protected static float lerpRotation(float currentRotation, float targetRotation) {
        while (targetRotation - currentRotation < -180.0F) {
            currentRotation -= 360.0F;
        }
        while (targetRotation - currentRotation >= 180.0F) {
            currentRotation += 360.0F;
        }
        return Mth.lerp(0.2F, currentRotation, targetRotation);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    // For registration purposes
    public static CardboardPlaneEntity createEmpty(EntityType<? extends CardboardPlaneEntity> entityType, Level level) {
        return new CardboardPlaneEntity(entityType, level);
    }
}
