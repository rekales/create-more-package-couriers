package com.kreidev.cmpackagecouriers;

import com.kreidev.cmpackagecouriers.plane.UnpackEffect;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PackageCouriersApi {

    private static final Map<Item, UnpackEffect> ITEM_UNPACK_EFFECTS = new HashMap<>();

    /**
     * @param item item which the effects will be added on
     * @param function a function that does something and manipulates the resulting item stack
     * see also cmpackagepipebombs for an example
     */
    public static void registerUnpackEffects(Item item, UnpackEffect function) {
        ITEM_UNPACK_EFFECTS.put(item, function);
    }

    public static boolean hasUnpackEffects(Item item) {
        return ITEM_UNPACK_EFFECTS.containsKey(item);
    }

    public static ItemStack handleUnpackEffects(Level level, ItemStack stack, Vec3 planePos, CourierTarget target) {
        UnpackEffect function = ITEM_UNPACK_EFFECTS.get(stack.getItem());
        if (function != null) {
            return function.apply(level, stack, planePos, target);
        }
        return stack;
    }
}