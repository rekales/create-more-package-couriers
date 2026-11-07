package com.kreidev.cmpackagecouriers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmartBEPackageDest implements PackageDestination {

    public static final MapCodec<SmartBEPackageDest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("BlockPos").forGetter(dest -> dest.blockPos),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("Dim").forGetter(SmartBEPackageDest::getDim)
    ).apply(instance, SmartBEPackageDest::new));

    @Nonnull private final BlockPos blockPos;
    @Nonnull private final ResourceKey<Level> dim;
    @Nonnull private final Vec3 centerPos;

    public SmartBEPackageDest(ServerLevel level, SmartBlockEntity be) {
        this.blockPos = be.getBlockPos();
        this.dim = level.dimension();
        this.centerPos = Vec3.atCenterOf(this.blockPos);
    }

    // For Codec Deserialization
    private SmartBEPackageDest(BlockPos blockPos, ResourceKey<Level> dim) {
        this.blockPos = blockPos;
        this.dim = dim;
        this.centerPos = Vec3.atCenterOf(this.blockPos);
    }

    @Override
    public Type getType() {
        return Type.SMART_BLOCK_ENTITY;
    }

    @Override
    public Vec3 getPos() {
        return this.centerPos;
    }

    @Override
    public ResourceKey<Level> getDim() {
        return this.dim;
    }

    @Override
    public boolean transferPackage(ServerLevel level, PackageCarrier carrier) {
        if (!(level.getBlockEntity(this.blockPos) instanceof SmartBlockEntity be)) return false;

        // TODO: use unified sbe behaviour
        if (be instanceof DepotBlockEntity depot) {
            if (depot.getHeldItem().is(Items.AIR)) {
                depot.setHeldItem(carrier.getPackage());
                depot.notifyUpdate();
                return true;
            }
            return false;
        } else if (BeltHelper.getSegmentBE(level, this.blockPos) instanceof BeltBlockEntity belt) {
            TransportedItemStack tStack = belt.getInventory().getStackAtOffset(belt.index);
            if (tStack == null) {
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, belt.getBlockPos(), null);
                if (handler == null) return false;
                handler.insertItem(0, carrier.getPackage(), false);
                return true;
            }
        }

        return false;
    }
}
