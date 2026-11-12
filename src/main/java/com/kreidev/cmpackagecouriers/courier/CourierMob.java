package com.kreidev.cmpackagecouriers.courier;

import com.kreidev.cmpackagecouriers.PackageCarrier;
import com.kreidev.cmpackagecouriers.PackageDestination;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;

import javax.annotation.Nullable;

public interface CourierMob extends PackageCarrier {

    @Nullable DeliveryNetwork getDeliveryNetwork();
    @Nullable DeliveryNetwork.Job getCurrentJob();

    default @Nullable PackageDestination getDestination() {
        DeliveryNetwork.Job job = this.getCurrentJob();
        if (job == null) return null;
        return job.to();
    }
}
