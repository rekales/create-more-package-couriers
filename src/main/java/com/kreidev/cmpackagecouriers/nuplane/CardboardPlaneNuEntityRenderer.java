package com.kreidev.cmpackagecouriers.nuplane;

import com.kreidev.cmpackagecouriers.plane.CardboardPlaneItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.logistics.box.PackageStyles;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class CardboardPlaneNuEntityRenderer extends EntityRenderer<CardboardPlaneNuEntity> {

    public CardboardPlaneNuEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CardboardPlaneNuEntity entity, float yaw, float partialTicks, @NotNull PoseStack ms, @NotNull MultiBufferSource buffer, int light) {
        if (entity.tickCount < 4) {  // Skip first few ticks cuz it's jittery
            super.render(entity, yaw, partialTicks, ms, buffer, light);
            return;
        }

        ms.pushPose();
        ms.translate(0, 0.25, 0);
        ms.scale(0.75f, 0.75f, 0.75f);
        ms.mulPose(Axis.YP.rotationDegrees(90+yaw));
        ms.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entity.oldDeltaYaw, entity.newDeltaYaw)*-4));
        ms.mulPose(Axis.ZP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

        CardboardPlaneItemRenderer.renderPlane(PackageStyles.getDefaultBox(), ms, buffer, light);

        ms.popPose();

        super.render(entity, yaw, partialTicks, ms, buffer, light);
    }

    @SuppressWarnings({"NullableProblems", "DataFlowIssue"})
    @Override
    public ResourceLocation getTextureLocation(CardboardPlaneNuEntity cardboardPlaneNuEntity) {
        return null;
    }
}
