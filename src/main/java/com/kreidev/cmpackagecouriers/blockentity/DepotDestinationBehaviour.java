package com.kreidev.cmpackagecouriers.blockentity;

import com.kreidev.cmpackagecouriers.PackageCarrier;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DepotDestinationBehaviour extends BlockEntityBehaviour {

    public static final BehaviourType<DepotDestinationBehaviour> TYPE = new BehaviourType<>();

    public DepotDestinationBehaviour(DepotBlockEntity be) {
        super(be);
        this.setLazyTickRate(50);
    }

    public void attachSubBehaviours(ModBlockEntityBehaviours.BehaviourInstaller installer) {
        SignAddressableBehaviour addressableBehaviour = new SignAddressableBehaviour(this.blockEntity);
        installer.attach(addressableBehaviour);
        installer.attach(new PackageDestinationBehaviour(this.blockEntity)
                .addressSupplier(addressableBehaviour::getAddressFilter)
                .hasSpaceWhen(this::hasSpace)
                .setInsertionHandler(this::tryInsertPackage)
        );
    }

    private boolean hasSpace() {
        return ((DepotBlockEntity) this.blockEntity).getHeldItem().is(Items.AIR);
    }

    private boolean tryInsertPackage(PackageCarrier carrier) {
        if (!this.hasSpace()) return false;
        DepotBlockEntity depot = (DepotBlockEntity) this.blockEntity;
        depot.setHeldItem(carrier.getPackage());
        depot.notifyUpdate();
        carrier.setPackage(ItemStack.EMPTY);
        return true;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
