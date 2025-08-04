package com.krei.cmpackagecouriers.stock_ticker;

import com.krei.cmpackagecouriers.compat.factory_abstractions.FactoryAbstractionsCompat;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;

// Shamelessly copied from Create: Mobile Packages
public class GenericStackListPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericStackListPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(FactoryAbstractionsCompat.GENERIC_STACK_STREAM_CODEC), packet -> packet.stacks,
            GenericStackListPacket::new
    );

    private final List<GenericStack> stacks;

    // Standard constructor
    public GenericStackListPacket(List<GenericStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        ClientScreenStorage.stacks = stacks;
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return PortableStockTickerReg.PortableStockTickerPackets.BIG_ITEM_STACK_LIST;
    }
}
