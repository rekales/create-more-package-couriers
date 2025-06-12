package com.krei.cmpackagecouriers;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid=PackageCouriers.MODID)
public class ServerHandler {

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide())
            return;

//        PackageCouriers.LOGGER.debug(event.getPlayer().level().getGameTime()+"");
    }
}
