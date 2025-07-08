package com.krei.cmpackagecouriers;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

// Copied and Altered from TridentItem
// NOTE: Might need to remove projectileItem interface
// NOTE: Using a compass with target in an item frame or placard to set a coordinate address
public class DeliveryPlaneItem extends Item implements ProjectileItem, EjectorLaunchEffect {

    public DeliveryPlaneItem(Properties p) {
        super(p.stacksTo(1));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player
                && this.getUseDuration(stack, entityLiving) - timeLeft >= 10
                && !level.isClientSide()) {
            DeliveryPlaneProjectile plane = new DeliveryPlaneProjectile(level);
            plane.setPos(player.getX(), player.getEyeY()-0.1f, player.getZ());
            plane.setTarget(player);
            plane.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.8F, 1.0F);
            plane.pickup = AbstractArrow.Pickup.DISALLOWED;
            level.addFreshEntity(plane);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        DeliveryPlaneProjectile plane = new DeliveryPlaneProjectile(level);
        plane.setPos(new Vec3(pos.x(), pos.y(), pos.z()));
        return plane;
    }

    @Override
    public boolean onEject(ItemStack stack, Level level, BlockPos pos) {
        if (level.isClientSide())
            return false;

        float yaw = switch (level.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST  -> 90f;
            default    -> -90f;
        };

        String address = PackageItem.getAddress(stack);
        ItemStack packageItem;
        ItemContainerContents container = stack.get(PackageCouriers.PLANE_PACKAGE);
        if (container != null && container.getStackInSlot(0).getItem() instanceof PackageItem) {
            packageItem = container.getStackInSlot(0);
        } else {
            packageItem = PackageStyles.getRandomBox();
            // TODO: Do some exception because this shouldn't happen
        }

        MinecraftServer server = level.getServer();
        if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayerByName(address);
            if (player != null) {
                DeliveryPlaneProjectile plane = new DeliveryPlaneProjectile(level, stack);
                plane.setPos(Vec3.atCenterOf(pos).add(0,1,0));
                plane.setTarget(player);
                plane.setItem(packageItem);
                plane.shootFromRotation(player, -45F, yaw, 0.0F, 0.8F, 1.0F);
                plane.pickup = AbstractArrow.Pickup.DISALLOWED;
                level.addFreshEntity(plane);
                return true;
            } else {
                // TODO: Check for valid coordinate address
                PackageCouriers.LOGGER.debug("{} Not Found", address);
            }
        }
        return false;
    }
}
