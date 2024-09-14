package com.io.norabotics.network.messages.server;

import com.io.norabotics.common.WorldData;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.helpers.EntityFinder;
import com.io.norabotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class PacketReleaseFromCommandGroup implements IMessage {

    protected int commandGroup;
    protected UUID entity;

    public PacketReleaseFromCommandGroup() {

    }

    public PacketReleaseFromCommandGroup(int commandGroup, UUID entity) {
        this.commandGroup = commandGroup;
        this.entity = entity;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(commandGroup);
        buf.writeUUID(entity);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.commandGroup = buf.readInt();
        this.entity = buf.readUUID();
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        WorldData.get().releaseRobotFromCommandGroup(commandGroup, entity);
        Entity ent = EntityFinder.getEntity(cxt.getSender().level(), entity);
        if(ent != null) {
            ent.getCapability(ModCapabilities.ROBOT).ifPresent(robotics -> robotics.setCommandGroup(0));
        }
    }
}
