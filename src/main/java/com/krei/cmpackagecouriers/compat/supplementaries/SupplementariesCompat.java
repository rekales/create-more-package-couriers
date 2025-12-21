package com.krei.cmpackagecouriers.compat.supplementaries;

import com.krei.cmpackagecouriers.PackageCouriers;
import net.mehvahdjukaar.supplementaries.api.neoforge.RegisterFireBehaviorsEvent;
import net.neoforged.neoforge.common.NeoForge;

public class SupplementariesCompat {
	public static void init() {
		NeoForge.EVENT_BUS.addListener(SupplementariesCompat::registerFireItemBehaviours);
	}

	public static void registerFireItemBehaviours(RegisterFireBehaviorsEvent event) {
		event.registerCannonBehavior(PackageCouriers.CARDBOARD_PLANE_ITEM, new CannonPlaneLaunch());
	}
}
