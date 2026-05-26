package com.kreidev.cmpackagecouriers.courier;

import com.jcraft.jorbis.Block;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WaybillItem extends Item {

    public WaybillItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        assert player != null;
        if (!level.isClientSide) {
            if (level.getBlockState(pos).getBlock() instanceof DepotBlock) {
                DeliveryNetwork network = getAssignedNetwork(context.getItemInHand());
                if (network != null) {
                    if (network.hasNode(pos)) {
                        network.removeNode(pos);
                        player.sendSystemMessage(Component.literal("removed " + pos));
                    } else {
                        network.addNode(pos);
                        player.sendSystemMessage(Component.literal("added " + pos));
                    }
                }
            }
        }

        return super.useOn(context);
    }

    public static @Nullable DeliveryNetwork getAssignedNetwork(ItemStack waybill) {
        if (waybill.getItem() instanceof WaybillItem) {
            UUID id = waybill.get(CourierAllayReg.DELIVERY_NETWORK_ID.get());
            return DeliveryNetwork.getNetwork(id);
        }
        return null;
    }

    public static void setAssignedNetwork(ItemStack waybill, @Nullable DeliveryNetwork network) {
        if (waybill.getItem() instanceof WaybillItem) {
            if (network != null) {
                waybill.set(CourierAllayReg.DELIVERY_NETWORK_ID.get(), network.getId());
            } else {
                waybill.remove(CourierAllayReg.DELIVERY_NETWORK_ID.get());
            }
        }
    }

}
