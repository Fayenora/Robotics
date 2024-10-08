package com.io.norabotics.network.proxy;

import com.io.norabotics.client.screen.base.IElement;
import com.io.norabotics.network.messages.IPacketDataReceiver;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

/**
 * Code which should not run on Dedicated servers (physical server). Note that we still need to handle both logical sides, hence we inherit the ServerProxy
 */
public class ClientProxy extends ServerProxy {

    @OnlyIn(Dist.CLIENT)
    @Override
    public Level getLevel() {
        return Minecraft.getInstance().level;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public WeakReference<Player> createFakePlayer(Level level, GameProfile profile) {
        if(level instanceof ClientLevel clientLevel) {
            return new WeakReference<>(new RemotePlayer(clientLevel, profile));
        }
        return super.createFakePlayer(level, profile);
    }

    @OnlyIn(Dist.CLIENT)
    public WeakReference<Player> createFakePlayer(Level level, UUID uuid) {
        //Ensure a safe connection
        if(!Minecraft.getInstance().isLocalServer() && !Minecraft.getInstance().getConnection().getConnection().isEncrypted()) return null;

        PlayerInfo networkInfo = Minecraft.getInstance().player.connection.getPlayerInfo(uuid);
        if(networkInfo == null) return new WeakReference<>(null);
        return createFakePlayer(level, networkInfo.getProfile());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isTexturePresent(ResourceLocation resourceLocation) {
        try(SimpleTexture texture = new SimpleTexture(resourceLocation)) {
            texture.load(Minecraft.getInstance().getResourceManager());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RegistryAccess getRegistryAccess() {
        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        if(integratedServer != null) return integratedServer.registryAccess();
        ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if(clientPacketListener != null) return clientPacketListener.registryAccess();
        return super.getRegistryAccess();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ResourceManager getResourceManager() {
        return Minecraft.getInstance().getResourceManager();
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<Screen> getScreen() {
        return Optional.ofNullable(Minecraft.getInstance().screen);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isLocalServer() {
        return Minecraft.getInstance().isLocalServer();
    }

    @Override
    public void handleGuiData(int[] guiPath, Object data) {
        Screen currScreen = Minecraft.getInstance().screen;
        if(!(currScreen instanceof IElement current)) return;

        if(guiPath != null) { //If gui path is null, use current screen
            for(int i = guiPath.length - 1; i >= 0; i--) {
                for(GuiEventListener comp : current.children()) {
                    if(comp.hashCode() == guiPath[i] && comp instanceof IElement element) {
                        current = element;
                        break;
                    }
                }
            }
        }

        if(!(current instanceof IPacketDataReceiver receiver)) return;
        receiver.receive(data);
    }
}
