package com.ignis.igrobotics.network.messages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IMessage {

    void encode(FriendlyByteBuf buf);

    void decode(FriendlyByteBuf buf);

    void handle(NetworkEvent.Context cxt);
}
