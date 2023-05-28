package com.ignis.igrobotics.network.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;

public interface IProxy {

    WeakReference<Player> createFakePlayer(Level level, GameProfile profile);

    Level getLevel();
}
