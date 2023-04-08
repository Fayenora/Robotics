package com.ignis.igrobotics.network.messages.server;

import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.network.messages.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PacketConstructRobot implements IMessage {

    private BlockPos pos;
    private String name;

    public PacketConstructRobot() {}

    public PacketConstructRobot(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(name);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        name = buf.readUtf();
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        BlockEntity tile = cxt.getSender().level.getBlockEntity(pos);
        if(!(tile instanceof FactoryBlockEntity factory)) return;

        factory.getEntity().ifPresent(ent -> ent.setCustomName(Component.literal(name)));
        if(factory.hasCraftedRobotReady()) {
            factory.createNewRobot(cxt.getSender().getUUID());
        } else factory.startMachine(2);
    }
}
