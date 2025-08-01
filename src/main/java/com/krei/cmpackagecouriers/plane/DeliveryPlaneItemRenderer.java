package com.krei.cmpackagecouriers.plane;

import com.krei.cmpackagecouriers.PackageCouriers;
import com.krei.cmpackagecouriers.compat.create_factory_logistics.FactoryLogisticsCompat;
import com.krei.cmpackagecouriers.compat.create_factory_logistics.JarPlaneRenderer;
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
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class DeliveryPlaneItemRenderer extends CustomRenderedItemModelRenderer {
    public static final PartialModel DELIVERY_PLANE = PartialModel.of(ResourceLocation.fromNamespaceAndPath(PackageCouriers.MODID,
            "item/cardboard_plane"));
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
        ItemStack box = DeliveryPlaneItem.getPackage(stack);
        if (box.isEmpty() || !PackageItem.isPackage(box))
            box = AllBlocks.CARDBOARD_BLOCK.asStack();

        // TODO: Simplify
        ms.pushPose();

        if (transformType == ItemDisplayContext.GUI) {
            ms.translate(-0.2f, -0.3f, 0);
            ms.scale(0.3f, 0.3f, 0.3f);
        } else if (transformType.firstPerson()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && player.isUsingItem()) {
                ms.mulPose(Axis.YP.rotationDegrees(-90));
                ms.mulPose(Axis.ZP.rotationDegrees(-120));
                ms.mulPose(Axis.XP.rotationDegrees(-10));
                ms.translate(1f, -0.5f, 0);
                ms.scale(0.5f, 0.5f, 0.5f);
            } else {
                ms.mulPose(Axis.YP.rotationDegrees(90));
                ms.translate(0.25f, -0.4f, 0);
                ms.scale(0.5f, 0.5f, 0.5f);
            }
        } else if (transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
        || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            ms.mulPose(Axis.YP.rotationDegrees(90));
            ms.translate(0.25f, -0.75f, 0f);
            ms.scale(1/3f, 1/3f, 1/3f);
            ms.translate(0, PackageItem.getHeight(box),0);
        } else if (transformType == ItemDisplayContext.GROUND){
            ms.translate(0, -0.5f, 0);
            ms.scale(1/4f, 1/4f, 1/4f);
            ms.translate(0, PackageItem.getHeight(box),0);
        } else {
            ms.mulPose(Axis.XP.rotationDegrees(90));
            ms.translate(0, -0.25f, 0.5f);
            ms.translate(0, PackageItem.getHeight(box),0);
        }

        renderPlane(DeliveryPlaneItem.getPackage(stack), ms, buffer, light);
        ms.popPose();
    }

    public static void renderPlane(ItemStack box, PoseStack ms, MultiBufferSource buffer, int light) {
        if (box.isEmpty() || !PackageItem.isPackage(box))
            box = AllBlocks.CARDBOARD_BLOCK.asStack();

        ms.pushPose();
        ms.mulPose(Axis.YP.rotationDegrees(-90));
        CachedBuffers.partial(DELIVERY_PLANE, Blocks.AIR.defaultBlockState())
                .light(light)
                .translate(-24/16f, 0, -16/16f)
                .scale(3)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ms.popPose();

        ms.pushPose();
        if (ModList.get().isLoaded("create_factory_logistics")
                && FactoryLogisticsCompat.isJar(box)) {
            JarPlaneRenderer.renderJar(box, ms, buffer, light);
        } else {
            PartialModel model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
            if (model != null)
                CachedBuffers.partial(model, Blocks.AIR.defaultBlockState())
                        .translate(-.5, -PackageItem.getHeight(box), -.5)
                        .rotateCentered(-AngleHelper.rad(180), Direction.UP)
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            PartialModel rope = PACKAGE_ROPE.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
            if (rope != null)
                CachedBuffers.partial(rope, Blocks.AIR.defaultBlockState())
                        .translate(-.5, -PackageItem.getHeight(box), -.5)
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        }
        ms.popPose();
    }

    public static void init() {}
}
