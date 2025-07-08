package com.krei.cmpackagecouriers.mixin;

import com.krei.cmpackagecouriers.DeliveryPlaneItem;
import com.krei.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = BeltDeployerCallbacks.class, remap = false)
public class BeltDeployerCallbacksMixin {

    @Inject(
            method = "activate",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;clearFanProcessingData()V"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void beforeClearFanProcessingData(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler, DeployerBlockEntity blockEntity,
                                                     Recipe<?> recipe, CallbackInfo ci, List<TransportedItemStack> collect) {
        if (collect != null
                && !collect.isEmpty()
                && transported.stack.getItem() instanceof PackageItem
                && PackageCouriers.CARDBOARD_PLANE_ITEM.isIn(blockEntity.getPlayer().getMainHandItem())
                && collect.getFirst().stack.getItem() instanceof DeliveryPlaneItem) {
            ItemContainerContents container = ItemContainerContents.fromItems(NonNullList.of(ItemStack.EMPTY, transported.stack.copy()));
            collect.getFirst().stack.set(PackageCouriers.PLANE_PACKAGE, container);
            PackageItem.addAddress(collect.getFirst().stack, PackageItem.getAddress(transported.stack));
        }
    }
}
