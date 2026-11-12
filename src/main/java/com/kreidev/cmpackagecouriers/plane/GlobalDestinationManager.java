package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.PackageDestination;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Kept separate from the interface to make space for potential optimizations
public class GlobalDestinationManager {

    private static final int TIMEOUT_DURATION = 20;

    public static final Map<PackageDestination, Pair<Long, String>> DESTINATIONS = new HashMap<>();

    public static void checkIn(ServerLevel level, PackageDestination dest, String address) {
        DESTINATIONS.put(dest, Pair.of(level.getGameTime(), address));
    }

    // TODO: profiling at scale and lag mitigation when needed
    public static Set<PackageDestination> getDestinations(ServerLevel level, String address) {
        long gameTime = level.getGameTime();
        DESTINATIONS.values().removeIf(p -> gameTime - p.getFirst() > TIMEOUT_DURATION);

        Set<PackageDestination> result = new HashSet<>();
        for (Map.Entry<PackageDestination, Pair<Long, String>> e : DESTINATIONS.entrySet()) {
            if (PackageItem.matchAddress(address, e.getValue().getSecond())) {
                result.add(e.getKey());
            }
        }
        return result;
    }

}