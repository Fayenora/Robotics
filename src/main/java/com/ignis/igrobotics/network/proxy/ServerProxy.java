package com.ignis.igrobotics.network.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.lang.ref.WeakReference;

public class ServerProxy implements IProxy {

    @Override
    public WeakReference<Player> createFakePlayer(Level level, GameProfile profile) {
        return new WeakReference<>(FakePlayerFactory.get((ServerLevel) level, profile));
    }
}
