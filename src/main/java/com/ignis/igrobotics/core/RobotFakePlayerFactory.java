package com.ignis.igrobotics.core;

import com.google.common.collect.Maps;
import com.ignis.igrobotics.Robotics;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobotFakePlayerFactory {

    private static final Map<GameProfile, FakePlayer> fakePlayers = Maps.newHashMap();

    public static FakePlayer get(Mob mob, GameProfile username) {
        if(!(mob.level() instanceof ServerLevel)) return null;
        if (!fakePlayers.containsKey(username)) {
            FakePlayer fakePlayer = new RobotFakePlayer(mob, username);
            fakePlayers.put(username, fakePlayer);
        }

        return fakePlayers.get(username);
    }

    @SubscribeEvent
    public static void unloadLevel(LevelEvent.Unload event) {
        fakePlayers.entrySet().removeIf(entry -> entry.getValue().level() == event.getLevel());
    }
}
