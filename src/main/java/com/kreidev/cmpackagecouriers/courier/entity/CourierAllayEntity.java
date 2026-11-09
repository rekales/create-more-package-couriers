package com.kreidev.cmpackagecouriers.courier.entity;

import com.kreidev.cmpackagecouriers.courier.CourierMob;
import com.kreidev.cmpackagecouriers.courier.WaybillItem;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;
import com.kreidev.cmpackagecouriers.courier.DeliveryNetworkReg;
import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
        }
    }

    @Override
    protected Brain.Provider<Allay> brainProvider() {
        return CourierAllayAi.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CourierAllayAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public void setWaybill(ItemStack waybill) {
        if (!waybill.is(DeliveryNetworkReg.WAYBILL_ITEM.get())) return;
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
        return this.getBrain().getMemory(DeliveryNetworkReg.DELIVERY_JOB_MEM.get()).orElse(null);
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
        this.setWaybill(ItemStack.parseOptional(this.registryAccess(), tag.getCompound(WAYBILL_TAG_KEY)));
    }

    public static boolean convert(Allay allay, ItemStack waybill) {
        if (!waybill.is(DeliveryNetworkReg.WAYBILL_ITEM.get())) return false;
        CourierAllayEntity courier = allay.convertTo(DeliveryNetworkReg.COURIER_ALLAY_ENTITY.get(), true);
        if (courier == null) return false;
        courier.setWaybill(waybill);
        return true;
    }

    public static boolean revert(CourierAllayEntity courier) {
        Allay allay = courier.convertTo(EntityType.ALLAY, true);
        return allay != null;
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getTarget() instanceof Mob mob)) return;

        ItemStack stack = event.getItemStack();
        Level level = event.getLevel();

        if (mob.getType() == EntityType.ALLAY && stack.is(DeliveryNetworkReg.WAYBILL_ITEM.get())) {
            if (!level.isClientSide && convert((Allay) mob, stack.copyWithCount(1))) {
                stack.consume(1, event.getEntity());
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
        } else if (mob.getType() == DeliveryNetworkReg.COURIER_ALLAY_ENTITY.get() && stack.isEmpty()) {
            // TODO: remove package first
            if (!level.isClientSide) {
                CourierAllayEntity courier = (CourierAllayEntity) mob;
                ItemStack waybill = courier.getWaybill();
                if (revert(courier)) {
                    event.getEntity().setItemInHand(InteractionHand.MAIN_HAND, waybill);
                }
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
        }
    }
}
