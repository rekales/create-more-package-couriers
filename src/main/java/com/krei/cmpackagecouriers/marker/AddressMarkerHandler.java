package com.krei.cmpackagecouriers.marker;

import com.krei.cmpackagecouriers.PackageCouriers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@EventBusSubscriber(modid=PackageCouriers.MODID)
public class AddressMarkerHandler {

    public static Map<MarkerTarget, MarkerTarget> markerMap = new HashMap<>();

    public static void addOrUpdateTarget(Level level, BlockPos pos, String address) {
        MarkerTarget marker = new MarkerTarget(level, pos, address);
        if (markerMap.containsKey(marker)) {
            marker = markerMap.get(marker);
            marker.resetTimeout();
        } else {
            markerMap.put(marker, marker);
            PackageCouriers.LOGGER.debug("Added: " + marker);
        }
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<MarkerTarget, MarkerTarget>> iterator = markerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<MarkerTarget, MarkerTarget> entry = iterator.next();
            MarkerTarget marker = entry.getValue();

            if (marker.tickAndCheckTimeout()) {
                iterator.remove();
                PackageCouriers.LOGGER.debug("Removed: " + marker);
            }
        }
    }

    @Nullable
    public static MarkerTarget getMarkerTarget(String address) {
        // Linear search is good enough
        for (MarkerTarget marker : markerMap.values()) {
            if (address.equals(marker.address)) {
                return marker;
            }
        }
        return null;
    }

    public static class MarkerTarget {
        public static final int TIMEOUT_TICKS = 20;
        public final BlockPos pos;
        public final Level level;
        public final String address;
        private int timeout = TIMEOUT_TICKS;

        public MarkerTarget(@Nonnull Level level, @Nonnull  BlockPos pos, @Nonnull  String address) {
            this.level = level;
            this.pos = pos;
            this.address = address;
        }

        public boolean tickAndCheckTimeout() {
            this.timeout--;
            return this.timeout < 0;
        }

        public void resetTimeout() {
            this.timeout = TIMEOUT_TICKS;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof MarkerTarget other)) return false;

            return Objects.equals(pos, other.pos) &&
                    Objects.equals(level.dimension(), other.level.dimension()) && // compare by dimension
                    Objects.equals(address, other.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, level.dimension(), address);
        }

        @Override
        public String toString() {
            return "(" + this.address + ": " + this.pos + ")";
        }
    }
}
