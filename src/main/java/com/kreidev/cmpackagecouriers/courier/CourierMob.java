package com.kreidev.cmpackagecouriers.courier;

import com.kreidev.cmpackagecouriers.courier.network.DeliveryNetwork;

public interface CourierMob {

    DeliveryNetwork getDeliveryNetwork();
    DeliveryNetwork.Job getCurrentJob();
}
