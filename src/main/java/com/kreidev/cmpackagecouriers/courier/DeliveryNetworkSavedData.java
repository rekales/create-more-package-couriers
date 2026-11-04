package com.kreidev.cmpackagecouriers.courier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

import static com.kreidev.cmpackagecouriers.PackageCouriers.LOGGER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DeliveryNetworkSavedData extends SavedData {

    public static final Codec<List<DeliveryNetwork>> CODEC =
            Codec.list(DeliveryNetwork.CODEC.codec());

    // TODO: make string constants for all nbt tags
    public static final String NETWORK_LIST_TAG_KEY = "DeliveryNetworks";

    private final Map<UUID, DeliveryNetwork> networks;

    public DeliveryNetworkSavedData() {
        this.networks = new HashMap<>();
    }

    public DeliveryNetworkSavedData(CompoundTag tag, HolderLookup.Provider provider) {
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

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        List<DeliveryNetwork> networkList = new ArrayList<>(this.networks.values());

        DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, networkList);
        result.resultOrPartial(error->LOGGER.error("Delivery network data was not saved \n"+error))
                .ifPresent(nbt -> tag.put(NETWORK_LIST_TAG_KEY, nbt));
        return tag;
    }

    public static void onServerStarting(ServerStartingEvent event) {
//        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
//        if(level != null && !level.isClientSide()) {
//            CardboardPlaneManager.INSTANCE = level.getDataStorage().computeIfAbsent(
//                    new SavedData.Factory<>(CardboardPlaneSavedData::new, CardboardPlaneSavedData::new, null), "cardboard_planes"
//            );
//        }
    }
}
