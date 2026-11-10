package com.kreidev.cmpackagecouriers.courier.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.kreidev.cmpackagecouriers.PackageDestination;
import com.kreidev.cmpackagecouriers.PlayerPackageDest;
import com.kreidev.cmpackagecouriers.SmartBlockPackageDest;
import com.kreidev.cmpackagecouriers.blockentity.PackageDestinationBehaviour;
import com.kreidev.cmpackagecouriers.courier.CourierMob;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

// TODO: add method for checking possible destinations from an address
// for when the current destination is full or package was given by the player
public class DeliveryNetwork {

    public static final MapCodec<DeliveryNetwork> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("ID").forGetter(DeliveryNetwork::getId),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("Dim").forGetter(DeliveryNetwork::getDimension),
            BlockPos.CODEC.listOf().<Set<BlockPos>>xmap(HashSet::new, ArrayList::new).fieldOf("Nodes").forGetter(DeliveryNetwork::getNodes)
            ).apply(instance, DeliveryNetwork::new));

    private static final int PLAYER_SEARCH_RADIUS = 16;  // TODO: config
    private static final int COURIER_TRACKING_TIMEOUT = 10;

    @Nonnull private final UUID id;
    private final ResourceKey<Level> dim;
    private final Set<BlockPos> nodes;

    private final Queue<Job> availableJobs;
    private final Map<Job, CourierMob> activeJobs;
    private final Map<CourierMob, Long> couriers;  // Courier : Level timestamp last check in

    private final Map<SectionPos, Set<BlockPos>> cellSearchMap = new HashMap<>();  // Spatial hash : Nodes covering that hash
    private final Set<Player> nearbyPlayers = new HashSet<>();
    private final BiMap<Player, CourierMob> followedPlayers = HashBiMap.create();  // Player : Assigned mob to be nearby

    // NOTE: optimizations - hotspot positions

    public DeliveryNetwork(ServerLevel level) {
        this.id = UUID.randomUUID();
        this.dim = level.dimension();
        this.nodes = new HashSet<>();
        this.availableJobs = new ArrayDeque<>();
        this.activeJobs = new HashMap<>();
        this.couriers = new HashMap<>();
    }

    // For codec use
    private DeliveryNetwork(@Nonnull UUID id, ResourceKey<Level> dim, Set<BlockPos> nodes) {
        this.id = id;
        this.dim = dim;
        this.nodes = nodes;
        this.availableJobs = new ArrayDeque<>();
        this.activeJobs = new HashMap<>();
        this.couriers = new HashMap<>();
        this.refreshCellMap();
    }

    public void tick(ServerLevel level) {
        if (level.getGameTime() % 5 == 1) {
            updateCouriers(level);
        }

        if (level.getGameTime() % 20 == 1) {  // remember to match equal check with above
            updateJobs(level);
            updateNearbyPlayers(level);
//            PackageCouriers.LOGGER.debug("jobs" + availableJobs);
//            PackageCouriers.LOGGER.debug("" + cellSearchMap);
            PackageCouriers.LOGGER.debug("" + this.nearbyPlayers);
        }
    }

    private void updateCouriers(ServerLevel level) {
        long gameTime = level.getGameTime();
        this.activeJobs.clear();
        for (Iterator<Map.Entry<CourierMob, Long>> it = this.couriers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<CourierMob, Long> e = it.next();
            if (gameTime - e.getValue() > COURIER_TRACKING_TIMEOUT) {
                it.remove();
                continue;
            }

            // Should be fine to restrict non entities right?
            if (!(e.getKey() instanceof Entity entity && entity.isAlive() && !entity.isRemoved())) {
                it.remove();
                continue;
            }

            Job job = e.getKey().getCurrentJob();
            if (job != null) {
                this.activeJobs.put(job, e.getKey());
            }
        }
    }

    private void updateJobs(ServerLevel level) {
        Set<BlockPos> reserved = new HashSet<>();
        for (Job job : activeJobs.keySet()) {
            reserved.add(job.from);
            if (job.to.getType() == PackageDestination.Type.SMART_BLOCK_ENTITY) {
                reserved.add(BlockPos.containing(job.to.getPos()));
            }
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

            // Destination Check
            // Should destinations also be suppliers if address is not matched?
            if (level.getBlockEntity(pos) instanceof SmartBlockEntity be) {
                PackageDestinationBehaviour behaviour = be.getBehaviour(PackageDestinationBehaviour.TYPE);
                if (behaviour != null) {
                    String address = behaviour.getAddress();
                    if (address != null && !address.isBlank() && behaviour.hasSpace()) {
                        destinations.put(pos, address);
                        continue;
                    }
                }
            }

            // Supplier Check
            if (level.getBlockEntity(pos) instanceof DepotBlockEntity depot) {
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

            // Special "@Address" interaction
            String packageAddress =  PackageItem.getAddress(entry.getValue());
            int atIndex = packageAddress.indexOf('@');  // TODO: also allow player targets even without '@'
            if (atIndex != -1) {  // TODO: ponder info about explicit @address player targets
                packageAddress = packageAddress.substring(atIndex + 1);
                for (Player player : this.nearbyPlayers) {
                    if (PackageItem.matchAddress(player.getName().getString(), packageAddress)) {
                        this.availableJobs.add(new Job(entry.getKey(), new PlayerPackageDest(player)));
                    }
                }
            } else {
                int priority = -100000;
                SmartBlockEntity targetDest = null;
                for (Map.Entry<BlockPos, String> destEntry : destinations.entrySet()) {
                    if (!(level.getBlockEntity(destEntry.getKey()) instanceof SmartBlockEntity be)) continue;
                    if (!PackageItem.matchAddress(entry.getValue(), destEntry.getValue())) continue;

                    int currentPriority = PackageDestination.getAddressPriority(destEntry.getValue());
                    if (currentPriority > priority) {
                        targetDest = be;
                        priority = currentPriority;
                    }
                }
                if (targetDest != null) {
                    this.availableJobs.add(new Job(entry.getKey(), new SmartBlockPackageDest(level, targetDest)));
                    destinations.remove(targetDest.getBlockPos());
                }
            }
        }
    }

    private void updateNearbyPlayers(ServerLevel level) {
        for (Player player : level.players()) {
            if (player.isDeadOrDying() || player.isRemoved()) {
                this.nearbyPlayers.remove(player);
                this.followedPlayers.remove(player);
                continue;
            }

            if (this.isNearbyNetwork(player.position())) {
                this.nearbyPlayers.add(player);
            } else {
                this.nearbyPlayers.remove(player);
                this.followedPlayers.remove(player);
            }
        }
    }

    /**
     * called by courier mobs to tell the network it exists.
     */
    public void checkIn(ServerLevel level, CourierMob courier) {
        if (courier.getDeliveryNetwork() != this) return;
        this.couriers.put(courier, level.getGameTime());
    }

    public boolean hasNode(BlockPos pos) {
        return this.nodes.contains(pos);
    }

    public void addNode(BlockPos pos) {
        this.nodes.add(pos);

        getSectionsInRadius(pos, PLAYER_SEARCH_RADIUS).forEach(sec ->
                this.cellSearchMap.computeIfAbsent(sec, k -> new HashSet<>()).add(pos)
        );
    }

    public void removeNode(BlockPos pos) {
        this.nodes.remove(pos);

        getSectionsInRadius(pos, PLAYER_SEARCH_RADIUS).forEach(sec -> {
            Set<BlockPos> set = this.cellSearchMap.get(sec);
            if (set == null) return;
            set.remove(pos);
            if (set.isEmpty()) this.cellSearchMap.remove(sec);
        });
    }

    public List<BlockPos> getAllNodes() {
        return this.nodes.stream().toList();
    }

    public void refreshCellMap() {
        this.cellSearchMap.clear();
        for (BlockPos pos : this.nodes) {
            getSectionsInRadius(pos, PLAYER_SEARCH_RADIUS).forEach(sec ->
                    this.cellSearchMap.computeIfAbsent(sec, k -> new HashSet<>()).add(pos)
            );
        }
    }

    public boolean isNearbyNetwork(Vec3 vec) {
        SectionPos sec = SectionPos.of(BlockPos.containing(vec));
        Set<BlockPos> candidates = this.cellSearchMap.get(sec);
        if (candidates == null) return false;

        for (BlockPos node : candidates) {
            double dx = vec.x - (node.getX() + 0.5);
            double dy = vec.y - (node.getY() + 0.5);
            double dz = vec.z - (node.getZ() + 0.5);
            if (dx*dx + dy*dy + dz*dz <= PLAYER_SEARCH_RADIUS * PLAYER_SEARCH_RADIUS) return true;
        }
        return false;
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

    public Set<Player> getNearbyPlayers() {
        return ImmutableSet.copyOf(this.nearbyPlayers);
    }

    public Set<Player> getUnfollowedPlayersNearby() {
        return Sets.difference(this.nearbyPlayers, this.followedPlayers.keySet());
    }

    public @Nullable Player getFollowingPlayer(CourierMob courier) {
        return this.followedPlayers.inverse().get(courier);
    }

    public void setFollowingPlayer(Player player, CourierMob courier) {
        if (!this.nearbyPlayers.contains(player)) return;
        this.followedPlayers.put(player, courier);
    }

    public void stopFollowingPlayer(CourierMob courier) {
        this.followedPlayers.inverse().remove(courier);
    }

    public @Nullable DeliveryNetwork.Job getJob(CourierMob courier) {
        if (!this.availableJobs.isEmpty()) {
            Job job = this.availableJobs.poll();
            this.activeJobs.put(job, courier);  // TODO: or maybe just let the courier remind the network themselves?
            return job;
        }
        return null;
    }

    public record Job(@Nullable BlockPos from, @Nonnull PackageDestination to) {
        public static final MapCodec<Job> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockPos.CODEC.optionalFieldOf("From").forGetter(t -> Optional.ofNullable(t.from())),
                PackageDestination.CODEC.fieldOf("To").forGetter(Job::to)
        ).apply(instance, (from, to) -> new Job(from.orElse(null), to)));
    }

    public static boolean isValidNode(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SmartBlockEntity be) {  // Destination
            PackageDestinationBehaviour behaviour = be.getBehaviour(PackageDestinationBehaviour.TYPE);
            if (behaviour != null) return true;
        }

        return level.getBlockState(pos).getBlock() instanceof DepotBlock;  // Supplier, depot only for now.
    }

    public static List<SectionPos> getSectionsInRadius(BlockPos pos, int radius) {
        SectionPos center = SectionPos.of(pos);
        int cellRadius = (int) Math.ceil((double) radius / SectionPos.SECTION_SIZE);
        List<SectionPos> sections = new ArrayList<>();

        for (int dx = -cellRadius; dx <= cellRadius; dx++) {
            for (int dy = -cellRadius; dy <= cellRadius; dy++) {
                for (int dz = -cellRadius; dz <= cellRadius; dz++) {
                    sections.add(SectionPos.of(center.x() + dx, center.y() + dy, center.z() + dz));
                }
            }
        }

        return sections;
    }
}
