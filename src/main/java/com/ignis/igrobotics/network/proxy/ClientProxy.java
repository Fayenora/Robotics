package com.ignis.igrobotics.network.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Code which should not run on Dedicated servers (physical server). Note that we still need to handle both logical sides!
 */
public class ClientProxy implements IProxy {

    @Override
    public Level getLevel() {
        return Minecraft.getInstance().level;
    }

    @Override
    public Entity getEntity(UUID uuid) {
        if(Minecraft.getInstance().level == null) return null;
        Player player = Minecraft.getInstance().player;
        for(Entity ent : player.level.getEntities(player, player.getBoundingBox().deflate(100))) {
            if(ent.getUUID().equals(uuid)) {
                return ent;
            }
        }
        return null;
    }

    @Override
    public WeakReference<Player> createFakePlayer(Level level, GameProfile profile) {
        if(level instanceof ServerLevel serverLevel) {
            return new WeakReference<>(FakePlayerFactory.get(serverLevel, profile));
        }
        if(level instanceof ClientLevel clientLevel) {
            return new WeakReference<>(new RemotePlayer(clientLevel, profile));
        }
        return new WeakReference<>(null);
    }

    public WeakReference<Player> createFakePlayer(Level level, UUID uuid) {
        //Ensure a safe connection
        if(!Minecraft.getInstance().isLocalServer() && !Minecraft.getInstance().getConnection().getConnection().isEncrypted()) return null;

        PlayerInfo networkInfo = Minecraft.getInstance().player.connection.getPlayerInfo(uuid);
        if(networkInfo == null) return new WeakReference<>(null);
        return createFakePlayer(level, networkInfo.getProfile());
    }

}
