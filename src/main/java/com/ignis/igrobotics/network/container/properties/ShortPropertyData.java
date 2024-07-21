package com.ignis.igrobotics.network.container.properties;

import com.ignis.igrobotics.common.menu.BaseMenu;
import com.ignis.igrobotics.network.container.PropertyData;
import com.ignis.igrobotics.network.container.PropertyType;
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
