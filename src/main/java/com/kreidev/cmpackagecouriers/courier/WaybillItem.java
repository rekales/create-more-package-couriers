package com.kreidev.cmpackagecouriers.courier;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WaybillItem extends Item {

    private static int clientNodeTimeout = 0;
    private static List<BlockPos> clientNodes = new ArrayList<>();

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
                        player.sendSystemMessage(Component.literal("Removed " + pos));
                    } else {
                        network.addNode(pos);
                        player.sendSystemMessage(Component.literal("Added " + pos));
                    }
                } else {
                    player.sendSystemMessage(Component.literal("Network not set"));
                }
            }
        }

        return super.useOn(context);
    }

    // Can't use DataComponents, otherwise every single waybill would have redundant data.
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide) {
            if (!(isSelected && entity instanceof ServerPlayer player)) return;
            DeliveryNetwork network = getAssignedNetwork(stack);
            if (network == null) return;
            if (level.getGameTime() % 2 == 1) {
                PacketDistributor.sendToPlayer(player, new DeliveryNetworkNodesPacket(network.getAllNodes()));
            }
        } else {
            boolean timeout = true;
            if (clientNodeTimeout > 0) {
                clientNodeTimeout--;
                timeout = false;
            } else {
                if (!clientNodes.isEmpty()) {
                    clientNodes.clear();
                }
            }

            if (isSelected && !timeout) {
                drawNodeOutlines(level);
            }
        }
    }

    public static void updateClientNodes(List<BlockPos> nodeList) {
        clientNodeTimeout = 10;
        clientNodes = nodeList;
    }

    private static void drawNodeOutlines(Level level) {
        for (BlockPos pos : clientNodes) {
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getShape(level, pos);
            if (!shape.isEmpty()) {
                Outliner.getInstance()
                        .showAABB(pos + "delnet", shape.bounds().move(pos))
                        .colored(0x9ede73)
                        .lineWidth(0.0625F);
            }
        }
    }

    public static @Nullable DeliveryNetwork getAssignedNetwork(ItemStack waybill) {
        if (!(waybill.getItem() instanceof WaybillItem)) return null;

        UUID id = waybill.get(DeliveryNetworkReg.DELIVERY_NETWORK_ID.get());
        if (id == null) return null;

        DeliveryNetwork delNet = DeliveryNetworkManager.getNetwork(id);
        if (delNet == null) {
            PackageCouriers.LOGGER.error("Delivery network not found with UUID of: " + id);
            waybill.remove(DeliveryNetworkReg.DELIVERY_NETWORK_ID.get());
            return null;
        }

        return delNet;
    }

    public static void setAssignedNetwork(ItemStack waybill, @Nullable DeliveryNetwork network) {
        if (waybill.getItem() instanceof WaybillItem) {
            if (network != null) {
                waybill.set(DeliveryNetworkReg.DELIVERY_NETWORK_ID.get(), network.getId());
            } else {
                waybill.remove(DeliveryNetworkReg.DELIVERY_NETWORK_ID.get());
            }
        }
    }

}
