package com.kreidev.cmpackagecouriers.sign;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class AddressSignItem extends BlockItem {

    public AddressSignItem(Properties properties) {
        super(AddressSignReg.ADDRESS_SIGN_BLOCK.get(), properties);
    }
}
