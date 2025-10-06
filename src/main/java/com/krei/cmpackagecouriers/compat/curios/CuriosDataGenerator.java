package com.krei.cmpackagecouriers.compat.curios;

import java.util.concurrent.CompletableFuture;

import static com.krei.cmpackagecouriers.PackageCouriers.MODID;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import top.theillusivec4.curios.api.CuriosDataProvider;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CuriosDataGenerator extends CuriosDataProvider {
	public CuriosDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
		super(MODID, output, fileHelper, registries);
	}

	@Override
	public void generate(Provider registries, ExistingFileHelper fileHelper) {
		// Create the belt slot for players
		createEntities("players")
			.addPlayer()
			.addSlots("belt");
	}
}
