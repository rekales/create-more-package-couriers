package com.kreidev.cmpackagecouriers.compat.create_factory_logistics;

import com.kreidev.cmpackagecouriers.plane.CardboardPlaneItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.FluidRenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class JarPlaneRenderer {

    @SuppressWarnings("unchecked")
    public static final FluidRenderHelper<FluidStack> FLUID_RENDERER = (FluidRenderHelper<FluidStack>) CatnipServices.FLUID_RENDERER;

    public static void renderJar(ItemStack box, PoseStack ms, MultiBufferSource buffer, int light) {
        ms.pushPose();

        ms.pushPose();
        ms.scale(1.25f, 1.25f, 1.25f);
        PartialModel model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
        if (model != null)
            CachedBuffers.partial(model, Blocks.AIR.defaultBlockState())
                    .translate(-0.5, -PackageItem.getHeight(box)-0.25f, -1)
                    .rotateCentered(-AngleHelper.rad(90), Direction.EAST)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        FluidStack fluidStack = FluidUtil.getFluidContained(box).orElse(FluidStack.EMPTY);
        ms.scale(0.98f, 0.98f, 0.98f);
        ms.translate(0, -0.01f, -0.01f);
        ms.translate(-4/16f, -8/16f, -10/16f);
        FLUID_RENDERER.renderFluidBox(fluidStack, 0f, 0f, 0f, 8/16f, 8/16f, 10/16f, buffer,
                ms, light, true, true);
        ms.popPose();

        ms.pushPose();
        ms.scale(0.825f, 0.825f, 0.825f);
        PartialModel rope = CardboardPlaneItemRenderer.PACKAGE_ROPE.get(BuiltInRegistries.ITEM.getKey(PackageStyles.getDefaultBox().getItem()));
        if (rope != null)
            CachedBuffers.partial(rope, Blocks.AIR.defaultBlockState())
                    .translate(-0.5, -PackageItem.getHeight(box)-0.25f, -1)
                    .rotateCentered(-AngleHelper.rad(90), Direction.UP)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ms.popPose();

        ms.popPose();
    }

    public static void init() {}
}
