package com.kreidev.cmpackagecouriers.courier;

import com.kreidev.cmpackagecouriers.PackageCarrier;
import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;

import javax.annotation.Nullable;

public interface CourierMob extends PackageCarrier {

    @Nullable DeliveryNetwork getDeliveryNetwork();
    @Nullable DeliveryNetwork.Job getCurrentJob();
}
