package com.kreidev.cmpackagecouriers.blockentity;

import com.kreidev.cmpackagecouriers.PackageCarrier;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

// TODO: interface for certain destination behaviours that also acts as courier network suppliers (i.e. depot)
// Or dynamically inserted behaviours could work too.
// Just tryna follow suite with higher-order programming pattern from other behaviours
public class PackageDestinationBehaviour extends BlockEntityBehaviour {

    private Supplier<String> addressSupplier;
    private Consumer<PackageCarrier> incomingTicker;
    private Supplier<Boolean> hasSpaceCheck;
    private Predicate<PackageCarrier> tryInsertCheck;

    public static final BehaviourType<PackageDestinationBehaviour> TYPE = new BehaviourType<>();

    public PackageDestinationBehaviour(SmartBlockEntity be) {
        super(be);
        this.addressSupplier = () -> "";
        this.incomingTicker = carrier -> {};
        this.hasSpaceCheck = () -> true;
        this.tryInsertCheck = carrier -> false;
    }

    /**
     * Intended to be called by the carrier when it's currently going to the destination.
     */
    public void tickIncoming(PackageCarrier carrier) {
        this.incomingTicker.accept(carrier);
    }

    public String getAddress() {
        return this.addressSupplier.get();
    }

    public boolean hasSpace() {
        return this.hasSpaceCheck.get();
    }

    public boolean tryInsertPackage(PackageCarrier carrier) {
        return this.tryInsertCheck.test(carrier);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public PackageDestinationBehaviour addressSupplier(Supplier<String> addressSupplier) {
        this.addressSupplier = addressSupplier;
        return this;
    }

    public PackageDestinationBehaviour incomingTicker(Consumer<PackageCarrier> incomingTicker) {
        this.incomingTicker = incomingTicker;
        return this;
    }

    public PackageDestinationBehaviour hasSpaceWhen(Supplier<Boolean> hasSpaceCheck) {
        this.hasSpaceCheck = hasSpaceCheck;
        return this;
    }

    public PackageDestinationBehaviour setInsertionHandler(Predicate<PackageCarrier> tryInsertCheck) {
        this.tryInsertCheck = tryInsertCheck;
        return this;
    }
}
