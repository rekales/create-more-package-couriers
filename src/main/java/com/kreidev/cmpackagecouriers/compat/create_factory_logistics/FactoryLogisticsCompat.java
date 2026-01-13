package com.kreidev.cmpackagecouriers.compat.create_factory_logistics;

import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;

public class FactoryLogisticsCompat {
    public static boolean isJar(ItemStack itemStack) {
        return itemStack.getItem() instanceof JarPackageItem;
    }

    public static void init() {
        JarPlaneRenderer.init();
    }
}
