package com.ignis.igrobotics.network.proxy;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Code which should not run on Dedicated servers (physical server). Note that we still need to handle both logical sides, hence we inherit the ServerProxy
 */
public class ClientProxy extends ServerProxy {

    /** How far to search away from the player if on the client */
    public static final int SEARCH_RADIUS = 100;

    @Override
    public Level getLevel() {
        return Minecraft.getInstance().level;
    }

    @Override
    public Entity getEntity(UUID uuid) {
        if(Minecraft.getInstance().level == null) return null;
        Player player = Minecraft.getInstance().player;
        for(Entity ent : player.level.getEntities(player, player.getBoundingBox().deflate(SEARCH_RADIUS))) {
            if(ent.getUUID().equals(uuid)) {
                return ent;
            }
        }
        return null;
    }

    @Override
    public Collection<LivingEntity> getRobotics(Predicate<IRobot> condition) {
        if(Minecraft.getInstance().level == null) return null;
        Player player = Minecraft.getInstance().player;
        Collection<LivingEntity> robots = new HashSet<>();
        for(Entity ent : player.level.getEntities(player, player.getBoundingBox().deflate(SEARCH_RADIUS))) {
            if(ent instanceof LivingEntity living
                    && ent.getCapability(ModCapabilities.ROBOT).isPresent()
                    && condition.test(ent.getCapability(ModCapabilities.ROBOT).resolve().get())) {
                robots.add(living);
            }
        }
        return robots;
    }

    @Override
    public WeakReference<Player> createFakePlayer(Level level, GameProfile profile) {
        if(level instanceof ClientLevel clientLevel) {
            return new WeakReference<>(new RemotePlayer(clientLevel, profile));
        }
        return super.createFakePlayer(level, profile);
    }

    public WeakReference<Player> createFakePlayer(Level level, UUID uuid) {
        //Ensure a safe connection
        if(!Minecraft.getInstance().isLocalServer() && !Minecraft.getInstance().getConnection().getConnection().isEncrypted()) return null;

        PlayerInfo networkInfo = Minecraft.getInstance().player.connection.getPlayerInfo(uuid);
        if(networkInfo == null) return new WeakReference<>(null);
        return createFakePlayer(level, networkInfo.getProfile());
    }

}
