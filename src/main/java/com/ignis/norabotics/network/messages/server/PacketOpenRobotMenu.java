package com.ignis.norabotics.network.messages.server;

import com.ignis.norabotics.common.helpers.RoboticsMenus;
import com.ignis.norabotics.network.messages.BufferSerializers;
import com.ignis.norabotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PacketOpenRobotMenu implements IMessage {

    private MenuType<?> type;
    private Object extraData;

    public PacketOpenRobotMenu() {}

    public PacketOpenRobotMenu(MenuType<?> type, Object extraData) {
        this.type = type;
        this.extraData = extraData;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeRegistryId(ForgeRegistries.MENU_TYPES, type);
        BufferSerializers.writeObject(buf, extraData);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        type = buf.readRegistryId();
        extraData = BufferSerializers.readObject(buf);
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        RoboticsMenus.openMenu(cxt.getSender(), type, extraData);
    }

}
