package com.krei.cmpackagecouriers;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

// NOTE: Simple hand thrown planes will require a different entity to simplify implementation
public class DeliveryPlaneProjectile extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData
            .defineId(DeliveryPlaneProjectile.class, EntityDataSerializers.ITEM_STACK);
    @Nullable private UUID targetEntityUUID = null;
    @Nullable protected Entity targetEntityCached = null;
    @Nullable protected Vec3 targetPos = null;
    @Nullable protected ResourceKey<Level> targetPosLevel = null;

    public float newDeltaYaw = 0;
    public float oldDeltaYaw = 0;

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

        this.oldDeltaYaw = this.newDeltaYaw;
        this.newDeltaYaw = this.yRotO-this.getYRot();

        if (this.level().isClientSide() && this.tickCount%3 == 0) {
            Vec3 lookAngle = this.getLookAngle().scale(0.5);
            this.level()
                    .addParticle(
                            ParticleTypes.FIREWORK,
                            this.getX() + lookAngle.x(),
                            this.getY() + lookAngle.y(),
                            this.getZ() + lookAngle.z(),
                            this.random.nextGaussian() * 0.05,
                            -this.getDeltaMovement().y * 0.5,
                            this.random.nextGaussian() * 0.05
                    );
        }

        if (targetEntityUUID != null && targetEntityCached == null) {
            if (this.level() instanceof ServerLevel serverLevel) {
                targetEntityCached = serverLevel.getEntity(targetEntityUUID);
                PackageCouriers.LOGGER.debug("cachedEntity");
            }
        }

        if (targetEntityCached != null) {
            if (targetEntityCached instanceof LivingEntity)
                targetPos = targetEntityCached.getEyePosition();
            else
                targetPos = targetEntityCached.position();
            targetPosLevel = targetEntityCached.level().dimension();
        }

        if (targetPos == null)
            return;

        // TODO: Check if in the right dimension
        if (targetPos.closerThan(this.position(), 1.5)) {
            onReachedTarget();
            remove(RemovalReason.DISCARDED);
            return;
        }

        if (targetEntityCached != null
                && targetEntityCached instanceof Player player
                && targetPos.closerThan(this.position(), 40)
                && !targetPos.closerThan(this.position(), 30)) {
            player.displayClientMessage(Component.literal("Package Inbound"), true);
        }

        if (level().isClientSide())  // Don't do unnecessary flight calculations on clientside
            return;

        if (tickCount < 10)  // fly in a straight line for a bit after launch
            return;

        Vec3 vecFrom = this.getDeltaMovement().normalize();
        Vec3 vecTo;

        if (targetPosLevel != level().dimension()) {  // Target not in the same dimension
            vecTo = this.getDeltaMovement().normalize();
        } else if (!targetPos.closerThan(this.position(), 80)) {  // Target is far, fly upwards in the general direction
            vecTo = targetPos.subtract(this.position());
            vecTo = new Vec3(vecTo.x(), vecTo.y() + vecTo.length()/2, vecTo.z()).normalize();
        } else {
            vecTo = targetPos.subtract(this.position()).normalize();
        }

        float augmentedDistance = (float)targetPos.subtract(this.position()).length() + Math.max(0, 60 - this.tickCount);
        float clampedDistance = Mth.clamp(augmentedDistance, 5, 60);
        float curveAmount = Mth.lerp((clampedDistance - 5f) / 55f, 0.35f, 0.06f);

        this.setDeltaMovement(vecFrom.lerp(vecTo, curveAmount).normalize().scale(this.speed));
