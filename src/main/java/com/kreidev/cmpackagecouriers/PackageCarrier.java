package com.kreidev.cmpackagecouriers;

import com.simibubi.create.content.logistics.box.PackageEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

// For things that hold and deliver packages to destinations (i.e. CourierAllays, CardboardPlanes)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface PackageCarrier {

    Vec3 getPos();

    void setPackage(ItemStack box);

    // In theory, carriers would only carry a single package
    ItemStack getPackage();

    @Nullable PackageDestination getDestination();

    default void dropPackage(ServerLevel level) {
        level.addFreshEntity(PackageEntity.fromItemStack(level, this.getPos(), this.getPackage()));
        this.setPackage(ItemStack.EMPTY);
    }

    default boolean shouldUnpack() {  // unpacking on arrival
        return false;
    }
}
