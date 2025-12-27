package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.kreidev.cmpackagecouriers.ServerConfig;
import com.kreidev.cmpackagecouriers.compat.Mods;
import com.kreidev.cmpackagecouriers.compat.curios.CuriosCompat;
import com.kreidev.cmpackagecouriers.marker.AddressMarkerHandler;
import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterItem;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

// Copied and Altered from TridentItem
// NOTE: Might need to remove projectileItem interface
// NOTE: Using a compass with target in an item frame or placard to set a coordinate address
public class CardboardPlaneItem extends Item implements EjectorLaunchEffect {

    public CardboardPlaneItem(Properties p) {
        super(p.stacksTo(1));
    }

    // TODO: throwing angles are a bit off, double check later.
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player
                && this.getUseDuration(stack, entityLiving) - timeLeft >= 10
                && !level.isClientSide()) {

            Vec3 pos = player.getEyePosition().subtract(0, 0.1, 0);
            boolean wasFired = this.fire(stack, level, pos, player.getXRot(), player.getYRot(), player);
            if (wasFired) {
                stack.shrink(1);
            } else {
                player.displayClientMessage(Component.translatable(PackageCouriers.MOD_ID + ".message.no_address"), true);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isCrouching()) {
            ItemStack box = CardboardPlaneItem.getPackage(player.getItemInHand(hand));
            player.getItemInHand(hand).shrink(1);
            player.getInventory().placeItemBackInInventory(box);
            player.getInventory().placeItemBackInInventory(PackageCouriers.CARDBOARD_PLANE_PARTS_ITEM.asStack());
        } else {
            player.startUsingItem(hand);
        }
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
    public boolean onEject(ItemStack stack, Level level, BlockPos pos, float yaw) {
        return fire(stack, level, Vec3.atCenterOf(pos.above()), yaw, -37.5F);
    }

    public boolean fire(ItemStack stack, Level level, Vec3 pos, float yaw, float pitch) {
        return fire(stack, level, pos, yaw, pitch, null);
    }

    public boolean fire(ItemStack stack, Level level, Vec3 pos, float yaw, float pitch, @Nullable Entity shooter) {
        if (level.isClientSide())
            return false;

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
            CardboardPlaneEntity plane = new CardboardPlaneEntity(level);
            plane.setPos(pos);
            plane.setPackage(packageItem);
            plane.setUnpack(stack.getOrDefault(PackageCouriers.PRE_OPENED, false));
            if (shooter != null) {
                plane.shootFromRotation(shooter, pitch, yaw, 0.0F, 0.8F, 1.0F);
            } else {
                plane.shootFromRotation(pitch, yaw, 0.0F, 0.8F, 1.0F);
            }

            ServerPlayer serverPlayer = server.getPlayerList().getPlayerByName(address);
            if (serverPlayer != null && ServerConfig.planePlayerTargets) {
                // Check if location transmitter is required and if the target player has one enabled
                if (!ServerConfig.locationTransmitterNeeded || hasEnabledLocationTransmitter(serverPlayer)) {
                    plane.setTarget(serverPlayer);
                    level.addFreshEntity(plane);
                    return true;
                }
                // TODO: handle this edge case properly
            } else {
                AddressMarkerHandler.MarkerTarget target = AddressMarkerHandler.getMarkerTarget(address);
                if (target != null && hasSpace(level, target.pos) && ServerConfig.planeLocationTargets) {
                    plane.setTarget(target.pos, target.level);
                    level.addFreshEntity(plane);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasSpace(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DepotBlockEntity depotBlockEntity) {
            return depotBlockEntity.getHeldItem().isEmpty();
        }
        // Other target types here, maybe using an injected interface for them instead of this.

        return true;
    }

    /**
     * Checks if the player has an enabled location transmitter in their inventory or Curios slots.
     * @param player The player to check
     * @return true if the player has an enabled location transmitter, false otherwise
     */
    public static boolean hasEnabledLocationTransmitter(ServerPlayer player) {
        // Check regular inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof LocationTransmitterItem && LocationTransmitterItem.isEnabled(stack)) {
                return true;
            }
        }

        // Check Curios slots if Curios is loaded
        if (Mods.CURIOS.isLoaded() && CuriosCompat.isCuriosLoaded()) {
            return CuriosCompat.hasEnabledLocationTransmitterInCurios(player);
        }

        return false;
    }

    public static ItemStack withPackage(ItemStack box) {
        ItemStack plane = PackageCouriers.CARDBOARD_PLANE_ITEM.asStack();
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
        if (plane.getItem() instanceof CardboardPlaneItem) {
            // added handling of @ in address to alow adress chaining and identifying player names
            String address = PackageItem.getAddress(getPackage(plane));
            int atIndex = address.indexOf('@');
            if (atIndex != -1) {
                address = address.substring(atIndex + 1);
            }
            return address;
        }
        return "";
    }

    public static void setPreOpened(ItemStack plane, boolean preopened) {
        if (plane.getItem() instanceof CardboardPlaneItem
                && plane.get(PackageCouriers.PLANE_PACKAGE) instanceof ItemContainerContents container
                && PackageItem.isPackage(container.getStackInSlot(0))) {
            plane.set(PackageCouriers.PRE_OPENED, preopened);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ItemStack box = getPackage(stack);
        if (box != null) {
            if (stack.getOrDefault(PackageCouriers.PRE_OPENED, false))
                tooltipComponents.add(Component.translatable("tooltip.cmpackagecouriers.cardboard_plane.preopened")
                        .withStyle(ChatFormatting.AQUA));
            box.getItem().appendHoverText(box, context, tooltipComponents, tooltipFlag);
        }
        else
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new CardboardPlaneItemRenderer()));
    }
}
