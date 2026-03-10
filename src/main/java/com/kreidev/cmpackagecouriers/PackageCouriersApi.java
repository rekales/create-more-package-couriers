package com.kreidev.cmpackagecouriers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class PackageCouriersApi {

    private static final Map<Item, BiConsumer<Level, ItemStack>> ITEM_UNPACK_EFFECTS = new HashMap<>();

    public static void registerUnpackEffects(Item item, BiConsumer<Level, ItemStack> handler) {
        ITEM_UNPACK_EFFECTS.put(item, handler);
    }

    public static boolean hasUnpackEffects(Item item) {
        return ITEM_UNPACK_EFFECTS.containsKey(item);
    }

    public static void handleUnpackEffects(Level level, ItemStack stack) {
        BiConsumer<Level, ItemStack> handler = ITEM_UNPACK_EFFECTS.get(stack.getItem());
        if (handler != null) {
            handler.accept(level, stack);
        }
    }
}