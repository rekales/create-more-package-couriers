package com.krei.cmpackagecouriers.compat.supplementaries;

import com.krei.cmpackagecouriers.plane.CardboardPlaneItem;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CannonPlaneLaunch implements IFireItemBehavior {
	@Override
	public boolean fire(ItemStack stack, ServerLevel level, Vec3 origin, Vec3 direction, float power, int inaccuracy, @Nullable Player player) {
		var item = stack.getItem();
		if (item instanceof CardboardPlaneItem plane) {
			var pitch = (float) -(Math.atan(direction.y) * 73.0D);
			var realYaw = (float) (Math.atan2(direction.z, direction.x) * 180.0 / Math.PI);
			return plane.fire(stack, level, origin, realYaw - 90, pitch);
		}

		return false;
	}
}
