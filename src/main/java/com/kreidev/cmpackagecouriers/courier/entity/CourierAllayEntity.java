package com.kreidev.cmpackagecouriers.courier.entity;

import com.kreidev.cmpackagecouriers.PackageDestination;
import com.kreidev.cmpackagecouriers.courier.CourierMob;
import com.kreidev.cmpackagecouriers.courier.WaybillItem;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;
import com.kreidev.cmpackagecouriers.courier.CourierNetworkReg;
import com.mojang.serialization.Dynamic;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.List;

public class CourierAllayEntity extends Allay implements CourierMob {

    private static final String WAYBILL_TAG_KEY = "Waybill";
    private static final EntityDataAccessor<ItemStack> WAYBILL =
            SynchedEntityData.defineId(CourierAllayEntity.class, EntityDataSerializers.ITEM_STACK);

    public CourierAllayEntity(EntityType<? extends CourierAllayEntity> entityType, Level level) {
        super(entityType, level);
        this.getPersistentData().putBoolean("TrainHat", true);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        if (this.getWaybill().isEmpty()) {
            revert(this);
            return;
        }

        DeliveryNetwork network = this.getDeliveryNetwork();
        if (network != null) {
            network.checkIn((ServerLevel) this.level(), this);
        }

        ItemStack stack = this.getItemInHand(InteractionHand.MAIN_HAND);
        if (!PackageItem.isPackage(stack)) {
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            BehaviorUtils.throwItem(this, stack, this.position());
        }
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("courierAllayBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        CourierAllayAi.updateActivity(this);
        this.level().getProfiler().pop();
    }

    @Override
    protected Brain.Provider<Allay> brainProvider() {
        return CourierAllayAi.brainProvider();
    }

    @SuppressWarnings("unchecked")  // Generics jank but I think it works without issues
    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<?> brain = this.brainProvider().makeBrain(dynamic);
        return CourierAllayAi.makeBrain((Brain<CourierAllayEntity>) brain);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        ItemStack box = this.getItemInHand(hand);

        if (stack.isEmpty()) {
            if (!box.isEmpty()) {
                this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                this.level().playSound(player, this, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 2.0F, 1.0F);
                this.swing(InteractionHand.MAIN_HAND);
            } else if (!this.level().isClientSide && revert(this)) {
                player.setItemInHand(hand, this.getWaybill());
            }
            return InteractionResult.SUCCESS;
        } else if (PackageItem.isPackage(stack) && box.isEmpty()) {
            this.setItemInHand(InteractionHand.MAIN_HAND, stack);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            this.level().playSound(player, this, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 2.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        this.spawnAtLocation(this.getWaybill());
    }

    public void setWaybill(ItemStack waybill) {
        if (!waybill.is(CourierNetworkReg.WAYBILL_ITEM.get())) return;
        this.entityData.set(WAYBILL, waybill);
    }

    public ItemStack getWaybill() {
        return this.entityData.get(WAYBILL);
    }

    @Override
    public @Nullable DeliveryNetwork getDeliveryNetwork() {
        return WaybillItem.getAssignedNetwork(this.getWaybill());
    }

    @Override
    public @Nullable DeliveryNetwork.Job getCurrentJob() {
        return this.getBrain().getMemory(CourierNetworkReg.DELIVERY_JOB_MEM.get()).orElse(null);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WAYBILL, ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put(WAYBILL_TAG_KEY, this.getWaybill().saveOptional(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setWaybill(ItemStack.parseOptional(this.registryAccess(), tag.getCompound(WAYBILL_TAG_KEY)));
    }

    public static boolean convert(Allay allay, ItemStack waybill) {
        if (!waybill.is(CourierNetworkReg.WAYBILL_ITEM.get())) return false;
        List<ItemStack> items = allay.getInventory().removeAllItems();
        CourierAllayEntity courier = allay.convertTo(CourierNetworkReg.COURIER_ALLAY_ENTITY.get(), true);
        if (courier == null) return false;
        items.forEach(allay::spawnAtLocation);
        courier.dropEquipment();
        courier.setWaybill(waybill);
        return true;
    }

    public static boolean revert(CourierAllayEntity courier) {
        Allay allay = courier.convertTo(EntityType.ALLAY, true);
        return allay != null;
    }

    public static void tryConvertToCourier(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getTarget() instanceof Allay allay && allay.getType() == EntityType.ALLAY)) return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(CourierNetworkReg.WAYBILL_ITEM.get())) return;
        if (!event.getLevel().isClientSide && convert(allay, stack.copyWithCount(1))) {
            stack.consume(1, event.getEntity());
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @Override
    public Vec3 getPos() {
        return this.position();
    }

    @Override
    public void setPackage(ItemStack box) {
        this.setItemInHand(InteractionHand.MAIN_HAND, box);
    }

    @Override
    public ItemStack getPackage() {
        return this.getItemInHand(InteractionHand.MAIN_HAND);
    }

    @Override
    public @Nullable PackageDestination getDestination() {
        return null;
    }
}
