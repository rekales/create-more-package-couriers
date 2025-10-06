package com.krei.cmpackagecouriers.compat.curios;

import com.krei.cmpackagecouriers.transmitter.LocationTransmitterItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Renderer for the Location Transmitter when worn in Curios slots.
 * Renders the transmitter on the player's belt area.
 */
public class LocationTransmitterRenderer implements ICurioRenderer {

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                          SlotContext slotContext,
                                                                          PoseStack matrixStack,
                                                                          RenderLayerParent<T, M> renderLayerParent,
                                                                          MultiBufferSource renderTypeBuffer,
                                                                          int light,
                                                                          float limbSwing,
                                                                          float limbSwingAmount,
                                                                          float partialTicks,
                                                                          float ageInTicks,
                                                                          float netHeadYaw,
                                                                          float headPitch) {
        if (!(stack.getItem() instanceof LocationTransmitterItem)) {
            return;
        }

        LivingEntity entity = slotContext.entity();
        
        // // Only render if the transmitter is enabled
        // if (!LocationTransmitterItem.isEnabled(stack)) {
        //     return;
        // }

        matrixStack.pushPose();

        // Position the transmitter on the belt area
        // Translate to the right hip area - closer to body and lower on hip
        matrixStack.translate(0.15, 0.45, -0.13);
        
        // Scale down the item to appropriate size for belt wearing
        matrixStack.scale(0.2f, 0.2f, 0.2f);
        
        // Rotate to face outward from the body and correct orientation
        matrixStack.mulPose(Axis.ZP.rotationDegrees(180f)); // Flip upright

        // Render the item
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                light,
                0xF000F0, // overlay
                matrixStack,
                renderTypeBuffer,
                entity.level(),
                (int) entity.position().x + (int) entity.position().y + (int) entity.position().z
        );

        matrixStack.popPose();
    }
}