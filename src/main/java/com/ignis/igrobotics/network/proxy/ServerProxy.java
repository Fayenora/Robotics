package com.ignis.igrobotics.network.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class ServerProxy implements IProxy {

    @Override
    public Level getLevel() {
        return ServerLifecycleHooks.getCurrentServer().overworld();
    }

    @Override
    public Entity getEntity(UUID uuid) {
        for(ServerLevel dimension : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            Entity entity = dimension.getEntity(uuid);
            if(entity != null) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public WeakReference<Player> createFakePlayer(Level level, GameProfile profile) {
        return new WeakReference<>(FakePlayerFactory.get((ServerLevel) level, profile));
    }
}
