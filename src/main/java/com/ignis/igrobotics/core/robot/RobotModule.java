package com.ignis.igrobotics.core.robot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.capabilities.perks.PerkMap;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class RobotModule {

    public static final String TEXTURE_PATH = Robotics.MODID + ":textures/robot/modules/";

    private Ingredient item;
    private IPerkMap perks;

    /** A cooldown of 0 indicates a passive module */
    public int cooldown = 0;
    /** How long this module applies an effect, if it is active */
    public int duration = 0;
    /** The cost to activate the module. If the module is passive this is the energy cost per tick */
    public int energyCost = 0;
    /** Overlay to be applied to the robot */
    private ResourceLocation overlay;

    private RobotModule(Ingredient item) {
        this.item = item;
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

    public static RobotModule deserialize(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();

        Ingredient item = Ingredient.fromJson(obj.get("items"));
        RobotModule module = new RobotModule(item);

        if(obj.has("cooldown")) module.cooldown = obj.get("cooldown").getAsInt();
        if(obj.has("duration")) module.duration = obj.get("duration").getAsInt();
        if(obj.has("energyCost")) module.energyCost = obj.get("energyCost").getAsInt();
        if(obj.has("texture")) {
            String path = obj.get("texture").getAsString();
            if(!path.endsWith(".png")) path += ".png";
            module.overlay = new ResourceLocation(TEXTURE_PATH + path);
        }

        module.perks = PerkMap.deserialize(obj.get("perks"));

        return module;
    }

    public static boolean isModule(ItemStack stack) {
        return RoboticsConfig.current().modules.isModule(stack.getItem());
    }
}
