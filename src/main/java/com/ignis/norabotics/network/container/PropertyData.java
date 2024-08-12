package com.ignis.norabotics.network.container;

import com.ignis.norabotics.common.content.menu.BaseMenu;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Credits to Mekanism's author
 * @see <a href="https://github.com/mekanism/Mekanism/blob/1.20.6/src/main/java/mekanism/common/network/to_client/container/property/PropertyData.java">...</a>
 * @author Sara Freimer
 */
public abstract class PropertyData {

    private final PropertyType type;
    private final short property;

    protected PropertyData(PropertyType type, short property) {
        this.type = type;
        this.property = property;
    }

    public PropertyType getType() {
        return type;
    }

    public short getProperty() {
        return property;
    }

    public abstract void handleWindowProperty(BaseMenu container);

    public abstract void write(FriendlyByteBuf buffer);

    public static void toBuffer(FriendlyByteBuf buffer, PropertyData data) {
        buffer.writeEnum(data.type);
        buffer.writeShort(data.property);
        data.write(buffer);
    }

    public static PropertyData fromBuffer(FriendlyByteBuf buffer) {
        PropertyType type = buffer.readEnum(PropertyType.class);
        short property = buffer.readShort();
        return type.createData(property, buffer);
    }
}