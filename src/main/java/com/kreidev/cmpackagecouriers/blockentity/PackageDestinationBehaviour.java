package com.kreidev.cmpackagecouriers.blockentity;

import com.kreidev.cmpackagecouriers.PackageCarrier;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

// TODO: interface for certain destination behaviours that also acts as courier network suppliers (i.e. depot)
// Or dynamically inserted behaviours could work too.
public abstract class PackageDestinationBehaviour extends BlockEntityBehaviour {

    // I think it's fine, only one of these would exist per be anyway and only used as key
    public static final BehaviourType<PackageDestinationBehaviour> TYPE = new BehaviourType<>();

    public PackageDestinationBehaviour(SmartBlockEntity be) {
        super(be);
    }

    /**
     * Intended to be called by the carrier when it's currently going to the destination.
     */
    public void tickIncoming(PackageCarrier carrier) {
    }

    public abstract String getAddress();

    public abstract boolean hasSpace();

    public abstract boolean tryInsertPackage(PackageCarrier carrier);

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
