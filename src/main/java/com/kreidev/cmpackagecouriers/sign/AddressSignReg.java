package com.kreidev.cmpackagecouriers.sign;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static com.kreidev.cmpackagecouriers.PackageCouriers.REGISTRATE;

public class AddressSignReg {

    public static final BlockEntry<AddressSignBlock> ADDRESS_SIGN_BLOCK = REGISTRATE
            .block("address_sign", AddressSignBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::forceSolidOn)
            .simpleItem()
            .register();

    // TODO: change to proper generic type later
    public static final BlockEntityEntry<AddressSignBlockEntity> ADDRESS_SIGN_BLOCK_ENTITY = REGISTRATE
            .blockEntity("address_sign", AddressSignBlockEntity::new)
            .validBlock(ADDRESS_SIGN_BLOCK)
//            .renderer(() -> AddressSignBlockEntityRenderer::new)
//            .renderer(() -> AddressSignRenderer::new)
            .register();

    public static void register() {
    }

    public static void clientInit() {
        AddressSignRenderer.init();
        BlockEntityRenderers.register(
                ADDRESS_SIGN_BLOCK_ENTITY.get(),
                AddressSignRenderer::new
        );

    }
}
