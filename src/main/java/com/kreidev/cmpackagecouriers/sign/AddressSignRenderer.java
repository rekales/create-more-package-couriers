package com.kreidev.cmpackagecouriers.sign;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.List;

import static com.kreidev.cmpackagecouriers.PackageCouriers.resLoc;
import static net.minecraft.client.renderer.blockentity.SignRenderer.getDarkColor;

@ParametersAreNonnullByDefault
public class AddressSignRenderer implements BlockEntityRenderer<AddressSignBlockEntity> {

    public static final PartialModel ADDRESS_SIGN = PartialModel.of(resLoc("block/address_sign"));

    public final Font font;

//    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.33333334F, 1.05/32f);
    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.155, -0.01);

    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);

    public AddressSignRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(AddressSignBlockEntity blockEntity, float partialTick, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        SignBlock signblock = (SignBlock) state.getBlock();


        ms.pushPose();

        // TODO: don't use renderer to draw the model
        float yaw = switch (blockEntity.getBlockState().getValue(WallSignBlock.FACING)) {
            case Direction.NORTH -> -90f;
            case Direction.SOUTH -> 90f;
            case Direction.WEST  -> 0f;
            default              -> 180f;
        };

//        ms.mulPose(Axis.YP.rotationDegrees(yaw));

        CachedBuffers.partial(ADDRESS_SIGN, AddressSignReg.ADDRESS_SIGN_BLOCK.getDefaultState())
                .light(light)
                .rotateCentered((float)Math.toRadians(yaw), Direction.Axis.Y)
                .renderInto(ms, buffer.getBuffer(RenderType.CUTOUT));

        ms.popPose();

        ms.pushPose();

        this.translateSign(ms, -signblock.getYRotationDegrees(state), state);

        this.renderSignText(
                blockEntity.getBlockPos(),
                blockEntity.getFrontText(),
                ms,
                buffer,
                light,
                blockEntity.getTextLineHeight(),
                blockEntity.getMaxTextLineWidth()
        );

        ms.popPose();
    }


    void renderSignText(BlockPos pos, SignText text, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int lineHeight, int maxWidth) {
        poseStack.pushPose();

        // translate
        float textScale = 0.015625F * this.getSignTextRenderScale();
        poseStack.translate(TEXT_OFFSET.x(), TEXT_OFFSET.y(), TEXT_OFFSET.z());
        poseStack.scale(textScale, -textScale, textScale);


        int i = getDarkColor(text);
        int j = 4 * lineHeight / 2;

        FormattedCharSequence charseq = text.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), component -> {
            List<FormattedCharSequence> list = this.font.split(component, maxWidth);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.getFirst();
        })[0];

        FormattedCharSequence[] aformattedcharsequence = text.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), p_277227_ -> {
            List<FormattedCharSequence> list = this.font.split(p_277227_, maxWidth);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.getFirst();
        });
        int k;
        boolean flag;
        int l;
        if (text.hasGlowingText()) {
            k = text.getColor().getTextColor();
            flag = isOutlineVisible(pos, k);
            l = 15728880;
        } else {
            k = i;
            flag = false;
            l = packedLight;
        }



        for (int i1 = 0; i1 < 4; i1++) {
            FormattedCharSequence formattedcharsequence = aformattedcharsequence[i1];
            float f = (float)(-this.font.width(formattedcharsequence) / 2);
            if (flag) {
                this.font.drawInBatch8xOutline(formattedcharsequence, f, (float)(i1 * lineHeight - j), k, i,
                        poseStack.last().pose(), buffer, l);
            } else {
                this.font.drawInBatch(formattedcharsequence, f, (float)(i1 * lineHeight - j), k, false,
                        poseStack.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, l);
            }
        }

        poseStack.popPose();
    }

    public float getSignTextRenderScale() {
        return 0.5F;
    }

    static boolean isOutlineVisible(BlockPos pos, int textColor) {
        if (textColor == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localplayer = minecraft.player;
            if (localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping()) {
                return true;
            } else {
                Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(pos)) < (double)OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    void translateSign(PoseStack poseStack, float yRot, BlockState state) {
        poseStack.translate(0.5F, 0.75F * this.getSignModelRenderScale(), 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        if (!(state.getBlock() instanceof StandingSignBlock)) {
            poseStack.translate(0.0F, -0.3125F, -0.4375F);
        }
    }

    public float getSignModelRenderScale() {
        return 0.6666667F;
    }


    public static void init() {}

}
