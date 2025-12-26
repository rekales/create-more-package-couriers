package com.kreidev.cmpackagecouriers.stock_ticker;

import net.createmod.catnip.platform.CatnipServices;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Shamelessly copied from Create: Mobile Packages
public class ClientScreenStorage {
    public static List<GenericStack> stacks = new ArrayList<>();

    private static int ticks = 0;

    public static void tick(UUID networkId) {
        if (ticks++ > 120) {
            update(networkId);
            ticks = 0;
        }
    }

    private static void update(UUID networkId) {
        CatnipServices.NETWORK.sendToServer(new RequestStockUpdate(networkId));
    }

    public static void manualUpdate(UUID networkId) {
        update(networkId);
    }
}
