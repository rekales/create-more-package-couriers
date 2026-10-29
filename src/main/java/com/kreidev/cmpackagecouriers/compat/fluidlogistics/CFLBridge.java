package com.kreidev.cmpackagecouriers.compat.fluidlogistics;

import com.kreidev.cmpackagecouriers.compat.Mods;
import com.yision.fluidlogistics.api.packager.PackageResourceDisplay;
import com.yision.fluidlogistics.api.packager.PackageResources;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.function.Supplier;

public final class CFLBridge {

    private static volatile boolean disabled;

    private CFLBridge() {
    }

    public static boolean isPresent() {
        return Mods.FLUIDLOGISTICS.isLoaded();
    }

    public static boolean isPackageResource(ItemStack stack) {
        return !stack.isEmpty() && safeCall(false, () -> PackageResources.findType(stack).isPresent());
    }

    public static boolean isPackageResource(GenericStack stack) {
        return isPackageResource(keyAsItemStack(stack));
    }

    public static boolean isPackageResource(BigGenericStack entry) {
        return isPackageResource(entry.get());
    }

    public static boolean containsPackageResource(GenericOrder order) {
        for (GenericStack stack : order.stacks()) {
            if (isPackageResource(stack)) {
                return true;
            }
        }
        return false;
    }

    public static int adjustAmount(ItemStack stack, int currentAmount, boolean forward, boolean shift, boolean control,
                                   int minAmount, int maxAmount, int steps, StockInteraction interaction) {
        return safeCall(currentAmount, () -> {
            PackageResourceDisplay.Interaction apiInteraction = switch (interaction) {
                case INVENTORY -> PackageResourceDisplay.Interaction.STOCK_KEEPER_INVENTORY;
                case ORDER -> PackageResourceDisplay.Interaction.STOCK_KEEPER_ORDER;
            };
            return PackageResources.adjustAmount(stack, new PackageResourceDisplay.Adjustment(
                            currentAmount, forward, shift, control, minAmount, maxAmount, steps, apiInteraction))
                    .orElse(currentAmount);
        });
    }

    public static ItemStack keyAsItemStack(GenericStack stack) {
        return BigGenericStack.of(stack.withAmount(1)).asStack().stack;
    }

    static <T> T safeCall(T fallback, Supplier<T> call) {
        if (!isPresent() || disabled) {
            return fallback;
        }
        try {
            if (!PackageResources.isBootstrapped()) {
                return fallback;
            }
            return call.get();
        } catch (LinkageError | RuntimeException e) {
            disabled = true;
            return fallback;
        }
    }

    public enum StockInteraction {
        INVENTORY,
        ORDER
    }
}
