package com.krei.cmpackagecouriers.compat.jei;

import com.krei.cmpackagecouriers.PackageCouriers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

// Shamelessly copied from Create: Mobile Packages
@JeiPlugin
public class JEICompat implements IModPlugin{
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PackageCouriers.MODID, "jei_plugin");

    public static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }
}
