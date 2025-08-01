package com.krei.cmpackagecouriers.plane;

import com.krei.cmpackagecouriers.PackageCouriers;
import com.krei.cmpackagecouriers.marker.AddressMarkerHandler;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

// Copied and Altered from TridentItem
// NOTE: Might need to remove projectileItem interface
// NOTE: Using a compass with target in an item frame or placard to set a coordinate address
public class DeliveryPlaneItem extends Item implements EjectorLaunchEffect {

    public DeliveryPlaneItem(Properties p) {
        super(p.stacksTo(1));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player
                && this.getUseDuration(stack, entityLiving) - timeLeft >= 10
                && !level.isClientSide()) {

            String address = getAddress(stack);
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
                DeliveryPlaneEntity plane = new DeliveryPlaneEntity(level);
                plane.setPos(player.getX(), player.getEyeY()-0.1f, player.getZ());
                plane.setPackage(packageItem);
                plane.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.8F, 1.0F);

                ServerPlayer serverPlayer = server.getPlayerList().getPlayerByName(address);
                if (serverPlayer != null) {
                    plane.setTarget(serverPlayer);
                    level.addFreshEntity(plane);
                    stack.shrink(1);
                } else {
                    AddressMarkerHandler.MarkerTarget target =  AddressMarkerHandler.getMarkerTarget(address);
                    if (target != null) {
                        plane.setTarget(target.pos, target.level);
                        level.addFreshEntity(plane);
                        stack.shrink(1);
                    }
                }
            }
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
    public boolean onEject(ItemStack stack, Level level, BlockPos pos) {
        if (level.isClientSide())
            return false;

        float yaw = switch (level.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST  -> 90f;
            default    -> -90f;
        };

        String address = getAddress(stack);
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
            DeliveryPlaneEntity plane = new DeliveryPlaneEntity(level);
            plane.setPos(Vec3.atCenterOf(pos).add(0,1,0));
            plane.setPackage(packageItem);
            plane.shootFromRotation(-45F, yaw, 0.0F, 0.8F, 1.0F);

            ServerPlayer serverPlayer = server.getPlayerList().getPlayerByName(address);
            if (serverPlayer != null) {
                plane.setTarget(serverPlayer);
                level.addFreshEntity(plane);
                return true;
            } else {
                AddressMarkerHandler.MarkerTarget target =  AddressMarkerHandler.getMarkerTarget(address);
                if (target != null) {
                    plane.setTarget(target.pos, target.level);
                    level.addFreshEntity(plane);
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack withPackage(ItemStack box) {
        ItemStack plane = PackageCouriers.DELIVERY_PLANE_ITEM.asStack();
        setPackage(plane, box);
        return plane;
    }

    public static void setPackage(ItemStack plane, ItemStack box) {
        if (box.getItem() instanceof PackageItem) {
            ItemContainerContents container = ItemContainerContents.fromItems(NonNullList.of(ItemStack.EMPTY, box.copy()));
            plane.set(PackageCouriers.PLANE_PACKAGE, container);
        }
    }

    public static ItemStack getPackage(ItemStack plane) {
        ItemContainerContents container = plane.get(PackageCouriers.PLANE_PACKAGE);
        if (container == null)
            return ItemStack.EMPTY;
        return container.getStackInSlot(0);
    }

    public static String getAddress(ItemStack plane) {
        if (plane.getItem() instanceof DeliveryPlaneItem) {
            return PackageItem.getAddress(getPackage(plane));
        }
        return "";
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ItemStack box = getPackage(stack);
        if (box != null)
            box.getItem().appendHoverText(box, context, tooltipComponents, tooltipFlag);
        else
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new DeliveryPlaneItemRenderer()));
    }
}
