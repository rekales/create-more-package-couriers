package com.kreidev.cmpackagecouriers.plane;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.server.ServerStartingEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.kreidev.cmpackagecouriers.PackageCouriers.LOGGER;

public class CardboardPlaneSavedData extends SavedData {

    public static final Codec<List<CardboardPlane>> CODEC =
            Codec.list(CardboardPlane.CODEC.codec());

    public List<Pair<CardboardPlane, CardboardPlaneEntity>> pairedPlanes;

    public CardboardPlaneSavedData() {
        super();
        this.pairedPlanes = new ArrayList<>();
    }

    public CardboardPlaneSavedData(CompoundTag tag) {
        if (tag.contains("CardboardPlanes")) {
            DataResult<List<CardboardPlane>> result = CODEC.parse(NbtOps.INSTANCE, tag.get("CardboardPlanes"));
            List<CardboardPlane> planeList = new ArrayList<>(
                    result.resultOrPartial(error -> LOGGER.error("Cardboard plane data was not loaded \n" + error))
                            .orElse(new ArrayList<>())
            );

            this.pairedPlanes = planeList.stream()
                    .map(plane -> Pair.of(plane, (CardboardPlaneEntity) null))
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            this.pairedPlanes = new ArrayList<>();
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        List<CardboardPlane> planeList = this.pairedPlanes.stream()
                .map(Pair::getFirst)
                .toList();

        DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, planeList);
        result.resultOrPartial(error->LOGGER.error("Cardboard plane data was not saved \n"+error))
                .ifPresent(nbt -> tag.put("CardboardPlanes", nbt));
        return tag;
    }

    public static void onServerStarting(ServerStartingEvent event) {
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if(level != null && !level.isClientSide()) {
            CardboardPlaneManager.INSTANCE = level.getDataStorage().computeIfAbsent(
                    CardboardPlaneSavedData::new, CardboardPlaneSavedData::new, "cardboard_planes"
            );
        }
    }
}
