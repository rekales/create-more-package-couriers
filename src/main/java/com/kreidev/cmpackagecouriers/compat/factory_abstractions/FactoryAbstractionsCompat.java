package com.kreidev.cmpackagecouriers.compat.factory_abstractions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

// Shamelessly copied from Create: Mobile Packages
public class FactoryAbstractionsCompat {
    public static final StreamCodec<RegistryFriendlyByteBuf, GenericOrder> GENERIC_ORDER_STREAM_CODEC =
            StreamCodec.of((buffer, order) -> order.write(buffer), GenericOrder::read);

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericStack> GENERIC_STACK_STREAM_CODEC =
            StreamCodec.of((buffer, stack) -> GenericStackSerializer.write(stack, buffer), GenericStackSerializer::read);
}
