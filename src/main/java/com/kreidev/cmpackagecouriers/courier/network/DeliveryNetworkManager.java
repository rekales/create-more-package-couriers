package com.kreidev.cmpackagecouriers.courier.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

import static com.kreidev.cmpackagecouriers.PackageCouriers.LOGGER;

// For holding and managing the singleton that contains all the delivery networks

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DeliveryNetworkManager extends SavedData {

    // TODO: make string constants for all nbt tags
    private static final String NETWORK_LIST_TAG_KEY = "DeliveryNetworks";
    private static final Codec<List<DeliveryNetwork>> CODEC =
            Codec.list(DeliveryNetwork.CODEC.codec());

    private static DeliveryNetworkManager INSTANCE;

    private final Map<UUID, DeliveryNetwork> networks;

    public DeliveryNetworkManager() {
        this.networks = new HashMap<>();
    }

    public DeliveryNetworkManager(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains(NETWORK_LIST_TAG_KEY)) {
            DataResult<List<DeliveryNetwork>> result = CODEC.parse(NbtOps.INSTANCE, tag.get(NETWORK_LIST_TAG_KEY));
            List<DeliveryNetwork> networkList = new ArrayList<>(
                    result.resultOrPartial(error -> LOGGER.error("Delivery network data was not loaded \n" + error))
                            .orElse(new ArrayList<>())
            );

            this.networks = networkList.stream()
                    .collect(Collectors.toMap(DeliveryNetwork::getId, network -> network));
        } else {
            this.networks = new HashMap<>();
        }
    }

    public static @Nullable DeliveryNetwork getNetwork(UUID id) {
        return INSTANCE.networks.get(id);
    }

    public static DeliveryNetwork createNetwork(ServerLevel level) {
        DeliveryNetwork network = new DeliveryNetwork(level);
        INSTANCE.networks.put(network.getId(), network);
        return network;
    }

    public static void removeNetwork(UUID id) {
        INSTANCE.networks.remove(id);
    }

    public static void clearNetworks() {
        INSTANCE.networks.clear();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        List<DeliveryNetwork> networkList = new ArrayList<>(this.networks.values());

        DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, networkList);
        result.resultOrPartial(error->LOGGER.error("Delivery network data was not saved \n"+error))
                .ifPresent(nbt -> tag.put(NETWORK_LIST_TAG_KEY, nbt));
        return tag;
    }

    public static void serverTick(ServerTickEvent.Pre event) {
        for (DeliveryNetwork network : INSTANCE.networks.values()) {
            ServerLevel level = event.getServer().getLevel(network.getDimension());
            if (level == null) continue;
            network.tick(level);
            INSTANCE.setDirty();
        }
    }

    public static void onServerStarting(ServerStartingEvent event) {
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level != null && !level.isClientSide()) {
            DeliveryNetworkManager.INSTANCE = level.getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(DeliveryNetworkManager::new, DeliveryNetworkManager::new, null),
                    "cmpc_delivery_networks"
            );
        }
    }
}
