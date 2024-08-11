package com.ignis.igrobotics.network.messages.server;

import com.ignis.igrobotics.common.access.AccessConfig;
import com.ignis.igrobotics.common.access.WorldAccessData;
import com.ignis.igrobotics.common.handlers.RobotBehavior;
import com.ignis.igrobotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class PacketSetAccessConfig implements IMessage {

    private WorldAccessData.EnumAccessScope scope;
    private int entityId;
    private AccessConfig config;

    public PacketSetAccessConfig() {}

    public PacketSetAccessConfig(WorldAccessData.EnumAccessScope scope, Entity robot, AccessConfig config) {
        this.scope = scope;
        this.entityId = robot.getId();
        this.config = config;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(scope.ordinal());
        buf.writeInt(entityId);
        config.write(buf);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        scope = WorldAccessData.EnumAccessScope.values()[buf.readByte()];
        entityId = buf.readInt();
        config = new AccessConfig();
        config.read(buf);
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        Entity entity = cxt.getSender().level().getEntity(entityId);
        if(entity == null) return;
        RobotBehavior.setAccess(scope, entity, config);
    }
}
