package com.kreidev.cmpackagecouriers.sign;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.MenuEntry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.kreidev.cmpackagecouriers.PackageCouriers.REGISTRATE;

@SuppressWarnings("unused")
public class AddressSignReg {

    public static final BlockEntry<AddressSignBlock> ADDRESS_SIGN_BLOCK = REGISTRATE
            .block("address_sign", AddressSignBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::forceSolidOn)
            .properties(BlockBehaviour.Properties::noCollission)
            .properties(p -> p.strength(1.0F))
            .simpleItem()
            .register();

    public static final BlockEntityEntry<AddressSignBlockEntity> ADDRESS_SIGN_BLOCK_ENTITY = REGISTRATE
            .blockEntity("address_sign", AddressSignBlockEntity::new)
            .validBlock(ADDRESS_SIGN_BLOCK)
//            .renderer(() -> AddressSignRenderer::new)
            .register();

    @SuppressWarnings("DataFlowIssue")
    public static final MenuEntry<AddressSignMenu> ADDRESS_SIGN_MENU = REGISTRATE
            .menu("address_sign", AddressSignMenu::new, () -> AddressSignScreen::new)
            .register();

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AddressSignReg::clientInit);
        modEventBus.addListener(AddressSignReg::registerPackets);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        AddressSignRenderer.init();
        BlockEntityRenderers.register(
                ADDRESS_SIGN_BLOCK_ENTITY.get(),
                AddressSignRenderer::new
        );
    }

    private static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(AddressSignDataPacket.TYPE, AddressSignDataPacket.STREAM_CODEC, new AddressSignDataPacket.Handler());
    }}
