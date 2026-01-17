package com.kreidev.cmpackagecouriers.stock_ticker;

import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.ArrayList;
import java.util.List;

// Shamelessly copied from Create: Mobile Packages
public class ClientScreenStorage {
    public static List<GenericStack> stacks = new ArrayList<>();

    private static int ticks = 0;

    public static void tick() {
        if (ticks++ > 20) {
            update();
            ticks = 0;
        }
    }

    private static void update() {
        PortableStockTickerPackets.getChannel().sendToServer(new RequestStockUpdate());
    }

    public static void manualUpdate() {
        update();
    }
}