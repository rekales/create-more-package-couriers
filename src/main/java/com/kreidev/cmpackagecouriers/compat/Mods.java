package com.kreidev.cmpackagecouriers.compat;

import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

import java.util.Optional;
import java.util.function.Supplier;

public enum Mods {
    JEI,
    CREATE_MOBILE_PACKAGES,
    CURIOS;

    // from com/simibubi/create/compat/Mods.java

    private final String id;

    Mods() {
        id = Lang.asId(name());
    }

    public String id() {
        return id;
    }

    public ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(id, path);
    }

    public Block getBlock(String id) {
        return BuiltInRegistries.BLOCK.get(rl(id));
    }

    public Item getItem(String id) {
        return BuiltInRegistries.ITEM.get(rl(id));
    }

    public boolean contains(ItemLike entry) {
        if (!isLoaded())
            return false;
        Item asItem = entry.asItem();
        return asItem != null && RegisteredObjectsHelper.getKeyOrThrow(asItem)
                .getNamespace()
                .equals(id);
    }

    /**
     * @return a boolean of whether the mod is loaded or not based on mod id
     */
    public boolean isLoaded() {
        return ModList.get().isLoaded(id);
    }

    /**
     * Simple hook to run code if a mod is installed
     * @param toRun will be run only if the mod is loaded
     * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
     */
    public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
        if (isLoaded())
            return Optional.of(toRun.get().get());
        return Optional.empty();
    }

    /**
     * Simple hook to execute code if a mod is installed
     * @param toExecute will be executed only if the mod is loaded
     */
    public void executeIfInstalled(Supplier<Runnable> toExecute) {
        if (isLoaded()) {
            toExecute.get().run();
        }
    }

    // /**
    //  * Initialize all mod compatibility integrations
    //  * Call this from the main mod constructor to set up all optional mod support
    //  */
    // public static void initializeCompatibility(IEventBus modEventBus, Logger logger) {
    //     // Initialize Curios integration
    //     CURIOS.executeIfInstalled(() -> () -> {
    //         try {
    //             Class.forName("com.krei.cmpackagecouriers.compat.curios.CuriosIntegration")
    //                 .getMethod("initialize")
    //                 .invoke(null);
    //             logger.info("Curios integration initialized successfully");
    //         } catch (Exception e) {
    //             logger.warn("Failed to initialize Curios integration", e);
    //         }
    //     });

    //     // Register data generation for Curios
    //     modEventBus.addListener((GatherDataEvent event) -> {
    //         CURIOS.executeIfInstalled(() -> () -> {
    //             try {
    //                 Class<?> dataGenClass = Class.forName("com.krei.cmpackagecouriers.compat.curios.CuriosDataGenerator");
    //                 Object dataProvider = dataGenClass.getDeclaredConstructor(
    //                     net.minecraft.data.PackOutput.class,
    //                     java.util.concurrent.CompletableFuture.class,
    //                     net.neoforged.neoforge.common.data.ExistingFileHelper.class
    //                 ).newInstance(
    //                     event.getGenerator().getPackOutput(),
    //                     event.getLookupProvider(),
    //                     event.getExistingFileHelper()
    //                 );
    //                 event.getGenerator().addProvider(event.includeServer(), (net.minecraft.data.DataProvider) dataProvider);
    //                 logger.info("Curios data generation registered successfully");
    //             } catch (Exception e) {
    //                 logger.warn("Failed to register Curios data generation", e);
    //             }
    //         });
    //     });

    //     // Add other mod compatibility here in the future
    //     // JEI.executeIfInstalled(() -> () -> { ... });
    // }
}
