package com.ignis.igrobotics.core.robot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.modules.ModuleActions;
import com.ignis.igrobotics.core.capabilities.energy.ModifiableEnergyStorage;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.capabilities.perks.PerkMap;
import com.ignis.igrobotics.core.util.StringUtil;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.EnumSet;

public class RobotModule {

    public static final String TEXTURE_PATH = Robotics.MODID + ":textures/robot/modules/";

    private final Ingredient item;
    private IPerkMap perks = new PerkMap();
    private EnumSet<EnumModuleSlot> viableSlots = EnumSet.noneOf(EnumModuleSlot.class);

    private ModuleActions action = ModuleActions.NONE;
    /** A cooldown of 0 indicates a passive module */
    private int cooldown = 0;
    /** How long this module applies an effect, if it is active */
    private int duration = 0;
    /** The cost to activate the module. If the module is passive this is the energy cost per tick */
    private int energyCost = 0;
    /** Overlay to be applied to the robot */
    private ResourceLocation overlay;

    protected RobotModule(Ingredient item) {
        this.item = item;
    }

    public boolean activate(LivingEntity caster) {
        if(action == ModuleActions.NONE) return false;
        if(!action.execute(caster, duration)) return false;
        if(energyCost > 0) {
            if(!caster.getCapability(ForgeCapabilities.ENERGY).isPresent()) return false;
            IEnergyStorage energyStorage = caster.getCapability(ForgeCapabilities.ENERGY).resolve().get();
            if(energyStorage.getEnergyStored() < energyCost) return false;
            if (energyStorage instanceof ModifiableEnergyStorage mod) {
                mod.setEnergy(mod.getEnergyStored() - energyCost);
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RobotModule module) {
            return item.equals(module.item);
        }
        return false;
    }

    @LuaFunction
    public int getCooldown() {
        return cooldown;
    }

    @LuaFunction
    public int getEnergyCost() {
        return energyCost;
    }

    @LuaFunction
    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return item.getItems().length >= 1 ? item.getItems()[0].getItem().toString() : "";
    }

    public Ingredient getItems() {
        return item;
    }

    public IPerkMap getPerks() {
        return perks;
    }

    public ResourceLocation getOverlay() {
        return overlay;
    }

    public boolean hasOverlay() {
        return overlay != null;
    }

    public EnumSet<EnumModuleSlot> getViableSlots() {
        return viableSlots;
    }

    public ModuleActions getAction() {
        return action;
    }

    public static RobotModule deserialize(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();

        try {
            Ingredient item = Ingredient.fromJson(obj.get("items"));
            RobotModule module = new RobotModule(item);

            if(obj.has("slots")) {
                for(JsonElement tag : obj.get("slots").getAsJsonArray()) {
                    String s = tag.getAsString().toUpperCase();
                    try {
                        module.viableSlots.add(EnumModuleSlot.valueOf(s));
                    } catch(IllegalArgumentException ignored) {
                        Robotics.LOGGER.warn("\"" + s + "\" is not a valid module slot. Viable values are: " + StringUtil.enumToString(EnumModuleSlot.values()));
                    }
                }
            } else {
                module.viableSlots.add(EnumModuleSlot.DEFAULT);
            }
            if (obj.has("cooldown")) module.cooldown = obj.get("cooldown").getAsInt();
            if (obj.has("duration")) module.duration = obj.get("duration").getAsInt();
            if (obj.has("energyCost")) module.energyCost = obj.get("energyCost").getAsInt();
            if (obj.has("action")) {
                try {
                    module.action = ModuleActions.valueOf(obj.get("action").getAsString().toUpperCase());
                } catch(IllegalArgumentException e) {
                    Robotics.LOGGER.warn("Did not find action \"" + obj.get("action").getAsString() + "\". Viable actions are: " + StringUtil.enumToString(ModuleActions.values()));
                }
            }
            if (obj.has("texture")) {
                String path = obj.get("texture").getAsString();
                if (!path.endsWith(".png")) path += ".png";
                module.overlay = new ResourceLocation(TEXTURE_PATH + path);
            }

            if(obj.has("perks")) {
                module.perks = PerkMap.deserialize(obj.get("perks"));
            }

            return module;
        } catch(JsonSyntaxException e) {
            Robotics.LOGGER.warn("Failed to register module: " + e.getLocalizedMessage());
            return null;
        }
    }

    public static void write(FriendlyByteBuf buffer, RobotModule module) {
        if(!(module.perks instanceof PerkMap perkMap)) return;
        module.item.toNetwork(buffer);
        PerkMap.write(buffer, perkMap);
        buffer.writeBoolean(module.hasOverlay());
        if(module.hasOverlay()) {
            buffer.writeResourceLocation(module.overlay);
        }
        buffer.writeEnumSet(module.viableSlots, EnumModuleSlot.class);
        buffer.writeEnum(module.action);
    }

    public static RobotModule read(FriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        RobotModule module = new RobotModule(ingredient);
        module.perks = PerkMap.read(buffer);
        if(buffer.readBoolean()) {
            module.overlay = buffer.readResourceLocation();
        }
        module.viableSlots = buffer.readEnumSet(EnumModuleSlot.class);
        module.action = buffer.readEnum(ModuleActions.class);
        return module;
    }

    public static RobotModule get(Item item) {
        return RoboticsConfig.current().modules.get(item);
    }

    public static RobotModule get(ItemStack stack) {
        return RoboticsConfig.current().modules.get(stack);
    }

    public static boolean isModule(ItemStack stack) {
        return RoboticsConfig.current().modules.isModule(stack.getItem());
    }
}
