package com.kreidev.cmpackagecouriers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

// For things that hold and deliver packages to destinations (i.e. CourierAllays, CardboardPlanes)
public interface PackageCarrier {

    Vec3 getPos();
    void setPackage(ItemStack box);
    ItemStack getPackage();
    @Nullable PackageDestination getTarget();
    default boolean shouldUnpack() {  // unpacking on arrival
        return false;
    }
}