//        this.setDeltaMovement(vecFrom.normalize().scale(this.speed));

        Vec3 posAhead = this.position().add(this.getDeltaMovement().normalize().scale(20));

        if (!isChunkTicking(level(), posAhead)
            || this.tickCount > 120) {

            if (level() instanceof ServerLevel serverLevel
                    && this.targetPosLevel != null) {
                ServerLevel tpLevel = serverLevel.getServer().getLevel(this.targetPosLevel);

                if (!targetPos.closerThan(this.position(), 80) || targetPosLevel != level().dimension()) {
                    Vec3 dirVec = this.position().subtract(targetPos);
                    dirVec = new Vec3(dirVec.x(), 0, dirVec.z()).normalize();
                    dirVec = new Vec3(dirVec.x(), 0.5, dirVec.z()).normalize();
                    Vec3 tpVec;
                    if (isChunkTicking(tpLevel, targetPos.add(dirVec.scale(70)))) {
                        tpVec = targetPos.add(dirVec.scale(70));
                    } else if (isChunkTicking(tpLevel, targetPos.add(dirVec.scale(50)))) {
                        tpVec = targetPos.add(dirVec.scale(50));
                    } else if (isChunkTicking(tpLevel, targetPos.add(dirVec.scale(30)))) {
                        tpVec = targetPos.add(dirVec.scale(30));
                    } else if (isChunkTicking(tpLevel, targetPos.add(dirVec.scale(10)))) {
                        tpVec = targetPos.add(dirVec.scale(10));
                    } else if (isChunkTicking(tpLevel, targetPos)) {
                        tpVec = targetPos;
                    } else {
                        PackageCouriers.LOGGER.debug("Target Not Loaded");
                        remove(RemovalReason.DISCARDED);
                        return;
                    }

                    if (targetPosLevel != level().dimension()) {  // Target not in the same dimension
                        // TODO: Maybe set the proper rotations?
                        teleportTo(tpLevel, tpVec.x(), tpVec.y(), tpVec.z(), Collections.emptySet(), this.getYRot(), this.getXRot());
                        PackageCouriers.LOGGER.debug("TP: " + tpLevel + " " + tpVec);
//                        PackageCouriers.LOGGER.debug("YD: " + (this.position().y() - targetPos.y()));
                    } else {
                        teleportTo(tpVec.x(), tpVec.y(), tpVec.z());
                        PackageCouriers.LOGGER.debug("TP: " + tpVec);
//                        PackageCouriers.LOGGER.debug("YD: " + (this.position().y() - targetPos.y()));

                    }
                        this.setDeltaMovement(targetPos.subtract(this.position()).normalize().scale(this.speed));
                }

            }
        }

        if (this.tickCount > 400) {
            // Timeout, teleport the plane directly to target
        }

        if (tickCount%5 == 0) {
            PackageCouriers.LOGGER.debug(curveAmount+"");
        }
    }

    protected void onReachedTarget() {
        if (targetEntityCached != null  // Assumes entity is cached
                && targetEntityCached instanceof Player player) {
            if (!level().isClientSide()) {
                player.getInventory().placeItemBackInInventory(this.getPackage());
            }
        } else if (targetPos != null) {
            if (!level().isClientSide()) {
                BlockPos blockPos = new BlockPos((int)Math.floor(this.targetPos.x()), (int)Math.floor(this.targetPos.y()), (int)Math.floor(this.targetPos.z()));
                if (level().getBlockState(blockPos).getBlock() instanceof DepotBlock
                        && level().getBlockEntity(blockPos) instanceof DepotBlockEntity depot
                        && depot.getHeldItem().is(Items.AIR)) {
                    depot.setHeldItem(this.getPackage());
                    depot.notifyUpdate();
                    //TODO: Belts and hoppers as targets
                } else {
                    level().addFreshEntity(PackageEntity.fromItemStack(level(), this.position(), this.getPackage()));
                }
            }
        }

        this.level().explode(this, null, AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,
                this.position().x(), this.position().y(), this.position().z(), 0.01F, false,
                Level.ExplosionInteraction.NONE, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE,
                SoundEvents.WIND_CHARGE_BURST);
    }

    public void shootFromRotation(float x, float y, float z, float velocity, float inaccuracy) {
        float f = -Mth.sin(y * (float) (Math.PI / 180.0)) * Mth.cos(x * (float) (Math.PI / 180.0));
        float f1 = -Mth.sin((x + z) * (float) (Math.PI / 180.0));
        float f2 = Mth.cos(y * (float) (Math.PI / 180.0)) * Mth.cos(x * (float) (Math.PI / 180.0));
        this.shoot(f, f1, f2, velocity, inaccuracy);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return PackageCouriers.CARDBOARD_PLANE_ITEM.asStack();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        ItemStack box = ItemStack.parseOptional(level().registryAccess(), compoundTag.getCompound("Box"));
        this.setPackage(box);

        if (compoundTag.hasUUID("TargetEntity")) {
            targetEntityUUID = compoundTag.getUUID("TargetEntity");
        } else if (compoundTag.contains("TargetPosX")) {
            double x = compoundTag.getDouble("TargetPosX");
            double y = compoundTag.getDouble("TargetPosY");
            double z = compoundTag.getDouble("TargetPosZ");
            targetPos = new Vec3(x, y, z);
        } else {
            // Illegal state
        }

        refreshDimensions();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        ItemStack box = this.getPackage();
        compoundTag.put("Box", box.saveOptional(level().registryAccess()));

        if (targetEntityUUID != null) {
            compoundTag.putUUID("TargetEntity", targetEntityUUID);
        } else if (targetPos != null){
            compoundTag.putDouble("TargetPosX", targetPos.x());
            compoundTag.putDouble("TargetPosY", targetPos.y());
            compoundTag.putDouble("TargetPosZ", targetPos.z());
        } else {
            // Illegal State
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

    public static boolean isChunkTicking(Level level, Vec3 pos) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos blockPos = new BlockPos((int) pos.x(),(int)  pos.y(),(int)  pos.z());
            return serverLevel.getChunkSource().chunkMap.getDistanceManager()
                    .inEntityTickingRange(ChunkPos.asLong(blockPos));
        }
        return false;
    }

    public static void init() {}

}
