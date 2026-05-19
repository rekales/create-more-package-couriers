package com.kreidev.cmpackagecouriers.compat.fluidlogistics;

import com.kreidev.cmpackagecouriers.compat.Mods;
import com.yision.fluidlogistics.item.CompressedTankItem;
import com.yision.fluidlogistics.util.FluidAmountHelper;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

public class CFLBridge {
    public static boolean isLoaded() {
        return Mods.FLUIDLOGISTICS.isLoaded();
    }

    public static boolean isVirtualFluid(ItemStack stack) {
        return stack.getItem() instanceof CompressedTankItem && CompressedTankItem.isVirtual(stack);
    }

    public static boolean isVirtualFluid(GenericStack stack) {
        return isVirtualFluid(keyAsItemStack(stack));
    }

    public static boolean isVirtualFluid(BigGenericStack entry) {
        return isVirtualFluid(entry.get());
    }

    public static boolean containsVirtualFluid(GenericOrder order) {
        for (GenericStack stack : order.stacks()) {
            if (isVirtualFluid(stack)) return true;
        }
        return false;
    }

    public static int adjustFluidRequestAmount(int currentAmount, boolean forward, boolean shift, boolean control,
                                               int minAmount, int maxAmount) {
        return FluidAmountHelper.adjustFluidRequestAmount(currentAmount, forward, shift, control, minAmount, maxAmount);
    }

    public static int adjustFluidRequestAmount(int currentAmount, boolean forward, boolean shift, boolean control,
                                               int minAmount, int maxAmount, int steps) {
        return FluidAmountHelper.adjustFluidRequestAmount(currentAmount, forward, shift, control, minAmount, maxAmount, steps);
    }

    public static ItemStack keyAsItemStack(GenericStack stack) {
        return BigGenericStack.of(stack.withAmount(1)).asStack().stack;
    }
}
