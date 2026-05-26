package com.kreidev.cmpackagecouriers.courier;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

@MethodsReturnNonnullByDefault
public record DeliveryNetworkNodesPacket(List<BlockPos> blockPosList) implements CustomPacketPayload {

    public static final Type<DeliveryNetworkNodesPacket> TYPE =
            new Type<>(PackageCouriers.resLoc("delivery_network_nodes"));

    public static final StreamCodec<ByteBuf, DeliveryNetworkNodesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    DeliveryNetworkNodesPacket::blockPosList,
                    DeliveryNetworkNodesPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeliveryNetworkNodesPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            WaybillItem.updateClientNodes(packet.blockPosList);
        });
    }
}
