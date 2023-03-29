package com.ignis.igrobotics.core.access;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.UUID;

/**
 * Saves access rights in the world. Can handle players setting global, group-specific or robot-specific access rights
 * @author Ignis
 */
public class WorldAccessData implements INBTSerializable<CompoundTag> {
	
	HashMap<UUID, AccessConfig> playerScopedConfigurations = new HashMap<>();
	HashMap<Integer, AccessConfig> groupScopedConfigurations = new HashMap<>();
	
	public AccessConfig getPlayerConfig(UUID player) {
		if(playerScopedConfigurations.containsKey(player)) {
			return playerScopedConfigurations.get(player);
		}
		return new AccessConfig(player);
	}
	
	public AccessConfig getGroupConfig(UUID owner, int group) {
		if(groupScopedConfigurations.containsKey(group)) {
			return groupScopedConfigurations.get(group);
		}
		return new AccessConfig(owner);
	}
	
	public void setPlayerAccessConfig(UUID player, AccessConfig config) {
		//If the config equals the default config, don't waste storage space
		AccessConfig defaultConfig = new AccessConfig();
		defaultConfig.setOwner(player);
		config.setOwner(player);
		if(config.equals(defaultConfig) && getPlayerConfig(player).equals(defaultConfig)) return;
		playerScopedConfigurations.put(player, config);
	}
	
	public void setGroupAccessConfig(UUID player, int group, AccessConfig config) {
		AccessConfig defaultConfig = new AccessConfig();
		defaultConfig.setOwner(player);
		config.setOwner(player);
		if(config.equals(defaultConfig) && getGroupConfig(player, group).equals(defaultConfig)) return;
		groupScopedConfigurations.put(group, config);
	}
	
	public AccessConfig getConfigFor(EnumAccessScope scope, ISecuredObject securedObject) {
		return switch (scope) {
			case PLAYER -> getPlayerConfig(securedObject.getOwner());
			case GROUP -> getGroupConfig(securedObject.getOwner(), securedObject.getGroup());
			case ROBOT -> securedObject.getConfiguration();
		};
	}
	
	public void setAccessConfigFor(EnumAccessScope scope, ISecuredObject securedObject, AccessConfig config) {
		switch(scope) {
		case PLAYER: setPlayerAccessConfig(securedObject.getOwner(), config); break;
		case GROUP: setGroupAccessConfig(securedObject.getOwner(), securedObject.getGroup(), config);
		case ROBOT: securedObject.setConfiguration(config); break;
		}
	}
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag allConfigData = new CompoundTag();
		CompoundTag playerConfigData = new CompoundTag();
		CompoundTag groupConfigData = new CompoundTag();
		for(UUID key : playerScopedConfigurations.keySet()) {
			CompoundTag config = playerScopedConfigurations.get(key).serializeNBT();
			playerConfigData.put(key.toString(), config);
		}
		for(Integer key : groupScopedConfigurations.keySet()) {
			CompoundTag config = groupScopedConfigurations.get(key).serializeNBT();
			groupConfigData.put(key.toString(), config);
		}
		allConfigData.put("playerConfigs", playerConfigData);
		allConfigData.put("groupConfigs", groupConfigData);
		return allConfigData;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		CompoundTag playerConfigData = nbt.getCompound("playerConfigs");
		CompoundTag groupConfigData = nbt.getCompound("groupConfigs");
		for(String key : playerConfigData.getAllKeys()) {
			AccessConfig config = new AccessConfig();
			config.deserializeNBT(playerConfigData.getCompound(key));
			setPlayerAccessConfig(UUID.fromString(key), config);
		}
		for(String key : groupConfigData.getAllKeys()) {
			AccessConfig config = new AccessConfig();
			config.deserializeNBT(groupConfigData.getCompound(key));
			setGroupAccessConfig(config.getOwner(), Integer.parseInt(key), config);
		}
	}
	
	public enum EnumAccessScope {
		PLAYER, //Configurations for all robots of a player
		GROUP, //Configurations for all robots of a group
		ROBOT //Configurations for an indiviual robot
	}

}