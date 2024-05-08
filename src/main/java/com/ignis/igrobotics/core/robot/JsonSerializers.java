package com.ignis.igrobotics.core.robot;

import com.google.gson.*;
import com.ignis.igrobotics.core.capabilities.perks.Perk;

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

}

abstract class BaseSerializer<T> implements com.google.gson.JsonSerializer<T>, JsonDeserializer<T> {
	
	public abstract Type getType();
	
}

