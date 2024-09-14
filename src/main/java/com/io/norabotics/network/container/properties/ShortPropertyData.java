package com.io.norabotics.network.container.properties;

import com.io.norabotics.common.content.menu.BaseMenu;
import com.io.norabotics.network.container.PropertyData;
import com.io.norabotics.network.container.PropertyType;
import net.minecraft.network.FriendlyByteBuf;

public class ShortPropertyData extends PropertyData {

    private final short value;

    public ShortPropertyData(short property, short value) {
        super(PropertyType.BYTE, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(BaseMenu container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeShort(value);
    }
}
