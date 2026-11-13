package com.kreidev.cmpackagecouriers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface PackageDestination {

    // NOTE: Can't be bothered with extendability for other mods, I'll be the one handling integration anyway.
    enum Type implements StringRepresentable {
        SMART_BLOCK_ENTITY("SmartBlockEntity", SmartBlockPackageDest.CODEC),
        PLAYER("Player", PlayerPackageDest.CODEC);

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        private final String name;
        private final MapCodec<? extends PackageDestination> codec;

        Type(String name, MapCodec<? extends PackageDestination> codec) { this.name = name; this.codec = codec; }
        public MapCodec<? extends PackageDestination> codec() { return codec; }
        @Override public String getSerializedName() { return name; }
    }

    Codec<PackageDestination> CODEC = Type.CODEC.dispatch(PackageDestination::getType, Type::codec);


    PackageDestination.Type getType();

    Vec3 getPos();

    ResourceKey<Level> getDim();

    String getAddress(ServerLevel level);

    /**
     * Called when the carrier arrives at the destination and to transfer the package to it.
     * @return false means it failed the transfer
     */
    boolean tryInsertPackage(ServerLevel level, PackageCarrier carrier);

    default boolean canReceive(ServerLevel level) {
        return true;
    }

    default void tickIncoming(ServerLevel level, PackageCarrier carrier) {
    }

    default PositionTracker getPositionTracker() {
        return new PositionTracker() {

            @Override
            public Vec3 currentPosition() {
                return PackageDestination.this.getPos();
            }

            @Override
            public BlockPos currentBlockPosition() {
                return BlockPos.containing(this.currentPosition());
            }

            @Override
            public boolean isVisibleBy(LivingEntity livingEntity) {
                return true;
            }
        };
    }

    static int getAddressPriority(String address) {
        return (int) -address.chars().filter(c -> c == '*').count();
    }
}
