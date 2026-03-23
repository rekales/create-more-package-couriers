package com.kreidev.cmpackagecouriers.stock_ticker;

import com.kreidev.cmpackagecouriers.PackageCouriersKeys;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.MenuEntry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;

import static com.kreidev.cmpackagecouriers.PackageCouriers.*;

// Shamelessly copied from Create: Mobile Packages
public class PortableStockTickerReg {

    public static final ItemEntry<PortableStockTicker> PORTABLE_STOCK_TICKER =
            REGISTRATE.item("portable_stock_ticker", PortableStockTicker::new)
                    .register();

    public static final MenuEntry<PortableStockTickerMenu> PORTABLE_STOCK_TICKER_MENU =
            REGISTRATE.menu(
                    "portable_stock_ticker_menu",
                    (MenuType, containerId, playerInventory) -> new PortableStockTickerMenu(containerId, playerInventory),
                    () -> PortableStockTickerScreen::new
            ).register();


    public static void register() {
        PortableStockTickerPackets.register();
        MinecraftForge.EVENT_BUS.addListener(PortableStockTickerReg::onKeyInput);
    }

    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;
        if (PackageCouriersKeys.OPEN_PORTABLE_STOCK_TICKER.isPressed()) {
            PortableStockTickerPackets.getChannel().sendToServer(new OpenPortableStockTicker());
        }
    }

}
