package com.kreidev.cmpackagecouriers.courier;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.kreidev.cmpackagecouriers.courier.entity.*;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetworkManager;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetworkNodesPacket;
import com.mojang.brigadier.Command;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.commands.Commands;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.kreidev.cmpackagecouriers.PackageCouriers.*;

@SuppressWarnings("unused")
public class CourierNetworkReg {

    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister
            .create(BuiltInRegistries.MEMORY_MODULE_TYPE, PackageCouriers.MOD_ID);

    public static final Supplier<MemoryModuleType<DeliveryNetwork.Job>> DELIVERY_JOB_MEM = MEMORY_MODULE_TYPES
            .register("delivery_job", () -> new MemoryModuleType<>(Optional.of(DeliveryNetwork.Job.CODEC.codec())));

    public static final Supplier<DataComponentType<UUID>> DELIVERY_NETWORK_ID = DATA_COMPONENTS
            .registerComponentType("delivery_network_id", builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    public static final EntityEntry<CourierAllayEntity> COURIER_ALLAY_ENTITY = REGISTRATE
            .entity("courier_allay", CourierAllayEntity::new, MobCategory.CREATURE)
            .properties(p -> p
                    .sized(0.35F, 0.6F)
                    .eyeHeight(0.36F)
                    .ridingOffset(0.04F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
            )
//            .attributes(Allay::createAttributes)
//            .renderer(() -> CourierAllayRenderer::new)
            .register();

    public static final ItemEntry<WaybillItem> WAYBILL_ITEM = REGISTRATE
            .item("waybill", WaybillItem::new)
            .properties(p -> p.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
            .register();

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CourierNetworkReg::clientInit);
        modEventBus.addListener(CourierNetworkReg::registerAttributes);
        modEventBus.addListener(CourierNetworkReg::registerPacketHandler);
        NeoForge.EVENT_BUS.addListener(CourierNetworkReg::registerCommands);
        NeoForge.EVENT_BUS.addListener(DeliveryNetworkManager::onServerStarting);
        NeoForge.EVENT_BUS.addListener(DeliveryNetworkManager::serverTick);
        NeoForge.EVENT_BUS.addListener(CourierAllayEntity::tryConvertToCourier);
        MEMORY_MODULE_TYPES.register(modEventBus);
    }

    private static void clientInit(final FMLClientSetupEvent event) {
        CourierAllayRenderer.init();
        EntityRenderers.register(COURIER_ALLAY_ENTITY.get(), CourierAllayRenderer::new);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(COURIER_ALLAY_ENTITY.get(), Allay.createAttributes().build());
    }

    private static void registerPacketHandler(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");
        registrar.playToClient(
                DeliveryNetworkNodesPacket.TYPE,
                DeliveryNetworkNodesPacket.STREAM_CODEC,
                DeliveryNetworkNodesPacket::handle
        );
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("waybill")
                        .then(Commands.literal("init")
                                .executes(context -> {
                                    ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
                                    if (stack.getItem() instanceof WaybillItem) {
                                        WaybillItem.setAssignedNetwork(stack, DeliveryNetworkManager.createNetwork(context.getSource().getLevel()));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("remove")
                                .executes(context -> {
                                    ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
                                    if (stack.getItem() instanceof WaybillItem) {
                                        DeliveryNetwork network = WaybillItem.getAssignedNetwork(stack);
                                        if (network != null) {
                                            DeliveryNetworkManager.removeNetwork(network.getId());
                                            WaybillItem.setAssignedNetwork(stack, null);
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
        );
    }

}
