package com.kreidev.cmpackagecouriers;

import com.kreidev.cmpackagecouriers.plane.UnpackEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class PackageCouriersApi {

    private static final Map<Item, UnpackEffect> ITEM_UNPACK_EFFECTS = new HashMap<>();

    public static void registerUnpackEffects(Item item, UnpackEffect consumer) {
        ITEM_UNPACK_EFFECTS.put(item, consumer);
    }

    public static boolean hasUnpackEffects(Item item) {
        return ITEM_UNPACK_EFFECTS.containsKey(item);
    }

    public static void handleUnpackEffects(Level level, ItemStack stack, Vec3 planePos, CourierTarget target) {
        UnpackEffect consumer = ITEM_UNPACK_EFFECTS.get(stack.getItem());
        if (consumer != null) {
            consumer.accept(level, stack, planePos, target);
        }
    }
}