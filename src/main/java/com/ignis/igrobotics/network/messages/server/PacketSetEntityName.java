package com.ignis.igrobotics.network.messages.server;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.Charset;

public class PacketSetEntityName implements IMessage {

    private int entityId;
    private String name;

    public PacketSetEntityName() {}

    public PacketSetEntityName(int entityId, String name) {
        this.entityId = entityId;
        this.name = name;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(name, Reference.MAX_ROBOT_NAME_LENGTH);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        name = buf.readUtf(Reference.MAX_ROBOT_NAME_LENGTH);
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        Entity entity = cxt.getSender().level.getEntity(entityId);
        if(entity == null) return;
        entity.setCustomName(Component.literal(name));
    }
}
