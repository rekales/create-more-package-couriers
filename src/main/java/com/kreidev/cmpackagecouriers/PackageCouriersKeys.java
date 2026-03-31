package com.kreidev.cmpackagecouriers;

import com.kreidev.cmpackagecouriers.stock_ticker.OpenPortableStockTicker;
import com.mojang.blaze3d.platform.InputConstants;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

// Shamelessly copied from Create: Mobile Packages
public enum PackageCouriersKeys {

    OPEN_PORTABLE_STOCK_TICKER("open_portable_stock_ticker",GLFW.GLFW_KEY_G);

    private KeyMapping keybind;
    private final String description;
    private final int key;
    private final boolean modifiable;

    PackageCouriersKeys(String description, int defaultKey) {
        this.description = PackageCouriers.MOD_ID + ".keyinfo." + description;
        this.key = defaultKey;
        this.modifiable = !description.isEmpty();
    }

    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;
        if (PackageCouriersKeys.OPEN_PORTABLE_STOCK_TICKER.isPressed()) {
            CatnipServices.NETWORK.sendToServer(OpenPortableStockTicker.INSTANCE);
        }
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        for (PackageCouriersKeys key : values()) {
            key.keybind = new KeyMapping(key.description, key.key, PackageCouriers.MOD_NAME);
            if (!key.modifiable)
                continue;

            event.register(key.keybind);
        }
    }

    public boolean isPressed() {
        if (!modifiable)
            return isKeyDown(key);
        return keybind.isDown();
    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance()
                .getWindow()
                .getWindow(), key);
    }
}
