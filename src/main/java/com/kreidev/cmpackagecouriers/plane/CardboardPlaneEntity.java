package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.logistics.box.PackageStyles;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

    // NOTE: only relevant in server side
    public CardboardPlane plane;

    public float newDeltaYaw = 0;
    public float oldDeltaYaw = 0;

    public CardboardPlaneEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    public CardboardPlaneEntity(Level level, CardboardPlane plane) {
        super(PackageCouriers.CARDBOARD_PLANE_ENTITY.get(), level);
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
        super(PackageCouriers.CARDBOARD_PLANE_ENTITY.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        this.updateRotation();
        this.setPos(this.position().add(this.getDeltaMovement()));
        this.oldDeltaYaw = this.newDeltaYaw;
        this.newDeltaYaw = this.yRotO-this.getYRot();

        if (!level().isClientSide()) {
            this.setDeltaMovement(plane.getDeltaMovement());

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
        return PackageStyles.getDefaultBox();
    }

    public void setPackage(ItemStack box) {

    }

    public void setSpeed(double speed) {

    }

    public void shootFromRotation(float t1, float t2, float t3, float t4, float t5) {

    }

    public void setTarget(BlockPos pos, Level level) {

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

    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    // For registration purposes
    public static CardboardPlaneEntity createEmpty(EntityType<? extends CardboardPlaneEntity> entityType, Level level) {
        return new CardboardPlaneEntity(entityType, level);
    }

    public static void init() {}
}
