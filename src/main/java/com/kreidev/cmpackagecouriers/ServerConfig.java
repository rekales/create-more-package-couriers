package com.kreidev.cmpackagecouriers;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@SuppressWarnings("unused")
public class ServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue PLANE_LOCATION_TARGETS = BUILDER
            .comment("enables targeting depots with cardboard planes")
            .define("enablePlaneLocationLogistics", true);

    private static final ForgeConfigSpec.BooleanValue PLANE_PLAYER_TARGETS = BUILDER
            .comment("enables targeting players with cardboard planes")
            .define("enablePlanePlayerLogistics", true);

    private static final ForgeConfigSpec.BooleanValue PLANE_CROSS_DIM_TRANSPORT = BUILDER
            .comment("allows/disables planes for transporting items to another dimension")
            .define("planeCrossDimTransport", true);

    private static final ForgeConfigSpec.BooleanValue ANGLE_BRACKET_SHOP_ADDRESS_REPLACEMENT = BUILDER
            .comment("enables integration with Create's Shop system that rewrites \"<>\" address to the ordering player's nick)")
            .define("enableShopAddressReplacement", true);  // TODO: rename to angle bracket replacement in next major version

    private static final ForgeConfigSpec.BooleanValue AT_SIGN_SHOP_ADDRESS_REPLACEMENT = BUILDER
            .comment("enables integration with Create's Shop system that rewrites \"@player\" addresses to the ordering player's nick)")
            .define("atSignShopAddressReplacement", false);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean planeLocationTargets;
    public static boolean planePlayerTargets;
    public static boolean planeCrossDimTransport;
    public static boolean angleBracketAddressReplacement;
    public static boolean atSignAddressReplacement;

    private static void updateConfigs() {
        planeLocationTargets = PLANE_LOCATION_TARGETS.get();
        planePlayerTargets = PLANE_PLAYER_TARGETS.get();
        planeCrossDimTransport = PLANE_CROSS_DIM_TRANSPORT.get();
        angleBracketAddressReplacement = ANGLE_BRACKET_SHOP_ADDRESS_REPLACEMENT.get();
        atSignAddressReplacement = AT_SIGN_SHOP_ADDRESS_REPLACEMENT.get();
    }

    static void onLoad(final ModConfigEvent.Loading event) {
        updateConfigs();
    }

    static void onReload(final ModConfigEvent.Reloading event) {
        updateConfigs();
    }
}