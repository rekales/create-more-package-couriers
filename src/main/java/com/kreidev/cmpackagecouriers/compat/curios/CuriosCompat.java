package com.kreidev.cmpackagecouriers.compat.curios;

import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;

/**
 * Curios compatibility for location transmitter functionality.
 * Provides methods to check for location transmitters in Curios slots.
 */
public class CuriosCompat {

    /**
     * Checks if the player has an enabled location transmitter in their Curios inventory.
     * @param player The player to check
     * @return true if the player has an enabled location transmitter in Curios slots, false otherwise
     */
    public static boolean hasEnabledLocationTransmitterInCurios(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).map(curiosInventory -> {
            Map<String, ICurioStacksHandler> curios = curiosInventory.getCurios();
            
            for (ICurioStacksHandler handler : curios.values()) {
                IDynamicStackHandler stacks = handler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (stack.getItem() instanceof LocationTransmitterItem 
                            && LocationTransmitterItem.isEnabled(stack)) {
                        return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }

    /**
     * Checks if Curios is loaded and available.
     * @return true if Curios is loaded, false otherwise
     */
    public static boolean isCuriosLoaded() {
        try {
            Class.forName("top.theillusivec4.curios.api.CuriosApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}