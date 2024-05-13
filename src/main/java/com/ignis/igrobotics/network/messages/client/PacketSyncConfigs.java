package com.ignis.igrobotics.network.messages.client;

import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.ignis.igrobotics.integration.jei.RoboticsJEIPlugin;
import com.ignis.igrobotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncConfigs implements IMessage {

    RoboticsConfig config;

    public PacketSyncConfigs() {
        config = new RoboticsConfig();
        //config.client = null;
    }

    public PacketSyncConfigs(RoboticsConfig config) {
        this.config = config;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        config.modules.toNetwork(buf);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        config.modules.fromNetwork(buf);
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        RoboticsConfig.receiveConfig(config);
    }
}
