package com.ignis.norabotics.common.helpers.util;

import java.util.EnumSet;

public interface Stable {

    int getStableId();

    static int encode(EnumSet<? extends Stable> set) {
        int ret = 0;

        for (Stable val : set) {
            ret |= (1 << val.getStableId());
        }

        return ret;
    }

    static <E extends Enum<E> & Stable> EnumSet<E> decode(int code, Class<E> clazz) {
        EnumSet<E> set = EnumSet.noneOf(clazz);

        for (E val : clazz.getEnumConstants()) {
            if((code & val.getStableId()) != 0) {
                set.add(val);
            }
        }

        return set;
    }

}
