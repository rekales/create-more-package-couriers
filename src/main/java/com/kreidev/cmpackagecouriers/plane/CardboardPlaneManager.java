package com.kreidev.cmpackagecouriers.plane;

import com.kreidev.cmpackagecouriers.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.kreidev.cmpackagecouriers.PackageCouriers.LOGGER;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@EventBusSubscriber(modid= PackageCouriers.MOD_ID)
public class CardboardPlaneManager extends SavedData {

    private static final String PLANE_LIST_TAG_KEY = "CardboardPlanes";
    public static final Codec<List<CardboardPlane>> CODEC = Codec.list(CardboardPlane.CODEC.codec());
    public static final int LIFESPAN_TICKS = 400;

    public static CardboardPlaneManager INSTANCE;

    public List<Pair<CardboardPlane, Optional<CardboardPlaneEntity>>> pairedPlanes;

    public CardboardPlaneManager() {
        super();
        this.pairedPlanes = new ArrayList<>();
    }

    public CardboardPlaneManager(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains(PLANE_LIST_TAG_KEY)) {
            DataResult<List<CardboardPlane>> result = CODEC.parse(NbtOps.INSTANCE, tag.get(PLANE_LIST_TAG_KEY));
            List<CardboardPlane> planeList = new ArrayList<>(
                    result.resultOrPartial(error -> LOGGER.error("Cardboard plane data was not loaded \n" + error))
                            .orElse(new ArrayList<>())
            );

            this.pairedPlanes = planeList.stream()
                    .map(plane -> Pair.of(plane, Optional.<CardboardPlaneEntity>empty()))
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            this.pairedPlanes = new ArrayList<>();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        List<CardboardPlane> planeList = this.pairedPlanes.stream()
                .map(Pair::getFirst)
                .toList();

        DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, planeList);
        result.resultOrPartial(error->LOGGER.error("Cardboard plane data was not saved \n"+error))
                .ifPresent(nbt -> tag.put(PLANE_LIST_TAG_KEY, nbt));
        return tag;
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Pre event) {
        if (INSTANCE == null) return;
        if (!INSTANCE.pairedPlanes.isEmpty()) INSTANCE.setDirty();

        MinecraftServer server = event.getServer();

        for (Iterator<Pair<CardboardPlane, Optional<CardboardPlaneEntity>>> iterator = INSTANCE.pairedPlanes.iterator(); iterator.hasNext();) {
            Pair<CardboardPlane, Optional<CardboardPlaneEntity>> pair = iterator.next();
            CardboardPlane plane = pair.getFirst();
            Optional<CardboardPlaneEntity> entity = pair.getSecond();

            plane.tick(server);

            if (plane.isForRemoval()) {
                entity.ifPresent(e -> e.remove(Entity.RemovalReason.DISCARDED));
                iterator.remove();
                continue;
            }

            if (plane.hasTeleported()) {
                entity.ifPresent(e -> e.remove(Entity.RemovalReason.DISCARDED));
                pair.setSecond(Optional.empty());
                plane.setTeleported(false);
            }

            // Adding a plane entity for rendering
            ServerLevel level = server.getLevel(plane.getCurrentDim());
            if (Utils.isChunkTicking(level, plane.getPos())) {
                if (entity.isEmpty()) {
                    CardboardPlaneEntity planeEntity = new CardboardPlaneEntity(level, plane);
                    pair.setSecond(Optional.of(planeEntity));
                    level.addFreshEntity(planeEntity);
                }
            } else if (entity.isPresent()) {
                entity.ifPresent(e -> e.remove(Entity.RemovalReason.DISCARDED));
                pair.setSecond(Optional.empty());
            }
        }
    }

    /**
     * @return false if plane failed to launch
     */
    public static boolean addPlane(ServerLevel level, Vec3 pos, float yaw, float pitch, ItemStack box) {
        return addPlane(level, pos, yaw, pitch, box, false);
    }

    /**
     * @return false if plane failed to launch
     */
    public static boolean addPlane(ServerLevel level, Vec3 pos, float yaw, float pitch, ItemStack box, boolean unpack) {
        if (!PackageItem.isPackage(box)) return false;
        MinecraftServer server = level.getServer();

        String address = PackageItem.getAddress(box);
        // TODO: ponder info if disabled cross dim transport
        PackageDestination dest = findDestination(level, address, ServerConfig.planeCrossDimTransport,
                ServerConfig.planePlayerTargets, ServerConfig.planeLocationTargets);
        if (dest == null) return false;

        ServerLevel targetLevel = server.getLevel(dest.getDim());
        if (targetLevel == null) return false;

        CardboardPlane plane = new CardboardPlane(level, dest, box);
        plane.setPos(pos);
        plane.setRot(pitch, yaw);
        plane.setUnpack(unpack);
        INSTANCE.pairedPlanes.add(Pair.of(plane, Optional.empty()));
        return true;
    }

    private static @Nullable PackageDestination findDestination(ServerLevel level, String address, boolean crossDim,
                                                                boolean playerTargets, boolean locationTargets) {
        // Special "<Address>" interaction
        int start = address.indexOf('<');
        int end = address.indexOf('>');
        if (start != -1 && end != -1 && end > start) {
            address = address.substring(start + 1, end);
        }

        // Special "@Address" interaction
        boolean playerOnly = false;
        int atIndex = address.indexOf('@');
        if (atIndex != -1) {
            address = address.substring(atIndex + 1);
            playerOnly = true;  // TODO: ponder info about explicit @address player targets
        }

        Map<PackageDestination, String> destinations = GlobalDestinationManager.findDestinations(level, address);

        if (playerOnly || !locationTargets) {
            destinations.entrySet().removeIf(entry -> entry.getKey().getType() != PackageDestination.Type.PLAYER);
        }
        if (!locationTargets) {
            destinations.entrySet().removeIf(entry -> entry.getKey().getType() == PackageDestination.Type.PLAYER);
        }
        if (!crossDim) {
            destinations.entrySet().removeIf(entry -> entry.getKey().getDim() != level.dimension());
        }

        return destinations.entrySet().stream()
                .min(Comparator
                        .comparingInt((Map.Entry<PackageDestination, String> e) -> e.getKey().getType() == PackageDestination.Type.PLAYER ? 0 : 1)
                        .thenComparingInt(e -> -PackageDestination.getAddressPriority(e.getValue())))
                .map(Map.Entry::getKey).orElse(null);
    }

    public static void onServerStarting(ServerStartingEvent event) {
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if(level != null && !level.isClientSide()) {
            CardboardPlaneManager.INSTANCE = level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                            CardboardPlaneManager::new, CardboardPlaneManager::new, null),
                    "cmpc_cardboard_planes"
            );
        }
    }

}
