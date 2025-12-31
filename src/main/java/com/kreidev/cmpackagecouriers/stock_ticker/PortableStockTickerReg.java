package com.kreidev.cmpackagecouriers.stock_ticker;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.MenuEntry;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static com.kreidev.cmpackagecouriers.PackageCouriers.*;

// Shamelessly copied from Create: Mobile Packages
public class PortableStockTickerReg {

    public static final ItemEntry<PortableStockTicker> PORTABLE_STOCK_TICKER =
            REGISTRATE.item("portable_stock_ticker", PortableStockTicker::new)
                    .register();

    public static final DataComponentType<CustomData> CMP_FREQ = register(
            "cmp_freq",
            builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC)
    );

    public static final DataComponentType<String> ADDRESS_TAG = register(
            "address_tag",
            builder -> builder.persistent(Codec.STRING)
    );

    public static final DataComponentType<List<ItemStack>> CATEGORIES = register(
            "categories",
            builder -> builder
                    .persistent(ItemStack.CODEC.listOf())
    );

    public static final DataComponentType<Map<UUID, List<Integer>>> HIDDEN_CATEGORIES = register(
            "hidden_categories",
            builder -> builder
                    .persistent(Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT.listOf()))
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    public static final MenuEntry<PortableStockTickerMenu> PORTABLE_STOCK_TICKER_MENU =
            REGISTRATE.menu(
                    "portable_stock_ticker_menu",
                    (MenuType, containerId, playerInventory) -> new PortableStockTickerMenu(containerId, playerInventory),
                    () -> PortableStockTickerScreen::new
            ).register();


    public enum PortableStockTickerPackets implements BasePacketPayload.PacketTypeProvider {
        // Client to Server
        LOGISTICS_PACKAGE_REQUEST(SendPackage.class, SendPackage.STREAM_CODEC),
        REQUEST_STOCK_UPDATE(RequestStockUpdate.class, RequestStockUpdate.STREAM_CODEC),
        HIDDEN_CATEGORIES(HiddenCategoriesPacket.class, HiddenCategoriesPacket.STREAM_CODEC),
        OPEN_PORTABLE_STOCK_TICKER(OpenPortableStockTicker.class, OpenPortableStockTicker.STREAM_CODEC),

        // Server to Client
        BIG_ITEM_STACK_LIST(GenericStackListPacket.class, GenericStackListPacket.STREAM_CODEC);


        private final CatnipPacketRegistry.PacketType<?> type;

        <T extends BasePacketPayload> PortableStockTickerPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
            String name = this.name().toLowerCase(Locale.ROOT);
            this.type = new CatnipPacketRegistry.PacketType<>(
                    new CustomPacketPayload.Type<>(resLoc(name)),
                    clazz, codec
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
            return (CustomPacketPayload.Type<T>) this.type.type();
        }

        public static void register() {
            CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(PackageCouriers.MOD_ID, 1);
            for (PortableStockTickerPackets packet : PortableStockTickerPackets.values()) {
                packetRegistry.registerPacket(packet.type);
            }
            packetRegistry.registerAllPackets();
        }
    }

    public static void register() {
        PortableStockTickerPackets.register();
    }

}
