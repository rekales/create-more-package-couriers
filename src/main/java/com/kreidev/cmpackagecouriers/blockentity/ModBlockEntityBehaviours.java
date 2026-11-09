package com.kreidev.cmpackagecouriers.blockentity;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

public class ModBlockEntityBehaviours {

    public static void attachBehaviours(BlockEntityBehaviourEvent event) {
        event.forType(AllBlockEntityTypes.DEPOT.get(), be -> {
            DepotDestinationBehaviour behaviour;
            event.attach(behaviour = new DepotDestinationBehaviour(be));
            behaviour.attachSubBehaviours(event::attach);
        });

        event.forType(AllBlockEntityTypes.BELT.get(), be -> {
            BeltDestinationBehaviour behaviour;
            event.attach(behaviour = new BeltDestinationBehaviour(be));
            behaviour.attachSubBehaviours(event::attach);
        });
    }

    @FunctionalInterface
    public interface BehaviourInstaller {
        void attach(BlockEntityBehaviour behaviour);
    }
}
