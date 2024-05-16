package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.modules.ModuleActions;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.capabilities.perks.PerkMap;
import com.ignis.igrobotics.core.robot.RobotModule;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

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

    /**
     * Server side perk map population. See {@link com.ignis.igrobotics.client.ClientEventHandler#onServerJoin(ClientPlayerNetworkEvent.LoggingIn)} for client
     */
    @SubscribeEvent
    public static void onServerWorldLoad(ServerStartedEvent event) {
        populatePerkMaps(event.getServer().registryAccess());
    }

    public static void reloadModules(RegistryAccess access) {
        modules.clear();
        overlays.clear();
        Registry<RobotModule> reg = access.registryOrThrow(KEY);
        for(RobotModule module : reg) {
            registerModule(module);
        }
    }

    public static void populatePerkMaps(RegistryAccess registryAccess) {
        Iterator<IPerkMap> it = queuedPerkMaps.keySet().iterator();
        Robotics.LOGGER.info("queuedPerkMaps: " + queuedPerkMaps.size());
        Registry<Perk> perkRegistry = registryAccess.registryOrThrow(ModPerks.KEY);
        while(it.hasNext()) {
            IPerkMap perkMap = it.next();
            for(ResourceLocation perkKey : queuedPerkMaps.get(perkMap).keySet()) {
                Perk perk = perkRegistry.get(perkKey);
                if(perk == null) {
                    Robotics.LOGGER.warn("Perk \"" + perkKey + "\" does not exist. ");
                }
                perkMap.add(perk, queuedPerkMaps.get(perkMap).get(perkKey));
            }
            it.remove();
        }
    }

    public static void queueInit(IPerkMap perkMap, List<Pair<ResourceLocation, Integer>> perks) {
        HashMap<ResourceLocation, Integer> map = new HashMap<>();
        perks.forEach(p -> map.put(p.getFirst(), p.getSecond()));
        queuedPerkMaps.put(perkMap, map);
    }

    public static void mergeQueued(IPerkMap map1, IPerkMap map2) {
        if(queuedPerkMaps.containsKey(map2)) {
            if(queuedPerkMaps.containsKey(map1)) {
                HashMap<ResourceLocation, Integer> perks = queuedPerkMaps.get(map1);
                for(ResourceLocation perkKey : queuedPerkMaps.get(map2).keySet()) {
                    perks.put(perkKey, perks.getOrDefault(perkKey, 0) + queuedPerkMaps.get(map2).get(perkKey));
                }
                queuedPerkMaps.put(map1, perks);
            } else {
                queueInit(map1, PerkMap.toCodecFormat(map2));
            }
        }
    }
}
