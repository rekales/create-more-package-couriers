package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.PackageDestination;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

// Kept a separate class to make space for potential optimizations
public class GlobalDestinationManager {

    private static final int TIMEOUT_DURATION = 20;

    public static final Map<PackageDestination, Pair<Long, String>> DESTINATIONS = new HashMap<>();

    public static void checkIn(ServerLevel level, PackageDestination dest, String address) {
        DESTINATIONS.put(dest, Pair.of(level.getGameTime(), address));
    }

    // TODO: profiling at scale and lag mitigation when needed
    public static Map<PackageDestination, String> findDestinations(ServerLevel level, String address) {
        long gameTime = level.getGameTime();
        DESTINATIONS.values().removeIf(p -> gameTime - p.getFirst() > TIMEOUT_DURATION);

        Map<PackageDestination, String> result = new HashMap<>();
        for (Map.Entry<PackageDestination, Pair<Long, String>> e : DESTINATIONS.entrySet()) {
            if (PackageItem.matchAddress(address, e.getValue().getSecond())) {
                result.put(e.getKey(), e.getValue().getSecond());
            }
        }
        return result;
    }

}