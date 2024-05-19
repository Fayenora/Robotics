package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.robot.RobotModule;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.LinkedList;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModModules {

    public static final ResourceKey<Registry<RobotModule>> KEY = ResourceKey.createRegistryKey(new ResourceLocation(Robotics.MODID, "modules"));
    private static final HashMap<Item, RobotModule> modules = new HashMap<>();
    private static final LinkedList<RobotModule> overlays = new LinkedList<>();
    private static final HashMap<IPerkMap, HashMap<ResourceLocation, Integer>> queuedPerkMaps = new HashMap<>();

    public static RobotModule get(Item item) {
        return modules.get(item);
    }

    public static RobotModule get(ItemStack stack) {
        return get(stack.getItem());
    }

    public static boolean isModule(Item item) {
        return modules.containsKey(item);
    }

    public static boolean isModule(ItemStack stack) {
        return isModule(stack.getItem());
    }

    public static int getOverlayID(RobotModule module) {
        return overlays.indexOf(module);
    }

    public static RobotModule byOverlayID(int id) {
        return overlays.get(id);
    }

    /**
     * Register a {@link RobotModule} for quick lookup through {@link #get(Level, Item)}
     */
    private static void registerModule(RobotModule module) {
        if(module == null) return;
        for(ItemStack stack : module.getItems().getItems()) {
            Item key = stack.getItem();
            if(!modules.containsKey(key)) {
                modules.put(key, module);
            } else if(!modules.get(key).equals(module)) {
                modules.put(key, modules.get(key).merge(module));
            }
        }
        Robotics.LOGGER.debug("Registered module " + module);
        if(module.hasOverlay()) {
            if(overlays.size() >= Reference.MAX_RENDER_LAYERS) {
                Robotics.LOGGER.error("Registered too many modules with textures! The texture " + module.getOverlay() + " will not show up in game! "
                        + "If you need more module textures, contact the mod author!");
                return;
            }
            //Validate the texture on clients
            if(!Robotics.proxy.isTexturePresent(module.getOverlay())) {
                Robotics.LOGGER.error("Did not find specified texture " + module.getOverlay());
                return;
            }
            Robotics.LOGGER.debug("Registered texture for module: " + module.getOverlay());
            overlays.push(module);
        }
    }

    /**
     * Server side module reload. See {@link com.ignis.igrobotics.client.ClientEventHandler#onServerJoin(ClientPlayerNetworkEvent.LoggingIn)} for client
     */
    @SubscribeEvent
    public static void onDataSync(OnDatapackSyncEvent event) {
        if(event.getPlayer() == null) {
            reloadModules(event.getPlayerList().getPlayers().get(0).level().registryAccess());
        } else if(modules.isEmpty()) {
            reloadModules(event.getPlayer().level().registryAccess());
        }
    }

    public static void reloadModules(RegistryAccess access) {
        modules.clear();
        overlays.clear();
        Registry<RobotModule> reg = access.registryOrThrow(KEY);
        for(RobotModule module : reg) {
            registerModule(module);
        }
    }
}
