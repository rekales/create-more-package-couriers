package com.kreidev.cmpackagecouriers.sign;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.kreidev.cmpackagecouriers.PackageCouriers.resLoc;

@ParametersAreNonnullByDefault
public class AddressSignBlockEntityRenderer extends SignRenderer {

    public final SignRenderer.SignModel signModel;

    public AddressSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);

        this.signModel = new SignRenderer.SignModel(context.bakeLayer(new ModelLayerLocation(resLoc("block/address_sign"), "main")));
    }

    @Override
    public void render(SignBlockEntity blockEntity, float partialTick, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState blockstate = blockEntity.getBlockState();
        SignBlock signblock = (SignBlock)blockstate.getBlock();
        this.renderSignWithText(blockEntity, ms, buffer, light, overlay, blockstate, signblock, WoodType.OAK, this.signModel);
    }


}
