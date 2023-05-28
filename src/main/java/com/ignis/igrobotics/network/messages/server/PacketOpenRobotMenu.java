package com.ignis.igrobotics.network.messages.server;

import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PacketOpenRobotMenu implements IMessage {

    private MenuType<?> type;
    private int entityId;

    public PacketOpenRobotMenu() {}

    public PacketOpenRobotMenu(MenuType<?> type, int entityId) {
        this.type = type;
        this.entityId = entityId;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeRegistryId(ForgeRegistries.MENU_TYPES, type);
        buf.writeInt(entityId);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        type = buf.readRegistryId();
        entityId = buf.readInt();
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        RobotBehavior.openRobotMenu(cxt.getSender(), type, cxt.getSender().level.getEntity(entityId));
    }

}
