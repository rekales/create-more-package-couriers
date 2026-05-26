package com.kreidev.cmpackagecouriers.courier;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@EventBusSubscriber(modid= PackageCouriers.MOD_ID)
public class DeliveryNetwork {



    private static final HashMap<UUID, DeliveryNetwork> NETWORKS = new HashMap<>();

    @Nonnull private final UUID id;
    private final List<BlockPos> nodes;
    private final ResourceKey<Level> dim;
    private final Queue<Job> availableJobs = new ArrayDeque<>();

    public DeliveryNetwork(ServerLevel level) {
        this.id = UUID.randomUUID();
        this.dim = level.dimension();
        this.nodes = new ArrayList<>();  // Maybe use HashSet or something
    }

    public void tick(ServerLevel level) {
        if (level.getGameTime() % 20 == 1) {
            List<BlockPos> destinations = new ArrayList<>();
            List<BlockPos> sources = new ArrayList<>();

            Iterator<BlockPos> posIterator = this.nodes.iterator();
            while (posIterator.hasNext()) {
                BlockPos pos = posIterator.next();
                if (!isValid(level, pos)) {
                    posIterator.remove();
                    continue;
                }

            }
        }

        drawNodeOutlines(level);
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

    public static boolean isValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof DepotBlock;
    }

    @Nonnull
    public UUID getId() {
        return id;
    }

    public @Nullable DeliveryNetwork.Job getJob(LivingEntity courier) {
        return null;
    }

    public record Job(BlockPos from, BlockPos to) { }


    public static @Nullable DeliveryNetwork getNetwork(UUID id) {
        return NETWORKS.get(id);
    }

    public static DeliveryNetwork createNetwork(ServerLevel level) {
        DeliveryNetwork network = new DeliveryNetwork(level);
        NETWORKS.put(network.id, network);
        return network;
    }

    public static void removeNetwork(UUID id) {
        NETWORKS.remove(id);
    }

    public static void clearNetworks() {
        NETWORKS.clear();
    }


    public void drawNodeOutlines(ServerLevel level) {
        for (BlockPos pos : this.nodes) {
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getShape(level, pos);
            if (!shape.isEmpty()) {
                Outliner.getInstance()
                        .showAABB(pos, shape.bounds().move(pos))
                        .colored(0x9ede73)
                        .lineWidth(0.0625F);
            }
        }
    }


    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Pre event) {
        for (DeliveryNetwork network : NETWORKS.values()) {
            ServerLevel level = event.getServer().getLevel(network.dim);
            if (level == null) continue;
            network.tick(level);
        }
    }
}
