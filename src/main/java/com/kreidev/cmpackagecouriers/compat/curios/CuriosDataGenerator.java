package com.kreidev.cmpackagecouriers.compat.curios;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

import static com.kreidev.cmpackagecouriers.PackageCouriers.MOD_ID;

public class CuriosDataGenerator extends CuriosDataProvider {
	public CuriosDataGenerator(PackOutput output, CompletableFuture<Provider> registries, ExistingFileHelper fileHelper) {
		super(MOD_ID, output, fileHelper, registries);
	}

	@Override
	public void generate(Provider registries, ExistingFileHelper fileHelper) {
		// Create the belt slot for players
		createEntities("players")
			.addPlayer()
			.addSlots("belt");
	}
}
