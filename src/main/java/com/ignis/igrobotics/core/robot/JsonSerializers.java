package com.ignis.igrobotics.core.robot;

import com.google.gson.*;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.capabilities.perks.PerkMap;

import java.lang.reflect.Type;

/**
 * Json Serializers for {@link Perk Perks}, {@link RobotPart Robot Parts} and {@link RobotModule Modules}
 * @author Ignis
 */
public class JsonSerializers {
	
	public static Gson initGson() {
		GsonBuilder gson = new GsonBuilder();
		
		gson.setPrettyPrinting();
		gson.serializeNulls();
		gson.disableHtmlEscaping();
		gson.registerTypeAdapter(PART.getType(), PART);
		gson.registerTypeAdapter(MODULE.getType(), MODULE);
		gson.registerTypeAdapter(PERK.getType(), PERK);
		
		return gson.create();
	}

	public static final BaseSerializer<Perk> PERK = new BaseSerializer<>() {
		@Override
		public Type getType() {
			return Perk.class;
		}
		@Override
		public JsonElement serialize(Perk src, Type typeOfSrc, JsonSerializationContext context) {
			return Perk.serialize(src);
		}
		@Override
		public Perk deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return Perk.deserialize(json);
		}
	};
	
	public static final BaseSerializer<RobotModule> MODULE = new BaseSerializer<>() {
		@Override
		public Type getType() {
			return RobotModule.class;
		}
		@Override
		public RobotModule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return RobotModule.deserialize(json);
		}
		@Override
		public JsonElement serialize(RobotModule src, Type typeOfSrc, JsonSerializationContext context) {
			throw new UnsupportedOperationException();
		}
	};
	
	public static final BaseSerializer<RobotPart> PART = new BaseSerializer<>() {
		@Override
		public Type getType() {
			return RobotPart.class;
		}

		@Override
		public JsonElement serialize(RobotPart src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			
			obj.addProperty("material", src.getMaterial().getName());
			obj.addProperty("part", src.getPart().getName());
			
			JsonElement perks = PerkMap.serialize(src.getPerks());
			obj.add("perks", perks);
			
			return obj;
		}

		@Override
		public RobotPart deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			
			EnumRobotMaterial[] materials = null;
			EnumRobotPart[] parts = null;
			
			if(obj.has("material")) {
				materials = new EnumRobotMaterial[1];
				materials[0] = EnumRobotMaterial.valueOf(obj.get("material").getAsString().toUpperCase());
			}
			if(obj.has("part")) {
				String val = obj.get("part").getAsString().toUpperCase();
				if(val.equals("ARM") || val.equals("LEG")) {
					parts = new EnumRobotPart[2];
					parts[0] = EnumRobotPart.valueOf("LEFT_" + val);
					parts[1] = EnumRobotPart.valueOf("RIGHT_" + val);
				} else {
					parts = new EnumRobotPart[1];
					parts[0] = EnumRobotPart.valueOf(val);
				}
			}
			IPerkMap perks = PerkMap.deserialize(obj.get("perks"));
			
			if(parts == null) {
				parts = EnumRobotPart.values();
			}
			if(materials == null) {
				materials = EnumRobotMaterial.valuesWithoutEmpty();
			}
			
			for(EnumRobotPart part : parts) {
				for(EnumRobotMaterial material : materials) {
					RobotPart.registerPerks(part, material, perks);
				}
			}
			
			return null;
		}
	};

}

abstract class BaseSerializer<T> implements com.google.gson.JsonSerializer<T>, JsonDeserializer<T> {
	
	public abstract Type getType();
	
}

