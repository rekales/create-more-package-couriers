package com.kreidev.cmpackagecouriers.sign;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

@EventBusSubscriber(modid= PackageCouriers.MOD_ID)
public class AddressSignHandler {

    public static Map<AddressSignTarget, AddressSignTarget> targetMap = new HashMap<>();

    public static void addOrUpdateTarget(Level level, BlockPos pos, String address) {
        AddressSignTarget marker = new AddressSignTarget(level, pos, address);
        if (targetMap.containsKey(marker)) {
            marker = targetMap.get(marker);
            marker.resetTimeout();
        } else {
            targetMap.put(marker, marker);
        }
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<AddressSignTarget, AddressSignTarget>> iterator = targetMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<AddressSignTarget, AddressSignTarget> entry = iterator.next();
            AddressSignTarget marker = entry.getValue();

            if (marker.tickAndCheckTimeout()) {
                iterator.remove();
            }
        }
    }

    public static @Nullable AddressSignTarget getTarget(String address) {
        // Linear search is good enough
        for (AddressSignTarget marker : targetMap.values()) {
            if (PackageItem.matchAddress(address, marker.address)) {
                return marker;
            }
        }
        return null;
    }

public static class AddressSignTarget {
    public static final int TIMEOUT_TICKS = 20;
    public final BlockPos pos;
    public final Level level;
    public final String address;
    private int timeout = TIMEOUT_TICKS;

    public AddressSignTarget(@Nonnull Level level, @Nonnull  BlockPos pos, @Nonnull  String address) {
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
        if (!(obj instanceof AddressSignTarget other)) return false;

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
