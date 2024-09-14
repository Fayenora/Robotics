package com.io.norabotics.network.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.ref.WeakReference;
import java.util.Optional;

public class ServerProxy implements IProxy {

    @Override
    public Level getLevel() {
        return ServerLifecycleHooks.getCurrentServer().overworld();
    }

    @Override
    public RegistryAccess getRegistryAccess() {
        return ServerLifecycleHooks.getCurrentServer().registryAccess();
    }

    @Override
    public WeakReference<Player> createFakePlayer(Level level, GameProfile profile) {
        if(!(level instanceof ServerLevel serverLevel)) return new WeakReference<>(null);
        return new WeakReference<>(FakePlayerFactory.get(serverLevel, profile));
    }

    @Override
    public boolean isTexturePresent(ResourceLocation resourceLocation) {
        return true;
    }

    @Override
    public void handleGuiData(int[] guiPath, Object data) {}

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public ResourceManager getResourceManager() {
        return ServerLifecycleHooks.getCurrentServer().getResourceManager();
    }

    @Override
    public boolean isLocalServer() {
        return false;
    }

    @Override
    public Optional<Screen> getScreen() {
        return Optional.empty();
    }
}
