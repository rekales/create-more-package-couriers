package com.kreidev.cmpackagecouriers.sign;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.kreidev.cmpackagecouriers.PackageCouriers.resLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AddressSignDataPacket(BlockPos pos, String address) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, AddressSignDataPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AddressSignDataPacket::pos,
            ByteBufCodecs.STRING_UTF8, AddressSignDataPacket::address,
            AddressSignDataPacket::new
    );

    public static final CustomPacketPayload.Type<AddressSignDataPacket> TYPE =
            new CustomPacketPayload.Type<>(resLoc("address_sign_data"));


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<AddressSignDataPacket> {
        public void handle(AddressSignDataPacket packet, IPayloadContext context) {
            Player player = context.player();
            Level level = player.level();
            if (!player.position().closerThan(packet.pos.getCenter(), player.blockInteractionRange())) return;
            if (!(level.getBlockEntity(packet.pos) instanceof AddressSignBlockEntity be)) return;
            be.setAddress(packet.address);
        }
    }
}