package com.ignis.igrobotics.core.util;

import net.minecraft.world.inventory.ContainerData;

public class ContainerDataUtil {

    public static ContainerData merge(ContainerData data1, ContainerData data2) {
        return new ContainerData() {
            @Override
            public int get(int key) {
                if(key < data1.getCount()) {
                    return data1.get(key);
                }
                return data2.get(key - data1.getCount());
            }

            @Override
            public void set(int key, int value) {
                if(key < data1.getCount()) {
                    data1.set(key, value);
                } else {
                    data2.set(key - data1.getCount(), value);
                }
            }

            @Override
            public int getCount() {
                return data1.getCount() + data2.getCount();
            }
        };
    }
}
