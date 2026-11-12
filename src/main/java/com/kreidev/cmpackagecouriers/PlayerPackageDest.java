package com.kreidev.cmpackagecouriers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlayerPackageDest implements PackageDestination {

    // NOTE: Entity not saved cuz I can't be assed to deal with it, edge case is unlikely and already has been mitigated anyway.
    public static final MapCodec<PlayerPackageDest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec3.CODEC.fieldOf("LastPos").forGetter(PlayerPackageDest::getPos),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("LastDim").forGetter(PlayerPackageDest::getDim)
    ).apply(instance, PlayerPackageDest::new));

    @Nullable private final Player entity;
    @Nonnull private Vec3 lastPos;
    @Nonnull private ResourceKey<Level> lastDim;

    public PlayerPackageDest(Player player) {
        this.entity = player;
        this.lastPos = player.getEyePosition();
        this.lastDim = player.level().dimension();
    }

    // For Codec Deserialization
    private PlayerPackageDest(Vec3 lastPos, ResourceKey<Level> lastDim) {
        this.entity = null;
        this.lastPos = lastPos;
        this.lastDim = lastDim;
    }

    @Override
    public Type getType() {
        return Type.PLAYER;
    }

    @Override
    public Vec3 getPos() {
        if (this.entity != null) {
            this.lastPos = this.entity.getEyePosition();
        }
        return this.lastPos;
    }

    @Override
    public ResourceKey<Level> getDim() {
        if (this.entity != null) {
            this.lastDim = this.entity.level().dimension();
        }
        return this.lastDim;
    }

    @Override
    public boolean tryInsertPackage(ServerLevel level, PackageCarrier carrier) {
        if (this.entity == null) return false;
        if (this.entity.isRemoved() || this.entity.isDeadOrDying()) return false;

        if (carrier.shouldUnpack()) {
            ItemStackHandler stacks = PackageItem.getContents(carrier.getPackage());
            for (int slot = 0; slot < stacks.getSlots(); slot++) {
                ItemStack stack = stacks.getStackInSlot(slot);
                if (PackageCouriersApi.hasUnpackEffects(stack.getItem())) {
                    stack = PackageCouriersApi.handleUnpackEffects(level, stack, this.getPos(), this);
                    if (stack.isEmpty()) continue;
                }
                this.entity.getInventory().placeItemBackInInventory(stack);
            }
        } else {
            this.entity.getInventory().placeItemBackInInventory(carrier.getPackage());
        }
        carrier.setPackage(ItemStack.EMPTY);
        return true;
    }

    @Override
    public String toString() {
        return String.format("Player{name=%s, pos=%s}",
                this.entity != null ? this.entity.getName().toString() : "",
                this.getPos()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerPackageDest that)) return false;
        if (that.entity == null) return false;
        return Objects.equals(this.entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.entity) * 429;
    }
}
