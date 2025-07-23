package com.krei.cmpackagecouriers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class DeliveryPlaneEntityRenderer extends EntityRenderer<DeliveryPlaneProjectile> {

    public DeliveryPlaneEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DeliveryPlaneProjectile entity, float yaw, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
        // NOTE: Better tilt when curving, seems to be delayed/ahead by 1 tick
        // TODO: Firework sounds

        ms.pushPose();

        ms.scale(0.75f, 0.75f, 0.75f);
        ms.mulPose(Axis.YP.rotationDegrees(90+yaw));
        ms.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entity.oldDeltaYaw, entity.newDeltaYaw)*-4));
        ms.mulPose(Axis.ZP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

        DeliveryPlaneItemRenderer.renderPlane(entity.getPackage(), ms, buffer, light);

        ms.popPose();

        super.render(entity, yaw, partialTicks, ms, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(DeliveryPlaneProjectile entity) {
        return null;
    }

    public static void init() {}
}