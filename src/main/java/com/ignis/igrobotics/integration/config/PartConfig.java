package com.ignis.igrobotics.integration.config;

import com.google.gson.Gson;
import com.ignis.igrobotics.core.capabilities.perks.PerkMap;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.core.util.Tuple;
import net.minecraft.network.FriendlyByteBuf;

import java.io.File;
import java.util.HashMap;

public class PartConfig implements IJsonConfig {

	public final HashMap<Tuple<EnumRobotPart, EnumRobotMaterial>, RobotPart> PARTS = new HashMap<>();

	@Override
	public void load(File file) {
		clearPerks();
		Gson gson = ConfigJsonSerializer.initGson();
		if(!file.exists()) ConfigUtils.copyFromDefault("robot_parts.json", file);
		ConfigUtils.readJson(gson, file, RobotPart[].class);
		//NOTE: Parts SHOULD be registered here, but the json reading takes case of that case as there may be read perks for multiple parts in one 'part' to deserialize
	}

	private void clearPerks() {
		for(EnumRobotPart part : EnumRobotPart.values()) {
			for(EnumRobotMaterial material : EnumRobotMaterial.values()) {
				RobotPart.get(part, material).getPerks().clear();
			}
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer) {
		for(Tuple<EnumRobotPart, EnumRobotMaterial> key : PARTS.keySet()) {
			buffer.writeShort(key.first.getID());
			buffer.writeShort(key.second.getID());
			if(!(PARTS.get(key).getPerks() instanceof PerkMap perkMap)) throw new RuntimeException("Something went wrong receiving the part config from the server! This is a bug, report it to the mod author!");
			PerkMap.write(buffer, perkMap);
		}
	}

	@Override
	public void fromNetwork(FriendlyByteBuf buffer) {
		PARTS.clear();
		while(buffer.isReadable()) {
			EnumRobotPart part = EnumRobotPart.byId(buffer.readShort());
			EnumRobotMaterial material = EnumRobotMaterial.byId(buffer.readShort());
			RobotPart.registerPerks(part, material, PerkMap.read(buffer));
		}
	}

}
