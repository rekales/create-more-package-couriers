package com.krei.cmpackagecouriers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeliveryPlaneRenderer extends EntityRenderer<DeliveryPlaneEntity> {

    protected DeliveryPlaneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DeliveryPlaneEntity entity) {
        return null;
    }

    @Override
    public void render(DeliveryPlaneEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer, int light) {
        CachedBuffers.partial(PackageCouriers.CASING, Blocks.AIR.defaultBlockState())
                .scale(.25f)
//                .translate(-2f, -1f, -2f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
}
