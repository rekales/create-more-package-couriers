package com.krei.cmpackagecouriers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class DeliveryPlaneItemRenderer extends CustomRenderedItemModelRenderer {
    public static final PartialModel DELIVERY_PLANE = PartialModel.of(ResourceLocation.fromNamespaceAndPath(PackageCouriers.MODID,
            "item/delivery_plane"));
    public static final Map<ResourceLocation, PartialModel> PACKAGE_ROPE = new HashMap<>();

    static {
        for (PackageStyles.PackageStyle style : PackageStyles.STYLES) {
            ResourceLocation key = style.getItemId();
            PACKAGE_ROPE.put(key, PartialModel.of(getRopeModel(style.width(), style.height())));
        }
    }

    public static ResourceLocation getRopeModel(int width, int height) {
        String size = width + "x" + height;
        return ResourceLocation.fromNamespaceAndPath(PackageCouriers.MODID, "item/rope_" + size);
    }


    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                          ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // TODO: Simplify
        ms.pushPose();

        if (transformType == ItemDisplayContext.GUI) {
            ms.scale(1/3f, 1/3f, 1/3f);
            ms.translate(-2/3f, -1.6f, 0);
        } else if (transformType.firstPerson()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && player.isUsingItem()) {
                ms.mulPose(Axis.YP.rotationDegrees(-90));
                ms.mulPose(Axis.ZP.rotationDegrees(-120));
                ms.mulPose(Axis.XP.rotationDegrees(-10));
                ms.translate(0.75f, -0.75f, 0);
                ms.scale(0.5f, 0.5f, 0.5f);
            } else {
                ms.mulPose(Axis.YP.rotationDegrees(90));
                ms.scale(0.5f, 0.5f, 0.5f);
                ms.translate(0.5f, -1.5f, 0);
            }
        } else if (transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
        || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            ms.mulPose(Axis.YP.rotationDegrees(90));
            ms.translate(0.25f, -0.75f, 0f);
            ms.scale(1/3f, 1/3f, 1/3f);
        } else if (transformType == ItemDisplayContext.GROUND){
            ms.translate(0, -0.5f, 0);
            ms.scale(1/4f, 1/4f, 1/4f);
        } else {
            ms.mulPose(Axis.XP.rotationDegrees(90));
            ms.translate(0, -0.25f, 0.5f);
            ms.scale(2/3f, 2/3f, 2/3f);
        }

        renderPlane(DeliveryPlaneItem.getPackage(stack), ms, buffer, light);
        ms.popPose();
    }

    public static void renderPlane(ItemStack box, PoseStack ms, MultiBufferSource buffer, int light) {
        if (box.isEmpty() || !PackageItem.isPackage(box))
            box = AllBlocks.CARDBOARD_BLOCK.asStack();

        ms.pushPose();

        ms.pushPose();
        ms.mulPose(Axis.YP.rotationDegrees(-90));
        CachedBuffers.partial(DELIVERY_PLANE, Blocks.AIR.defaultBlockState())
                .light(light)
                .translate(-24/16f, PackageItem.getHeight(box), -16/16f)
                .scale(3)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ms.popPose();

        PartialModel model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
        if (model != null)
            CachedBuffers.partial(model, Blocks.AIR.defaultBlockState())
                    .translate(-.5, 0, -.5)
                    .rotateCentered(-AngleHelper.rad(180), Direction.UP)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        PartialModel rope = PACKAGE_ROPE.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
        if (rope != null)
            CachedBuffers.partial(rope, Blocks.AIR.defaultBlockState())
                    .translate(-.5, 0, -.5)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        ms.popPose();
    }

    public static void init() {}
}
