package com.ignis.igrobotics.network;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.network.messages.IMessage;
import com.ignis.igrobotics.network.messages.client.PacketContainerData;
import com.ignis.igrobotics.network.messages.client.PacketGuiData;
import com.ignis.igrobotics.network.messages.client.PacketSetEntityEffects;
import com.ignis.igrobotics.network.messages.server.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {

    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Robotics.MODID, "packets"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    private static int id = 0;

    public static void registerMessages() {
        registerMessage(PacketContainerData.class, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(PacketSetEntityEffects.class, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(PacketGuiData.class, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(PacketSetWatched.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(PacketOpenRobotMenu.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(PacketComponentAction.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(PacketConstructRobot.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(PacketSetEntityName.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessageHandledOnMessageThread(PacketRequestEntitySearch.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(PacketSetCommands.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(PacketSetAccessConfig.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(PacketReleaseFromCommandGroup.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(PacketAddCommand.class, NetworkDirection.PLAY_TO_SERVER);
    }

    private static <MSG extends IMessage> void registerMessage(Class<MSG> clazz, NetworkDirection direction) {
        INSTANCE.messageBuilder(clazz, id++, direction)
                .encoder(defaultEncoder())
                .decoder(defaultDecoder(clazz))
                .consumerMainThread(defaultHandler(direction))
                .add();
    }

    private static <MSG extends IMessage> void registerMessageHandledOnMessageThread(Class<MSG> clazz, NetworkDirection direction) {
        INSTANCE.messageBuilder(clazz, id++, direction)
                .encoder(defaultEncoder())
                .decoder(defaultDecoder(clazz))
                .consumerMainThread(handleOnMsgThread(direction))
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToAllPlayersWithChunk(MSG message, LevelChunk chunk) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    private static <MSG extends IMessage>  BiConsumer<MSG, FriendlyByteBuf> defaultEncoder() {
        return IMessage::encode;
    }

    private static <MSG extends IMessage> Function<FriendlyByteBuf, MSG> defaultDecoder(Class<MSG> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            return (buf) -> {
                try {
                    MSG msg = clazz.cast(constructor.newInstance());
                    msg.decode(buf);
                    return msg;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    Robotics.LOGGER.debug("Badly registered message " + clazz + "could not be constructed");
                    return null;
                }
            };
        } catch (NoSuchMethodException e) {
            Robotics.LOGGER.error("Registered Message " + clazz + " does not have empty constructor. This shouldn't happen, report it the mod author!");
            return (buf) -> null;
        }
    }

    private static <MSG extends IMessage> BiConsumer<MSG, Supplier<NetworkEvent.Context>> defaultHandler(NetworkDirection dir) {
        return switch (dir.getReceptionSide()) {
            case CLIENT -> (msg, cxt) -> {
                cxt.get().enqueueWork(() -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> msg.handle(cxt.get()));
                });
                cxt.get().setPacketHandled(true);
            };
            case SERVER -> (msg, cxt) -> {
                cxt.get().enqueueWork(() -> msg.handle(cxt.get()));
                cxt.get().setPacketHandled(true);
            };
        };
    }

    private static <MSG extends IMessage> BiConsumer<MSG, Supplier<NetworkEvent.Context>> handleOnMsgThread(NetworkDirection dir) {
        return switch (dir.getReceptionSide()) {
            case CLIENT -> (msg, cxt) -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> msg.handle(cxt.get()));
                cxt.get().setPacketHandled(true);
            };
            case SERVER -> (msg, cxt) -> {
                msg.handle(cxt.get());
                cxt.get().setPacketHandled(true);
            };
        };
    }

}
