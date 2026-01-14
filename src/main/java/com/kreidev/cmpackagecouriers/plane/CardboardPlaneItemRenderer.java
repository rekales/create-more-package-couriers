package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.compat.create_factory_logistics.FactoryLogisticsCompat;
import com.kreidev.cmpackagecouriers.compat.create_factory_logistics.JarPlaneRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

import static com.kreidev.cmpackagecouriers.PackageCouriers.resLoc;

@OnlyIn(Dist.CLIENT)
public class CardboardPlaneItemRenderer extends CustomRenderedItemModelRenderer {
    public static final PartialModel DELIVERY_PLANE = PartialModel.of(resLoc("item/cardboard_plane"));

    public static final Map<ResourceLocation, PartialModel> PACKAGE_ROPE = new HashMap<>();

    static {
        //noinspection UnstableApiUsage
        for (PackageStyles.PackageStyle style : PackageStyles.STYLES) {
            ResourceLocation key = style.getItemId();
            PACKAGE_ROPE.put(key, PartialModel.of(getRopeModel(style.width(), style.height())));
        }
    }

    public static ResourceLocation getRopeModel(int width, int height) {
        String size = width + "x" + height;
        return resLoc("item/rope_" + size);
    }


    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                          ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ItemStack box = CardboardPlaneItem.getPackage(stack);
        if (box.isEmpty() || !PackageItem.isPackage(box))
            box = PackageStyles.getDefaultBox();

        ms.pushPose();
        LocalPlayer player = Minecraft.getInstance().player;
        if (transformType.firstPerson() && player != null && player.isUsingItem()) {
            ms.mulPose(Axis.YP.rotationDegrees(-160));
            ms.mulPose(Axis.XP.rotationDegrees(-120));
            ms.mulPose(Axis.YP.rotationDegrees(10));
            ms.translate(0, -0.2, -0.75);
        }

        ms.translate(0, -4/16f + PackageItem.getHeight(box)/3f, 0);
        renderPlane(box, ms, buffer, light);
        ms.popPose();
    }

    public static void renderPlane(ItemStack box, PoseStack ms, MultiBufferSource buffer, int light) {
        if (box.isEmpty() || !PackageItem.isPackage(box))
            box = PackageStyles.getDefaultBox();

        ms.pushPose();
        ms.translate(0, -4/16f, 0);

        CachedBuffers.partial(DELIVERY_PLANE, Blocks.AIR.defaultBlockState())
                .translate(-0.5f, 0, -0.5f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        ms.scale(1/3f, 1/3f, 1/3f);
        if (ModList.get().isLoaded("create_factory_logistics")
                && FactoryLogisticsCompat.isJar(box)) {
            JarPlaneRenderer.renderJar(box, ms, buffer, light);
        } else {
            PartialModel model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
            if (model != null)
                CachedBuffers.partial(model, Blocks.AIR.defaultBlockState())
                        .translate(-0.5, -PackageItem.getHeight(box), -1)
                        .rotateCentered(-AngleHelper.rad(90), Direction.UP)
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

            PartialModel rope = PACKAGE_ROPE.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
            if (rope != null)
                CachedBuffers.partial(rope, Blocks.AIR.defaultBlockState())
                        .translate(-0.5, -PackageItem.getHeight(box), -1)
                        .rotateCentered(-AngleHelper.rad(90), Direction.UP)
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        }
        ms.popPose();
    }

    public static void init() {}
}
