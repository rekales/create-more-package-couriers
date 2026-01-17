package com.kreidev.cmpackagecouriers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@SuppressWarnings("FieldMayBeFinal")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber(modid= PackageCouriers.MOD_ID)
public class CourierTarget {
    public enum Type implements StringRepresentable {
        BLOCK, ENTITY;

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    // NOTE: Entity not saved cuz I can't be assed to deal with it, edge case is unlikely and already has been mitigated anyway.
    public static final MapCodec<CourierTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(Type::values).fieldOf("Type").forGetter(CourierTarget::getType),
            Codec.STRING.fieldOf("Address").forGetter(CourierTarget::getAddress),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("Dim").forGetter(CourierTarget::getDim),
            Vec3.CODEC.fieldOf("Pos").forGetter(CourierTarget::getPos)
    ).apply(instance, CourierTarget::new));

    private final Type type;
    @Nullable private Entity entity;
    @Nonnull private Vec3 pos;
    @Nonnull private ResourceKey<Level> dim;
    @Nonnull private String address;

    public CourierTarget(String address, Entity entity) {
        this.type = Type.ENTITY;
        this.address = address;
        this.entity = entity;
        this.dim = entity.level().dimension();
        this.pos = entity.position();
    }

    public CourierTarget(String address, Level level, BlockPos pos) {
        this.type = Type.BLOCK;
        this.address = address;
        this.entity = null;
        this.pos = Vec3.atCenterOf(pos);
        this.dim = level.dimension();
    }

    // For Serialization
    private CourierTarget(Type type, String address, ResourceKey<Level> dim, Vec3 pos) {
        this.type = Type.BLOCK;
        this.address = address;
        this.entity = null;
        this.pos = pos;
        this.dim = dim;
    }

    // Only relevant for Type.ENTITY
    private void update() {
        if (this.entity != null) {
            if (this.entity.isRemoved()) {
                this.entity = null;
            } else if (this.entity instanceof LivingEntity) {
                this.pos = this.entity.getEyePosition();
            } else {
                this.pos = this.entity.position();
            }
        }
    }

    public Type getType() {
        return this.type;
    }
    public Vec3 getPos() {
        return this.pos;
    }
    public ResourceKey<Level> getDim() {
        return this.dim;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public @Nullable Entity getEntity() {
        return this.entity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CourierTarget other) {
            if (this.type == Type.ENTITY) {
                return this.address.equals(other.address) && this.entity == other.entity;
            } else {
                return this.address.equals(other.address) && this.dim == other.dim && this.pos.equals(other.pos);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.type == Type.ENTITY) {
            return Objects.hash(this.address, this.entity);
        } else {
            return Objects.hash(this.address, this.dim, this.pos);
        }
    }

    @Override
    public String toString() {
        return "(" + this.address + ": " + this.pos + ")";
    }


    public static final int TIMEOUT_TICKS = 20;
    public static Map<CourierTarget, Integer> activeTargets = new HashMap<>();

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<CourierTarget, Integer>> iterator = activeTargets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<CourierTarget, Integer> entry = iterator.next();
            entry.getKey().update();
            entry.setValue(entry.getValue()+1);

            if (entry.getValue() > TIMEOUT_TICKS) {
                iterator.remove();
            }
        }
    }

    public static void addOrUpdateTarget(CourierTarget target) {
        activeTargets.put(target, 0);
    }

    public static @Nullable CourierTarget getActiveTarget(String address) {
        // Linear search is good enough
        CourierTarget ret = null;
        for (CourierTarget target : activeTargets.keySet()) {
            if (PackageItem.matchAddress(address, target.getAddress())) {
                if (ret == null || target.getType() == Type.ENTITY) {  // Prioritize entity targets
                    ret = target;
                }
            }
        }
        return ret;
    }
}