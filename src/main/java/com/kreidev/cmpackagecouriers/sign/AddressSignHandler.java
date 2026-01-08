package com.kreidev.cmpackagecouriers.sign;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.kreidev.cmpackagecouriers.marker.AddressMarkerHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;

//@EventBusSubscriber(modid= PackageCouriers.MOD_ID)
public class AddressSignHandler {



//    @SubscribeEvent
//    public static void serverTick(ServerTickEvent.Post event) {
//        Iterator<Map.Entry<AddressMarkerHandler.MarkerTarget, AddressMarkerHandler.MarkerTarget>> iterator = markerMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<AddressMarkerHandler.MarkerTarget, AddressMarkerHandler.MarkerTarget> entry = iterator.next();
//            AddressMarkerHandler.MarkerTarget marker = entry.getValue();
//
//            if (marker.tickAndCheckTimeout()) {
//                iterator.remove();
////                PackageCouriers.LOGGER.debug("Removed: " + marker);
//            }
//        }
//    }
//
//    public static class SignTarget {
//
//    }

}
