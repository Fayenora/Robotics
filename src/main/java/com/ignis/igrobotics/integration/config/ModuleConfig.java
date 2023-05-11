package com.ignis.igrobotics.integration.config;

import com.google.gson.Gson;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.robot.RobotModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class ModuleConfig implements IJsonConfig {
	
	/** Actual list of modules loaded from the json config file. Filled during config reading */
	public final HashMap<Item, RobotModule> MODULES = new HashMap<>();
	/** A list of modules with texture overlays. This way each textured module is assigned an id (position in this list), 
	 * which can be sent to clients */
	public final LinkedList<RobotModule> overlays = new LinkedList();
	
	@Override
	public void load(File file) {
		MODULES.clear();
		overlays.clear();
		Gson gson = ConfigJsonSerializer.initGson();
		if(!file.exists()) ConfigUtils.copyFromDefault("robot_modules.json", file);
		
		RobotModule[] modules = (RobotModule[]) ConfigUtils.readJson(gson, file, RobotModule[].class);

		if(modules == null) return;
		for(RobotModule module : modules) {
			registerModule(module);
		}
	}

	private void registerModule(RobotModule module) {
		if(module == null) return;
		for(ItemStack stack : module.getItems().getItems()) {
			if(MODULES.containsKey(stack.getItem())) {
				Robotics.LOGGER.error("Registered 2 modules on item " + stack.getItem() + "! Only one will work!");
			}
			MODULES.put(stack.getItem(), module);
		}
		Robotics.LOGGER.debug("Registered module " + module);
		if(module.hasOverlay()) {
			if(overlays.size() >= Integer.BYTES) {
				Robotics.LOGGER.error("Registered too many modules with textures! The texture " + module.getOverlay() + " will not show up in game! "
						+ "If you need more module textures, contact the mod author!");
				return;
			}
			Robotics.LOGGER.debug("Registered texture for module: " + module.getOverlay());
			overlays.push(module);
		}
	}
	
	public RobotModule get(Item item) {
		return MODULES.get(item);
	}
	
	public RobotModule get(ItemStack stack) {
		return MODULES.get(stack.getItem());
	}
	
	public boolean isModule(Item item) {
		return MODULES.containsKey(item);
	}
	
	@Override
	public void toNetwork(FriendlyByteBuf buffer) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void fromNetwork(FriendlyByteBuf buffer) {
		// TODO Auto-generated method stub
		// TODO Validate received textures like this. Remember to do this in the proxy!
		/*
		SimpleTexture texture = new SimpleTexture(module.getOverlay());
		try {
			texture.loadTexture(Minecraft.getMinecraft().getResourceManager());
		} catch (IOException e) {
			Robotics.logger.error("Did not find specified texture " + module.getOverlay());
		}
		 */
	}

}
