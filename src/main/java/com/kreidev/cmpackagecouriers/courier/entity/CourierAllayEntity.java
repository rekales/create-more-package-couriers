package com.kreidev.cmpackagecouriers.courier.entity;

import com.kreidev.cmpackagecouriers.courier.CourierMob;
import com.kreidev.cmpackagecouriers.courier.WaybillItem;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;
import com.kreidev.cmpackagecouriers.courier.DeliveryNetworkReg;
import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
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

    // Relevant only on serverside
    protected DeliveryNetwork network;  // TODO: or waybill stack?

    public CourierAllayEntity(EntityType<? extends CourierAllayEntity> entityType, Level level) {
        super(entityType, level);
        this.network = null;
        this.getPersistentData().putBoolean("TrainHat", true);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        if (this.network == null) {
            // revert to allay
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

    public void setDeliveryNetwork(DeliveryNetwork network) {
        this.network = network;
    }

    @Override
    public DeliveryNetwork getDeliveryNetwork() {
        return this.network;
    }

    @Override
    public @Nullable DeliveryNetwork.Job getCurrentJob() {
        return this.getBrain().getMemory(DeliveryNetworkReg.DELIVERY_JOB_MEM.get()).orElse(null);
    }

    public static boolean convert(Allay allay, DeliveryNetwork network) {
        CourierAllayEntity courier = allay.convertTo(DeliveryNetworkReg.COURIER_ALLAY_ENTITY.get(), true);
        if (courier == null) return false;
        courier.setDeliveryNetwork(network);
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
            DeliveryNetwork delNet = WaybillItem.getAssignedNetwork(stack);
            if (delNet != null) {
                if (!level.isClientSide && convert((Allay) mob, delNet)) {
                    stack.consume(1, event.getEntity());
                }
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                event.setCanceled(true);
            }
        } else if (mob.getType() == DeliveryNetworkReg.COURIER_ALLAY_ENTITY.get() && stack.isEmpty()) {
            // TODO: remove package first
            if (!level.isClientSide) {
                CourierAllayEntity courier = (CourierAllayEntity) mob;
                DeliveryNetwork delNet = courier.getDeliveryNetwork();
                if (revert(courier)) {
                    ItemStack waybill = new ItemStack(DeliveryNetworkReg.WAYBILL_ITEM.get());
                    WaybillItem.setAssignedNetwork(waybill, delNet);
                    event.getEntity().setItemInHand(InteractionHand.MAIN_HAND, waybill);
                }
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
        }
    }
}
