package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.CourierTarget;
import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.kreidev.cmpackagecouriers.CourierDestination;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

// Use CardboardPlaneManager for adding and removing planes
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CardboardPlane {

    public static final MapCodec<CardboardPlane> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("ID").forGetter(CardboardPlane::getId),
            Vec3.CODEC.fieldOf("Delta").forGetter(CardboardPlane::getDeltaMovement),
            Vec3.CODEC.fieldOf("Pos").forGetter(CardboardPlane::getPos),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("CurrentDim").forGetter(CardboardPlane::getCurrentDim),
            CourierTarget.CODEC.fieldOf("Target").forGetter(CardboardPlane::getTarget),
            ItemStack.CODEC.fieldOf("Box").forGetter(CardboardPlane::getPackage)
    ).apply(instance, CardboardPlane::new));

    public static final double SPEED = 0.8;
    public static final int LIFESPAN_TICKS = 400;

    public UUID id;
    public Vec3 deltaMovement;
    public Vec3 pos;
    public ResourceKey<Level> currentDim;
    public int tickCount = 0;

    public final CourierTarget target;
    public ItemStack box;
    public boolean unpack = false;
    public boolean hasTeleported = false;  // NOTE: For notifying the manager, don't forget to reset
    public boolean forRemoval = false;  // NOTE: For notifying the manager

    public CardboardPlane(Level currentLevel, CourierTarget target, ItemStack box) {
        this.id = UUID.randomUUID();
        this.currentDim = currentLevel.dimension();
        this.target = target;
        this.pos = Vec3.ZERO;
        this.deltaMovement = new Vec3(0,1,0);
        this.setPackage(box);
    }

    // For serialization purposes
    public CardboardPlane(UUID id, Vec3 deltaMovement, Vec3 pos, ResourceKey<Level> currentDim,
                          CourierTarget target, ItemStack box) {
        this.id = id;
        this.deltaMovement = deltaMovement;
        this.pos = pos;
        this.currentDim = currentDim;
        this.target = target;
        this.setPackage(box);
    }

    // NOTE: Match sequence with CardboardPlaneEntity::tick else it desyncs
    public void tick(MinecraftServer server) {
        this.tickCount++;

        this.pos = this.pos.add(this.deltaMovement);

        this.updateDelta();

        if (this.tickCount > 120 && (!target.getPos().closerThan(pos, 80) || this.currentDim != target.getDim())) {
            this.tpCloserToTarget();
        }

        if (this.hasReachedTarget() || this.getTickCount() > LIFESPAN_TICKS) {
            this.onReachedTarget(server);
            this.forRemoval = true;
        }
    }

    public boolean hasReachedTarget() {
        return target.getPos().closerThan(this.pos, 1.5);
    }

    // NOTE: Potential issue, what happens if the destination is not loaded?
    public void onReachedTarget(MinecraftServer server) {
        ServerLevel level = server.getLevel(this.target.getDim());
        if (level == null) return;

        if (target.getEntity() instanceof Player player) {
            if (unpack) {
                ItemStackHandler stacks = PackageItem.getContents(this.getPackage());
                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    ItemStack stack = stacks.getStackInSlot(slot);
                    if (stack.getItem() instanceof UnpackEffects) ((UnpackEffects) stack.getItem()).unpack(level, stack);
                    player.getInventory().placeItemBackInInventory(stack);
                }
            } else {
                player.getInventory().placeItemBackInInventory(this.getPackage());
            }
        } else {
            BlockPos blockPos = new BlockPos((int)Math.floor(target.getPos().x()), (int)Math.floor(target.getPos().y()), (int)Math.floor(target.getPos().z()));
            if (level.getBlockState(blockPos).getBlock() instanceof CourierDestination dest
                    && dest.cmpc$hasSpace(level, blockPos)) {
                dest.cmpc$onReachedDestination(level, blockPos, this);
            } else {
                level.addFreshEntity(PackageEntity.fromItemStack(level, this.pos, this.getPackage()));
            }
        }

        ParticleOptions particleOption = new ItemParticleOption(ParticleTypes.ITEM, AllItems.CARDBOARD.asStack());
        level.sendParticles(
                particleOption,
                this.pos.x(), this.pos.y(), this.pos.z(),
                20,
                0.3, 0.3, 0.3,
                0.1
        );

        level.playSound(
                null,
                this.pos.x(), this.pos.y(), this.pos.z(),
                SoundEvents.WIND_CHARGE_BURST.value(),
                SoundSource.NEUTRAL,
                1.0F,
                0.75F
        );
    }

    private void updateDelta() {
        Vec3 vecFrom = this.getDeltaMovement().normalize();
        Vec3 vecTo;
        if (this.currentDim != target.getDim()) {  // Target not in the same dimension
            vecTo = this.getDeltaMovement().normalize();
        } else {
            double xzDistance = Math.sqrt(Math.pow(target.getPos().x-this.pos.x, 2) + Math.pow(target.getPos().z-this.pos.z, 2));
            Vec3 adjustedTargetPos = new Vec3(
                    target.getPos().x,
                    target.getPos().y + (xzDistance > 2 ? xzDistance/4 : 0),
                    target.getPos().z
            );
            vecTo = adjustedTargetPos.subtract(this.pos).normalize();
        }

        float augmentedDistance = (float)target.getPos().subtract(this.pos).length() + Math.max(0, 70 - this.tickCount);
        float clampedDistance = Mth.clamp(augmentedDistance, 5, 60);
        float curveAmount = Mth.lerp((clampedDistance - 5f) / 55f, 0.4f, 0.06f);
        this.setDeltaMovement(vecFrom.lerp(vecTo, curveAmount).normalize().scale(SPEED));
    }

    public void tpCloserToTarget() {
        PackageCouriers.LOGGER.debug("{}", this.pos.subtract(this.target.getPos()));
        PackageCouriers.LOGGER.debug("{}", this.pos.subtract(this.target.getPos()).length());

        Vec3 dirVec = this.pos.subtract(this.target.getPos()).normalize();

        dirVec = new Vec3(dirVec.x(), 0.5, dirVec.z()).normalize();
        Vec3 tpVec = target.getPos().add(dirVec.scale(60));

        this.currentDim = target.getDim();
        this.pos = tpVec;
        this.setDeltaMovement(target.getPos().subtract(this.pos).normalize().scale(SPEED));
        this.hasTeleported = true;
    }

    public void setRot(float xRot, float yRot) {
        this.deltaMovement = getDeltaMovementFromRotation(xRot, yRot);
    }


    public UUID getId() {
        return this.id;
    }
    public Vec3 getDeltaMovement() {
        return this.deltaMovement;
    }
    public void setDeltaMovement(Vec3 deltaMovement) {
        this.deltaMovement = deltaMovement;
    }
    public Vec3 getPos() {
        return this.pos;
    }
    public void setPos(Vec3 pos) {
        this.pos = pos;
    }
    public ResourceKey<Level> getCurrentDim() {
        return this.currentDim;
    }
    public int getTickCount() {
        return this.tickCount;
    }
    public CourierTarget getTarget() {
        return this.target;
    }
    public void setUnpack(boolean unpack) {
        this.unpack = unpack;
    }
    public boolean hasTeleported() {
        return this.hasTeleported;
    }
    public void setTeleported(boolean teleported) {
        this.hasTeleported = teleported;
    }
    public boolean isForRemoval() {return this.forRemoval;}
    public ItemStack getPackage() {
        return this.box;
    }
    public void setPackage(ItemStack box) {
        if (!PackageItem.isPackage(box)) {
            box = PackageStyles.getDefaultBox();
        }
        this.box = box;
    }

    @Override
    public String toString() {
        return "CardboardPlane{" +
                "id=" + id +
                ", pos=" + pos +
                ", tickCount=" + tickCount +
                '}';
    }

    public static Vec3 getDeltaMovementFromRotation(float xRot, float yRot) {
        float xRotRad = xRot * ((float)Math.PI / 180F);
        float yRotRad = yRot * ((float)Math.PI / 180F);

        float cosXRot = Mth.cos(xRotRad);
        float sinXRot = Mth.sin(xRotRad);
        float cosYRot = Mth.cos(yRotRad);
        float sinYRot = Mth.sin(yRotRad);

        return new Vec3(
                -sinYRot * cosXRot,
                -sinXRot,
                cosYRot * cosXRot
        );
    }
}
