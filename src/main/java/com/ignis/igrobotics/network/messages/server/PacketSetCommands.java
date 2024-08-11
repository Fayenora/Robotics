package com.ignis.igrobotics.network.messages.server;

import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.robot.RobotCommand;
import com.ignis.igrobotics.network.messages.IMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;

public class PacketSetCommands implements IMessage {

    protected int entityId;
    protected List<RobotCommand> commands;

    public PacketSetCommands() {

    }

    public PacketSetCommands(int entityId, List<RobotCommand> commands) {
        this.entityId = entityId;
        this.commands = commands;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        CompoundTag nbt = new CompoundTag();
        RobotCommand.writeToNBT(nbt, commands); //TODO: Optimize
        buf.writeNbt(nbt);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        commands = RobotCommand.readFromNBT(buf.readNbt());
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        Entity entity = cxt.getSender().level().getEntity(entityId);
        if(entity == null) return;
        entity.getCapability(ModCapabilities.COMMANDS).ifPresent(robot -> {
            robot.setCommands(commands);
        });
    }
}
