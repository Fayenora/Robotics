package com.ignis.igrobotics.network.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.ref.WeakReference;

public class ServerProxy implements IProxy {

    @Override
    public Level getLevel() {
        return ServerLifecycleHooks.getCurrentServer().overworld();
    }

    @Override
    public WeakReference<Player> createFakePlayer(Level level, GameProfile profile) {
        if(!(level instanceof ServerLevel serverLevel)) return new WeakReference<>(null);
        return new WeakReference<>(FakePlayerFactory.get(serverLevel, profile));
    }
}
