package com.ignis.igrobotics.integration.config;

import com.google.gson.Gson;
import com.ignis.igrobotics.core.RobotPart;
import net.minecraft.network.FriendlyByteBuf;

import java.io.File;
import java.util.HashMap;

public class PartConfig implements IJsonConfig {
	
	public final HashMap<RobotPart.EnumRobotPart, HashMap<RobotPart.EnumRobotMaterial, RobotPart>> PARTS = new HashMap<>();
	
	@Override
	public void load(File file) {
		Gson gson = ConfigJsonSerializer.initGson();
		if(!file.exists()) ConfigUtils.copyFromDefault("robot_parts.json", file);
		RobotPart[] parts = (RobotPart[]) ConfigUtils.readJson(gson, file, RobotPart[].class);
		//TODO register the parts
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
			RobotPart.get(RobotPart.EnumRobotPart.HEAD, RobotPart.EnumRobotMaterial.IRON),
			RobotPart.get(RobotPart.EnumRobotPart.BODY, RobotPart.EnumRobotMaterial.IRON),
			RobotPart.get(RobotPart.EnumRobotPart.LEFT_ARM, RobotPart.EnumRobotMaterial.IRON),
			RobotPart.get(RobotPart.EnumRobotPart.RIGHT_ARM, RobotPart.EnumRobotMaterial.IRON),
			RobotPart.get(RobotPart.EnumRobotPart.LEFT_LEG, RobotPart.EnumRobotMaterial.IRON),
			RobotPart.get(RobotPart.EnumRobotPart.RIGHT_LEG, RobotPart.EnumRobotMaterial.IRON)
		};
	}

}
