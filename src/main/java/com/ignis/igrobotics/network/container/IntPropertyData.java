package com.ignis.igrobotics.network.container;

import com.ignis.igrobotics.client.menu.BaseMenu;
import net.minecraft.network.FriendlyByteBuf;

public class IntPropertyData extends PropertyData {
    private final int value;

    public IntPropertyData(short property, int value) {
        super(PropertyType.INT, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(BaseMenu container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(value);
    }
}
