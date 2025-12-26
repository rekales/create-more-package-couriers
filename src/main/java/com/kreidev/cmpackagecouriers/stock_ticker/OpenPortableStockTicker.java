package com.kreidev.cmpackagecouriers.stock_ticker;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

// Shamelessly copied from Create: Mobile Packages
public class OpenPortableStockTicker implements ServerboundPacketPayload {
    public static final OpenPortableStockTicker INSTANCE = new OpenPortableStockTicker();
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPortableStockTicker> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public OpenPortableStockTicker() {
    }

    @Override
    public void handle(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (id, inv, ply) -> new PortableStockTickerMenu(id, inv),
                Component.translatable("item.cmpackagecouriers.portable_stock_ticker")
        ));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return PortableStockTickerReg.PortableStockTickerPackets.OPEN_PORTABLE_STOCK_TICKER;
    }
}
