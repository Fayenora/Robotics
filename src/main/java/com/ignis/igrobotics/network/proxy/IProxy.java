package com.ignis.igrobotics.network.proxy;

import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public interface IProxy {

    WeakReference<Player> createFakePlayer(Level level, GameProfile profile);

    Level getLevel();

    @Nullable
    Entity getEntity(UUID uuid);

    Collection<LivingEntity> getRobotics(Predicate<IRobot> condition);
}
