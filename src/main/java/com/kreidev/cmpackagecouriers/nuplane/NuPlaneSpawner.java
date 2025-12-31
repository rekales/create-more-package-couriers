package com.kreidev.cmpackagecouriers.nuplane;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class NuPlaneSpawner extends Item {

    public NuPlaneSpawner(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (!level.isClientSide()) {
            Vec3 pos = player.getEyePosition();
            ItemStack box = PackageStyles.getDefaultBox();
            PackageItem.addAddress(box, "targ");
            CardboardPlaneManager.addPlane(level, pos, player.getXRot(), player.getYRot(), box);
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }
}
