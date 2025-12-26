package com.kreidev.cmpackagecouriers.compat.curios;

import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterReg;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * Handles registration of all Curios renderers for the mod.
 * Similar to Create's CuriosRenderers class.
 */
@OnlyIn(Dist.CLIENT)
public class CuriosRenderers {

    /**
     * Registers all Curios item renderers for this mod.
     * This method should be called during client setup.
     */
    public static void register() {
        // Register the Location Transmitter renderer
        CuriosRendererRegistry.register(
            LocationTransmitterReg.LOCATION_TRANSMITTER.get(), 
            LocationTransmitterRenderer::new
        );
        
        // Add more renderer registrations here as needed for future curios items
    }
}