package com.ignis.norabotics.network.container.properties;

import com.ignis.norabotics.common.content.menu.BaseMenu;
import com.ignis.norabotics.network.container.PropertyData;
import com.ignis.norabotics.network.container.PropertyType;
import net.minecraft.network.FriendlyByteBuf;

public class BytePropertyData extends PropertyData {

    private final byte value;

    public BytePropertyData(short property, byte value) {
        super(PropertyType.BYTE, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(BaseMenu container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(value);
    }
}
