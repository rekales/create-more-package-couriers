package com.kreidev.cmpackagecouriers.courier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class DeliveryNetwork {

    public static final MapCodec<DeliveryNetwork> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("ID").forGetter(DeliveryNetwork::getId),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("Dim").forGetter(DeliveryNetwork::getDimension),
            BlockPos.CODEC.listOf().<Set<BlockPos>>xmap(HashSet::new, ArrayList::new).fieldOf("Nodes").forGetter(DeliveryNetwork::getNodes)
            ).apply(instance, DeliveryNetwork::new));

    @Nonnull private final UUID id;
    private final ResourceKey<Level> dim;
    private final Set<BlockPos> nodes;  // TODO: Consider using an interface to allow player destinations
    private final Queue<Job> availableJobs;
    private final Map<Job, CourierMob> activeJobs;  // TODO: make the courier remind the delnet because of load/save inconsistencies.
    // TODO: hotspot positions
    // TODO: default address

    public DeliveryNetwork(ServerLevel level) {
        this.id = UUID.randomUUID();
        this.dim = level.dimension();
        this.nodes = new HashSet<>();
        this.availableJobs = new ArrayDeque<>();
        this.activeJobs = new HashMap<>();
    }

    // For codec use
    private DeliveryNetwork(@Nonnull UUID id, ResourceKey<Level> dim, Set<BlockPos> nodes) {
        this.id = id;
        this.dim = dim;
        this.nodes = nodes;
        this.availableJobs = new ArrayDeque<>();
        this.activeJobs = new HashMap<>();
    }

    public void tick(ServerLevel level) {
        if (level.getGameTime() % 20 == 1) {
            verifyActiveJobs(level);
            updateJobs(level);
//            PackageCouriers.LOGGER.debug("jobs" + availableJobs);
        }
    }

    @SuppressWarnings("unused")
    private void verifyActiveJobs(ServerLevel level) {
        activeJobs.entrySet().removeIf(entry ->
                !(entry.getValue() instanceof Entity entity && entity.isAlive())
                || entry.getValue().getCurrentJob() != entry.getKey());
    }

    private void updateJobs(ServerLevel level) {
        Set<BlockPos> reserved = new HashSet<>();
        for (Job job : activeJobs.keySet()) {
            reserved.add(job.from);
            reserved.add(job.to);
        }

        Map<BlockPos, String> destinations = new HashMap<>();
        Map<BlockPos, ItemStack> sources = new HashMap<>();

        Iterator<BlockPos> posIterator = this.nodes.iterator();
        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            if (!isValidNode(level, pos)) {
                posIterator.remove();
                continue;
            }

            if (reserved.contains(pos)) continue;

            if (!(level.getBlockEntity(pos) instanceof DepotBlockEntity depot)) continue;

            // TODO: address depot behaviour
            String address = getDepotAddress(level, pos);
            if (address != null && !address.isBlank()) {
                if (depot.getHeldItem().isEmpty()) {
                    destinations.put(pos, address);
                }
            } else {
                ItemStack stack = depot.getHeldItem();
                if (PackageItem.isPackage(stack)) {
                    String packageAddress =  PackageItem.getAddress(stack);
                    if (packageAddress != null && !packageAddress.isBlank()) {
                        sources.put(pos, stack);
                    }
                }
            }
        }

        this.availableJobs.clear();
        for (Map.Entry<BlockPos, ItemStack> entry : sources.entrySet()) {
            int priority = -100000;
            BlockPos targetDest = null;
            for (Map.Entry<BlockPos, String> destEntry : destinations.entrySet()) {
                if (PackageItem.matchAddress(entry.getValue(), destEntry.getValue())) {
                    int currentPriority = getAddressPriority(destEntry.getValue());
                    if (currentPriority > priority) {
                        targetDest = destEntry.getKey();
                        priority = currentPriority;
                    }
                }
            }
            if (targetDest != null) {
                this.availableJobs.add(new Job(entry.getKey(), targetDest));
                destinations.remove(targetDest);
            }
        }
    }

    private static int getAddressPriority(String address) {
        return (int) -address.chars().filter(c -> c == '*').count();
    }

    private static @Nullable String getDepotAddress(Level level, BlockPos pos) {
        for (Direction side : Iterate.directions) {
            String address = getSign(level, pos, side);
            if (address == null || address.isBlank())
                continue;
            return address;
        }
        return null;
    }

    private static String getSign(Level level, BlockPos pos, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos.relative(side));
        if (!(blockEntity instanceof SignBlockEntity sign))
            return null;
        for (boolean front : Iterate.trueAndFalse) {
            SignText text = sign.getText(front);
            String address = "";
            for (Component component : text.getMessages(false)) {
                String string = component.getString();
                if (!string.isBlank())
                    address += string.trim() + " ";
            }
            if (!address.isBlank())
                return address.trim();
        }
        return null;
    }

    public boolean hasNode(BlockPos pos) {
        return this.nodes.contains(pos);
    }

    public void addNode(BlockPos pos) {
        this.nodes.add(pos);
    }

    public void removeNode(BlockPos pos) {
        this.nodes.remove(pos);
    }

    public List<BlockPos> getAllNodes() {
        return this.nodes.stream().toList();
    }

    public static boolean isValidNode(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof DepotBlock;
    }

    @Nonnull
    public UUID getId() {
        return this.id;
    }

    public ResourceKey<Level> getDimension() {
        return this.dim;
    }

    private Set<BlockPos> getNodes() {
        return this.nodes;
    }

    public @Nullable DeliveryNetwork.Job getJob(CourierMob courier) {
        if (!availableJobs.isEmpty()) {
            Job job = availableJobs.poll();
            activeJobs.put(job, courier);
            return job;
        }
        return null;
    }

    public record Job(BlockPos from, BlockPos to) { }
}
