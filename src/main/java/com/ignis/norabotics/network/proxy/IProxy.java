package com.ignis.norabotics.network.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.util.Optional;

public interface IProxy {

    WeakReference<Player> createFakePlayer(Level level, GameProfile profile);

    Level getLevel();

    RegistryAccess getRegistryAccess();

    Player getPlayer();

    ResourceManager getResourceManager();

    boolean isLocalServer();

    Optional<Screen> getScreen();

    boolean isTexturePresent(ResourceLocation resourceLocation);

    void handleGuiData(int[] guiPath, Object data);
}
