package com.kreidev.cmpackagecouriers.courier;

import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;

import javax.annotation.Nullable;

public interface CourierMob {

    @Nullable DeliveryNetwork getDeliveryNetwork();
    @Nullable DeliveryNetwork.Job getCurrentJob();
}
