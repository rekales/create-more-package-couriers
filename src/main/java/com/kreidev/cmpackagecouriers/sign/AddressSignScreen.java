package com.kreidev.cmpackagecouriers.sign;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

@ParametersAreNonnullByDefault
public class AddressSignScreen extends AbstractSimiContainerScreen<AddressSignMenu> {

    public AddressSignBlockEntity be;
    public boolean deferFocus;
    public String address;

    public List<Rect2i> extraAreas = Collections.emptyList();
    public IconButton confirmButton;
    public AddressEditBox addressBox;
    public AddressSignScreen.GuiTexture background;

    public AddressSignScreen(AddressSignMenu container, Inventory inv, Component title) {
        super(container, inv, title);  // TODO: Translatable component
        this.background = new GuiTexture(PackageCouriers.MOD_ID, "address_sign", 218, 79);
        this.be = this.getMenu().contentHolder;
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 7);
        setWindowSize(Math.max(background.getWidth(), PLAYER_INVENTORY.getWidth()),
                background.getHeight() + 4 + PLAYER_INVENTORY.getHeight());
        super.init();

        int x = leftPos;
        int y = topPos;

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            if (minecraft == null || minecraft.player == null) return;
            minecraft.player.closeContainer();
        });
        addRenderableWidget(confirmButton);

        addressBox = new AddressEditBox(this, this.font, x + 44, y + 28, 129, 9, false);
        addressBox.setTextColor(0xffffff);
        addressBox.setValue(be.getAddress());
        addressBox.setResponder(this::onAddressEdited);
        addRenderableWidget(addressBox);

        setFocused(addressBox);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (deferFocus) {
            deferFocus = false;
            setFocused(addressBox);
        }
        addressBox.tick();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.getHeight() + 4;
        renderPlayerInventory(graphics, invX, invY);

        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(leftPos + 16, topPos + 23, 0);
        GuiGameElement.of(PackageStyles.getDefaultBox())
                .render(graphics);
        ms.popPose();
    }

    private void onAddressEdited(String s) {
        this.address = s;
        CatnipServices.NETWORK.sendToServer(new AddressSignDataPacket(this.be.getBlockPos(), s));
    }

    public static class GuiTexture implements ScreenElement, TextureSheetSegment {
        private final ResourceLocation location;
        private final int width;
        private final int height;

        GuiTexture(String namespace, String location, int width, int height) {
            this.location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/gui/" + location + ".png");
            this.width = width;
            this.height = height;
        }

        @Override
        public @NotNull ResourceLocation getLocation() {
            return location;
        }

        @Override
        public int getStartX() {
            return 0;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getStartY() {
            return 0;
        }

        @Override
        public void render(GuiGraphics graphics, int x, int y) {
            graphics.blit(location, x, y, 0, 0, width, height);
        }
    }
}