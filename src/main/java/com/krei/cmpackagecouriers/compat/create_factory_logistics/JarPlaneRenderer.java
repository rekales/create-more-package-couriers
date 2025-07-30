package com.krei.cmpackagecouriers.compat.create_factory_logistics;

import com.krei.cmpackagecouriers.plane.DeliveryPlaneItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import net.createmod.catnip.platform.CatnipServices;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.FluidRenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class JarPlaneRenderer {

    public static final FluidRenderHelper<FluidStack> FLUID_RENDERER = (FluidRenderHelper<FluidStack>) CatnipServices.FLUID_RENDERER;

    public static void renderJar(ItemStack box, PoseStack ms, MultiBufferSource buffer, int light) {
        ms.pushPose();
        ms.scale(1.25f, 1.25f, 1.25f);

        ms.pushPose();
        ms.translate(-0.25, -0.25, 0);
        ms.mulPose(Axis.ZP.rotationDegrees(90));
        PartialModel model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
        if (model != null)
            CachedBuffers.partial(model, Blocks.AIR.defaultBlockState())
                    .translate(-.5, -PackageItem.getHeight(box), -.5)
                    .rotateCentered(-AngleHelper.rad(180), Direction.UP)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ms.popPose();

        ms.pushPose();
        ms.translate(-0.075, 0, 0);
        float scaleFactor = PackageItem.getHeight(box)/0.75f;
        ms.scale(scaleFactor, scaleFactor, scaleFactor);
        ms.translate(0, -0.25, 0);
        PartialModel rope = DeliveryPlaneItemRenderer.PACKAGE_ROPE.get(BuiltInRegistries.ITEM.getKey(PackageStyles.getDefaultBox().getItem()));
        if (rope != null)
            CachedBuffers.partial(rope, Blocks.AIR.defaultBlockState())
                    .translate(-.5, -PackageItem.getHeight(box), -.5)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ms.popPose();

        ms.pushPose();
        FluidStack fluidStack = FluidUtil.getFluidContained(box).orElse(FluidStack.EMPTY);
        ms.scale(0.9f, 0.9f, 0.9f);
        ms.translate(-0.375f, -0.5f, -0.25f);
        FLUID_RENDERER.renderFluidBox(fluidStack, 0f, 0f, 0f, 0.625f, 0.5f, 0.5f, buffer,
                ms, light, true, true);
        ms.popPose();

        ms.popPose();
    }
}
