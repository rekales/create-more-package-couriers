package com.kreidev.cmpackagecouriers.blockentity;

import com.kreidev.cmpackagecouriers.PackageCarrier;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class BeltDestinationBehaviour extends BlockEntityBehaviour {

    public static final BehaviourType<BeltDestinationBehaviour> TYPE = new BehaviourType<>();

    public BeltDestinationBehaviour(BeltBlockEntity be) {
        super(be);
    }

    public void addSubBehaviours(List<BlockEntityBehaviour> behaviours) {
        SignAddressableBehaviour addressableBehaviour = new SignAddressableBehaviour(this.blockEntity);
        behaviours.add(addressableBehaviour);
        behaviours.add(new PackageDestinationBehaviour(this.blockEntity)
                .addressSupplier(addressableBehaviour::getAddressFilter)
                .hasSpaceWhen(this::hasSpace)
                .setInsertionHandler(this::tryInsertPackage)
        );
    }

    private boolean hasSpace() {
        BeltBlockEntity belt = (BeltBlockEntity) this.blockEntity;
        return belt.getInventory().getStackAtOffset(belt.index) == null;
    }

    private boolean tryInsertPackage(PackageCarrier carrier) {
        if (!this.hasSpace()) return false;
        if (this.blockEntity.getLevel() == null) return false;
        BeltBlockEntity belt = (BeltBlockEntity) this.blockEntity;
        IItemHandler handler = belt.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, belt.getBlockPos(), null);
        if (handler == null) return false;
        handler.insertItem(0, carrier.getPackage(), false);
        return true;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
