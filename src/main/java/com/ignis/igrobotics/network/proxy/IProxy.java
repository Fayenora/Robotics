package com.ignis.igrobotics.network.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

public interface IProxy {

    WeakReference<Player> createFakePlayer(Level level, GameProfile profile);

    Level getLevel();

    @Nullable
    Entity getEntity(UUID uuid);
}
