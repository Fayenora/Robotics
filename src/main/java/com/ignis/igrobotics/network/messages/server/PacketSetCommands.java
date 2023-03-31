package com.ignis.igrobotics.network.messages.server;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.network.messages.IMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
        Level level = cxt.getSender().level;
        level.getEntity(entityId).getCapability(ModCapabilities.COMMANDS).ifPresent(robot -> {
            robot.setCommands(commands);
        });
    }
}
