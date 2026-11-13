package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.PackageDestination;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Kept separate from the interface to make space for potential optimizations
public class GlobalDestinationManager {

    private static final int TIMEOUT_DURATION = 20;

    public static final Map<PackageDestination, Long> DESTINATIONS = new HashMap<>();

    public static void checkIn(ServerLevel level, PackageDestination dest) {
        DESTINATIONS.put(dest, level.getGameTime());
    }

    // TODO: profiling at scale and lag mitigation when needed
    public static Set<PackageDestination> findDestinations(ServerLevel level, String address) {
        long gameTime = level.getGameTime();
        DESTINATIONS.values().removeIf(p -> gameTime - p > TIMEOUT_DURATION);

        Set<PackageDestination> ret = new HashSet<>();
        for (Map.Entry<PackageDestination, Long> e : DESTINATIONS.entrySet()) {
            if (PackageItem.matchAddress(address, e.getKey().getAddress(level))) {
                ret.add(e.getKey());
            }
        }
        return ret;
    }

}