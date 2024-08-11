package com.ignis.igrobotics.network.messages.server;

import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.robot.RobotCommand;
import com.ignis.igrobotics.network.messages.IMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class PacketAddCommand implements IMessage {

    protected int entityId;
    protected RobotCommand command;

    public PacketAddCommand() {

    }

    public PacketAddCommand(int entityId, RobotCommand command) {
        this.entityId = entityId;
        this.command = command;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        CompoundTag nbt = new CompoundTag();
        RobotCommand.writeToNBT(nbt, command); //TODO Optimize
        buf.writeNbt(nbt);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        command = RobotCommand.readSingleCommandFromNBT(buf.readNbt());
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        Entity entity = cxt.getSender().level().getEntity(entityId);
        if(entity == null) return;
        entity.getCapability(ModCapabilities.COMMANDS).ifPresent(robot -> {
            robot.addCommand(command);
        });
    }
}
