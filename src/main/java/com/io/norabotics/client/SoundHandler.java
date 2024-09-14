package com.io.norabotics.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/*
 * Who would've thought playing an interruptable sound is this difficult. Code adopted from
 * https://github.com/mekanism/Mekanism/blob/c2bfbd4d9ecbac45f8657cab79b9447323651f14/src/main/java/mekanism/client/sound/SoundHandler.java
 */
public class SoundHandler {

    private static final Long2ObjectMap<SoundInstance> soundMap = new Long2ObjectOpenHashMap<>();

    public static void playSound(SoundEvent sound) {
        playSound(SimpleSoundInstance.forUI(sound, 1, 1));
    }

    public static void playSound(SoundInstance sound) {
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    public static SoundInstance startTileSound(SoundEvent soundEvent, SoundSource category, float volume, RandomSource random, BlockPos pos) {
        // First, check to see if there's already a sound playing at the desired location
        SoundInstance s = soundMap.get(pos.asLong());
        if (s == null || !Minecraft.getInstance().getSoundManager().isActive(s)) {
            // No sound playing, start one up - we assume that tile sounds will play until explicitly stopped
            // We only use a simple sound here, there's no need to periodically update our sound's volume
            s = forTile(soundEvent, category, volume, random, pos);

            if (!isClientPlayerInRange(s)) return null;
            playSound(s);
            soundMap.put(pos.asLong(), s);
        }
        return s;
    }

    public static void stopTileSound(BlockPos pos) {
        long posKey = pos.asLong();
        SoundInstance s = soundMap.get(posKey);
        if (s != null) {
            Minecraft.getInstance().getSoundManager().stop(s);
            soundMap.remove(posKey);
        }
    }

    private static boolean isClientPlayerInRange(SoundInstance sound) {
        if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE) {
            //If the sound is global or has no attenuation, then return that the player is in range
            return true;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            //Shouldn't happen but just in case
            return false;
        }
        Sound s = sound.getSound();
        if (s == null) {
            //If the sound hasn't been initialized yet for some reason try initializing it
            sound.resolve(Minecraft.getInstance().getSoundManager());
            s = sound.getSound();
        }
        //Attenuation distance, defaults to 16 blocks
        int attenuationDistance = s.getAttenuationDistance();
        //Scale the distance based on the sound's volume
        float scaledDistance = Math.max(sound.getVolume(), 1) * attenuationDistance;
        //Check if the player is within range of hearing the sound
        return player.position().distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) < scaledDistance * scaledDistance;
    }

    public static SimpleSoundInstance forTile(SoundEvent event, SoundSource category, float volume, RandomSource randomSource, BlockPos pos) {
        return new SimpleSoundInstance(event, category, volume, 1, randomSource, pos.getX(), pos.getY(), pos.getZ());
    }
}
