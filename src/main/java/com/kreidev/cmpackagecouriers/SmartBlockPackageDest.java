package com.kreidev.cmpackagecouriers;

import com.kreidev.cmpackagecouriers.blockentity.PackageDestinationBehaviour;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmartBlockPackageDest implements PackageDestination {

    public static final MapCodec<SmartBlockPackageDest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("BlockPos").forGetter(dest -> dest.blockPos),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("Dim").forGetter(SmartBlockPackageDest::getDim)
    ).apply(instance, SmartBlockPackageDest::new));

    @Nonnull private final BlockPos blockPos;
    @Nonnull private final ResourceKey<Level> dim;
    @Nonnull private final Vec3 centerPos;

    public SmartBlockPackageDest(ServerLevel level, SmartBlockEntity be) {
        this.blockPos = be.getBlockPos();
        this.dim = level.dimension();
        this.centerPos = Vec3.atCenterOf(this.blockPos);
    }

    // For Codec Deserialization
    private SmartBlockPackageDest(BlockPos blockPos, ResourceKey<Level> dim) {
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
    public boolean tryInsertPackage(ServerLevel level, PackageCarrier carrier) {
        if (!(level.getBlockEntity(this.blockPos) instanceof SmartBlockEntity be)) return false;

        PackageDestinationBehaviour behaviour = be.getBehaviour(PackageDestinationBehaviour.TYPE);
        if (behaviour == null) return false;
        return behaviour.tryInsertPackage(carrier);
    }

    @Override
    public String toString() {
        return String.format("SBE{pos=%s}",
                this.blockPos
        );
    }
}
