package com.kreidev.cmpackagecouriers.nuplane;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.createmod.catnip.codecs.stream.CatnipLargerStreamCodecs;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Use CardboardPlaneManager for adding and removing planes
public class CardboardPlane {

    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceKey<Level>> DIMENSION_STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ResourceKey::location,
            t1 -> ResourceKey.create(Registries.DIMENSION, t1)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CardboardPlane> STREAM_CODEC = CatnipLargerStreamCodecs.composite(
            UUIDUtil.STREAM_CODEC, CardboardPlane::getId,
            CatnipStreamCodecs.VEC3, CardboardPlane::getDeltaMovement,
            CatnipStreamCodecs.VEC3, CardboardPlane::getPos,
            DIMENSION_STREAM_CODEC, CardboardPlane::getCurrentDim,
            DIMENSION_STREAM_CODEC, CardboardPlane::getTargetDim,
            CatnipStreamCodecs.VEC3, CardboardPlane::getTargetPos,
            ItemStack.STREAM_CODEC, CardboardPlane::getPackage,
            CardboardPlane::new
    );

    public static final MapCodec<CardboardPlane> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("ID").forGetter(CardboardPlane::getId),
            Vec3.CODEC.fieldOf("Delta").forGetter(CardboardPlane::getDeltaMovement),
            Vec3.CODEC.fieldOf("Pos").forGetter(CardboardPlane::getPos),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("CurrentDim").forGetter(CardboardPlane::getTargetDim),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("TargetDim").forGetter(CardboardPlane::getTargetDim),
            Vec3.CODEC.fieldOf("TargetPos").forGetter(CardboardPlane::getTargetPos),
            ItemStack.CODEC.fieldOf("Box").forGetter(CardboardPlane::getPackage)
    ).apply(instance, CardboardPlane::new));


    public static final double SPEED = 0.8;

    @NotNull public UUID id;
    @NotNull public Vec3 deltaMovement;
    @NotNull public Vec3 pos;
    @NotNull public ResourceKey<Level> currentDim;
    public int tickCount = 0;

    @Nullable public UUID targetEntityUUID = null;
    @Nullable public Entity targetEntityCached = null;
    @NotNull protected Vec3 targetPos;
    @NotNull protected ResourceKey<Level> targetDim;

    @NotNull public ItemStack box;
    public boolean unpack = false;
    public boolean hasTeleported = false;  // NOTE: For notifying the manager, don't forget to reset

    public CardboardPlane(@NotNull Level currentLevel, @NotNull Entity targetEntity, @NotNull ItemStack box) {
        this.id = UUID.randomUUID();
        this.currentDim = currentLevel.dimension();
        this.targetPos = Vec3.ZERO;  // So warnings can shut it
        this.targetDim = Level.OVERWORLD;  // So warnings can shut it
        this.setTarget(targetEntity);
        this.pos = Vec3.ZERO;
        this.deltaMovement = new Vec3(0,1,0);
        this.setPackage(box);
    }

    public CardboardPlane(@NotNull Level currentLevel, @NotNull Level targetLevel, @NotNull BlockPos targetPos, @NotNull ItemStack box) {
        this.id = UUID.randomUUID();
        this.currentDim = currentLevel.dimension();
        this.targetPos = Vec3.ZERO;  // So warnings can shut it
        this.targetDim = Level.OVERWORLD;  // So warnings can shut it
        this.setTarget(targetLevel, targetPos);
        this.pos = Vec3.ZERO;
        this.deltaMovement = new Vec3(0,1,0);
        this.setPackage(box);
    }

    // For serialization purposes
    public CardboardPlane(@NotNull UUID id, @NotNull Vec3 deltaMovement, @NotNull Vec3 pos, @NotNull ResourceKey<Level> currentDim,
                          @NotNull ResourceKey<Level> targetDim, @NotNull Vec3 targetPos, @NotNull ItemStack box) {
        this.id = id;
        this.deltaMovement = deltaMovement;
        this.pos = pos;
        this.currentDim = currentDim;
        this.targetPos = targetPos;
        this.targetDim = targetDim;
        this.setPackage(box);
    }

    // NOTE: Match sequence with CardboardPlaneEntity::tick else it desyncs
    public void tick(MinecraftServer server) {
        this.tickCount++;

        this.pos = this.pos.add(this.deltaMovement);

        this.updateDelta(server.getLevel(this.currentDim));

        if (this.tickCount > 120 && (!targetPos.closerThan(pos, 80) || this.currentDim != this.targetDim)) {


            this.tpCloserToTarget();
        }
    }

    public boolean hasReachedTarget() {
        return targetPos.closerThan(this.pos, 1.5);
    }

    // Called outside the class
    public void onReachedTarget(MinecraftServer server) {
        ServerLevel level = server.getLevel(this.targetDim);
        if (level == null) return;

        if (targetEntityCached != null  // Assumes entity is cached
                && targetEntityCached instanceof Player player) {
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
            BlockPos blockPos = new BlockPos((int)Math.floor(this.targetPos.x()), (int)Math.floor(this.targetPos.y()), (int)Math.floor(this.targetPos.z()));
            if (level.getBlockState(blockPos).getBlock() instanceof DepotBlock
                    && level.getBlockEntity(blockPos) instanceof DepotBlockEntity depot
                    && depot.getHeldItem().is(Items.AIR)) {
                depot.setHeldItem(this.getPackage());
                depot.notifyUpdate();
                //TODO: add behaviors on belts and shit and check behaviours if exists
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
        } else if (!targetPos.closerThan(this.pos, 80)) {  // Target is far, fly upwards in the general direction
            vecTo = targetPos.subtract(this.pos);
            vecTo = new Vec3(vecTo.x(), vecTo.y() + vecTo.length()/2, vecTo.z()).normalize();
        } else {
            vecTo = targetPos.subtract(this.pos).normalize();
        }

        float augmentedDistance = (float)targetPos.subtract(this.pos).length() + Math.max(0, 80 - this.tickCount);
        float clampedDistance = Mth.clamp(augmentedDistance, 5, 60);
        float curveAmount = Mth.lerp((clampedDistance - 5f) / 55f, 0.4f, 0.06f);
        this.setDeltaMovement(vecFrom.lerp(vecTo, curveAmount).normalize().scale(SPEED));
    }

    public void tpCloserToTarget() {
        PackageCouriers.LOGGER.debug(this.pos.subtract(this.targetPos)+"");
        PackageCouriers.LOGGER.debug(this.pos.subtract(this.targetPos).length()+"");

        Vec3 dirVec = this.pos.subtract(this.targetPos).normalize();

        dirVec = new Vec3(dirVec.x(), 0.5, dirVec.z()).normalize();
        Vec3 tpVec = targetPos.add(dirVec.scale(60));

        this.currentDim = this.targetDim;
        this.pos = tpVec;
        this.setDeltaMovement(this.targetPos.subtract(this.pos).normalize().scale(SPEED));
        this.hasTeleported = true;
    }

    public void setTarget(Entity targetEntity) {
        if (targetEntity != null) {
            this.targetEntityUUID = targetEntity.getUUID();
            this.targetEntityCached = targetEntity;
            this.targetDim = targetEntity.level().dimension();
            this.targetPos = targetEntity.position();
        }
    }

    public void setTarget(@NotNull Level level, @NotNull BlockPos targetBlock) {
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
        return this.id;
    }
    public @NotNull Vec3 getDeltaMovement() {
        return this.deltaMovement;
    }
    public void setDeltaMovement(@NotNull Vec3 deltaMovement) {
        this.deltaMovement = deltaMovement;
    }
    @NotNull public Vec3 getPos() {
        return this.pos;
    }
    public void setPos(@NotNull Vec3 pos) {
        this.pos = pos;
    }
    public @NotNull ResourceKey<Level> getCurrentDim() {
        return this.currentDim;
    }
    public int getTickCount() {
        return this.tickCount;
    }
    public @NotNull Vec3 getTargetPos() {
        return this.targetPos;
    }
    public @NotNull ResourceKey<Level> getTargetDim() {
        return this.targetDim;
    }
    public ItemStack getPackage() {
        return this.box;
    }
    public void setPackage(ItemStack box) {
        if (!PackageItem.isPackage(box)) {
            box = PackageStyles.getDefaultBox();
        }
        this.box = box;
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
