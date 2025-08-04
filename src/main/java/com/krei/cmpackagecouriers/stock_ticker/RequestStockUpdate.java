package com.krei.cmpackagecouriers.stock_ticker;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

import static com.krei.cmpackagecouriers.stock_ticker.StockCheckingItem.getAccurateSummary;

// Shamelessly copied from Create: Mobile Packages
public class RequestStockUpdate implements ServerboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestStockUpdate> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.networkId,
            RequestStockUpdate::new
    );
    private final UUID networkId;

    public RequestStockUpdate(UUID networkId) {
        if (networkId == null) {
            this.networkId = UUID.randomUUID();
            return;
        }
        this.networkId = networkId;
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player != null) {
            ItemStack stack = PortableStockTicker.find(player.getInventory());
            if (stack == null || stack.isEmpty()) return;

            GenericStackListPacket responsePacket = new GenericStackListPacket(getAccurateSummary(stack).get());
            CatnipServices.NETWORK.sendToClient(player, responsePacket);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return PortableStockTickerReg.PortableStockTickerPackets.REQUEST_STOCK_UPDATE;
    }
}
