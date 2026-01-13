package com.kreidev.cmpackagecouriers.plane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.logistics.box.PackageStyles;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CardboardPlaneEntityRenderer extends EntityRenderer<CardboardPlaneEntity> {

    public CardboardPlaneEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CardboardPlaneEntity entity, float yaw, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
        // NOTE: Better tilt when curving, seems to be delayed/ahead by 1 tick
        // NOTE: Should use relative delta yaw for delta yaw instead of absolute delta yaw
        // TODO: Fade out at far distances
        // TODO: dynamic package render

        if (entity.tickCount < 4) {  // Skip first few ticks cuz it's jittery
            super.render(entity, yaw, partialTicks, ms, buffer, light);
            return;
        }

        ms.pushPose();
        ms.mulPose(Axis.YP.rotationDegrees(yaw));
        ms.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        ms.mulPose(Axis.ZP.rotationDegrees(-Mth.lerp(partialTicks, entity.oldDeltaYaw, entity.newDeltaYaw)*-4));

        ms.scale(2, 2, 2);
        ms.translate(0,0.35f,0.15f);
        CardboardPlaneItemRenderer.renderPlane(PackageStyles.getDefaultBox(), ms, buffer, light);

        ms.popPose();

        super.render(entity, yaw, partialTicks, ms, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CardboardPlaneEntity entity) {
        return null;
    }

    public static void init() {}
}