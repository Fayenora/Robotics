package com.ignis.igrobotics.integration.config;

import com.google.gson.Gson;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import net.minecraft.network.FriendlyByteBuf;

import java.io.File;
import java.util.HashMap;

public class PartConfig implements IJsonConfig {
	
	public final HashMap<EnumRobotPart, HashMap<EnumRobotMaterial, RobotPart>> PARTS = new HashMap<>();
	
	@Override
	public void load(File file) {
		clearPerks();
		Gson gson = ConfigJsonSerializer.initGson();
		if(!file.exists()) ConfigUtils.copyFromDefault("robot_parts.json", file);
		RobotPart[] parts = (RobotPart[]) ConfigUtils.readJson(gson, file, RobotPart[].class);
		//NOTE Parts SHOULD be registered here, but the json reading takes case of that case as there may be read perks for multiple parts in one 'part' to deserialize
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
		// TODO Auto-generated method stub
	}
	
	@Override
	public void fromNetwork(FriendlyByteBuf buffer) {
		// TODO Auto-generated method stub
	}

	public RobotPart[] getDefaultParts() {
		return new RobotPart[] {
			RobotPart.get(EnumRobotPart.HEAD, EnumRobotMaterial.IRON),
			RobotPart.get(EnumRobotPart.BODY, EnumRobotMaterial.IRON),
			RobotPart.get(EnumRobotPart.LEFT_ARM, EnumRobotMaterial.IRON),
			RobotPart.get(EnumRobotPart.RIGHT_ARM, EnumRobotMaterial.IRON),
			RobotPart.get(EnumRobotPart.LEFT_LEG, EnumRobotMaterial.IRON),
			RobotPart.get(EnumRobotPart.RIGHT_LEG, EnumRobotMaterial.IRON)
		};
	}

}
