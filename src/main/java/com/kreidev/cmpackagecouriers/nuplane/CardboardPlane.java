package com.kreidev.cmpackagecouriers.nuplane;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.createmod.catnip.codecs.stream.CatnipLargerStreamCodecs;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CardboardPlane {

    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceKey<Level>> DIMENSION_STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ResourceKey::location,
            t1 -> ResourceKey.create(Registries.DIMENSION, t1)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CardboardPlane> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CardboardPlane::getId,
            CatnipStreamCodecs.VEC3, CardboardPlane::getDeltaMovement,
            CatnipStreamCodecs.VEC3, CardboardPlane::getPos,
            DIMENSION_STREAM_CODEC, CardboardPlane::getTargetDim,
            CatnipStreamCodecs.VEC3, CardboardPlane::getTargetPos,
            CardboardPlane::new
    );

    public static final MapCodec<CardboardPlane> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("ID").forGetter(CardboardPlane::getId),
            Vec3.CODEC.fieldOf("Delta").forGetter(CardboardPlane::getDeltaMovement),
            Vec3.CODEC.fieldOf("Pos").forGetter(CardboardPlane::getPos),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("DeltaMovement").forGetter(CardboardPlane::getTargetDim),
            Vec3.CODEC.fieldOf("TargetPos").forGetter(CardboardPlane::getTargetPos)
    ).apply(instance, CardboardPlane::new));


    public static final double SPEED = 0.8;

    @NotNull public UUID id;
    @NotNull public Vec3 deltaMovement;
    @NotNull public Vec3 pos;
    public int tickCount = 0;

    @Nullable public UUID targetEntityUUID = null;
    @Nullable public Entity targetEntityCached = null;
    @NotNull protected Vec3 targetPos;
    @NotNull protected ResourceKey<Level> targetDim;

    public CardboardPlane(Entity targetEntity) {
        this.id = UUID.randomUUID();
        this.targetPos = Vec3.ZERO;  // So warnings can shut it
        this.targetDim = Level.OVERWORLD;  // So warnings can shut it
        this.setTarget(targetEntity);
        this.pos = Vec3.ZERO;
        this.deltaMovement = new Vec3(0,1,0);
    }

    public CardboardPlane(Level targetLevel, BlockPos targetPos) {
        this.id = UUID.randomUUID();
        this.targetPos = Vec3.ZERO;  // So warnings can shut it
        this.targetDim = Level.OVERWORLD;  // So warnings can shut it
        this.setTarget(targetLevel, targetPos);
        this.pos = Vec3.ZERO;
        this.deltaMovement = new Vec3(0,1,0);
    }

    // For serialization purposes
    public CardboardPlane(@NotNull UUID id, @NotNull Vec3 deltaMovement, @NotNull Vec3 pos,
                          @NotNull ResourceKey<Level> targetDim, @NotNull Vec3 targetPos) {
        this.id = id;
        this.deltaMovement = deltaMovement;
        this.pos = pos;
        this.targetPos = targetPos;
        this.targetDim = targetDim;
    }

    // NOTE: Match sequence with CardboardPlaneEntity::tick else it desyncs
    public void tick(MinecraftServer server) {
        this.tickCount++;

        this.pos = this.pos.add(this.deltaMovement);

        this.updateDelta(server.getLevel(this.targetDim));
    }

    private void updateDelta(ServerLevel level) {
        if (this.targetEntityUUID != null) {
            if (this.targetEntityCached == null) {
                this.targetEntityCached = level.getEntity(targetEntityUUID);
            }

            if (this.targetEntityCached instanceof LivingEntity) {
                this.targetPos = this.targetEntityCached.getEyePosition();
            } else if (targetEntityCached != null) {
                this.targetPos = this.targetEntityCached.position();
            }
        }

        Vec3 vecFrom = this.getDeltaMovement().normalize();
        Vec3 vecTo;
        if (this.targetDim != level.dimension()) {  // Target not in the same dimension
            vecTo = this.getDeltaMovement().normalize();
        } else if (!targetPos.closerThan(this.getPos(), 80)) {  // Target is far, fly upwards in the general direction
            vecTo = targetPos.subtract(this.getPos());
            vecTo = new Vec3(vecTo.x(), vecTo.y() + vecTo.length()/2, vecTo.z()).normalize();
        } else {
            vecTo = targetPos.subtract(this.getPos()).normalize();
        }

        float augmentedDistance = (float)targetPos.subtract(this.getPos()).length() + Math.max(0, 80 - this.tickCount);
        float clampedDistance = Mth.clamp(augmentedDistance, 5, 60);
        float curveAmount = Mth.lerp((clampedDistance - 5f) / 55f, 0.4f, 0.06f);
        this.setDeltaMovement(vecFrom.lerp(vecTo, curveAmount).normalize().scale(SPEED));
    }



    public void setTarget(Entity targetEntity) {
        if (targetEntity != null) {
            this.targetEntityUUID = targetEntity.getUUID();
            this.targetEntityCached = targetEntity;
            this.targetDim = targetEntity.level().dimension();
            this.targetPos = targetEntity.position();
        }
    }

    public void setTarget(Level level, BlockPos targetBlock) {
        this.targetPos = Vec3.atCenterOf(targetBlock);
        this.targetDim = level.dimension();
        this.targetEntityCached = null;
        this.targetEntityUUID = null;
    }

    public void setRot(Vec3 delta) {
        this.deltaMovement = delta.normalize().scale(SPEED);
    }

    public void setRot(float xRot, float yRot) {
        this.deltaMovement = getDeltaMovementFromRotation(xRot, yRot);
    }


    public @NotNull UUID getId() {
        return id;
    }

    public @NotNull Vec3 getDeltaMovement() {
        return this.deltaMovement;
    }

    public void setDeltaMovement(@NotNull Vec3 deltaMovement) {
        this.deltaMovement = deltaMovement;
    }

    @NotNull public Vec3 getPos() {
        return pos;
    }

    public void setPos(@NotNull Vec3 pos) {
        this.pos = pos;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public @NotNull Vec3 getTargetPos() {
        return targetPos;
    }

    public @NotNull ResourceKey<Level> getTargetDim() {
        return targetDim;
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
