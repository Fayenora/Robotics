package com.ignis.igrobotics.integration.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.network.FriendlyByteBuf;

import java.io.File;
import java.util.HashMap;

public class PerkConfig implements IJsonConfig {
	
	public final HashMap<String, Perk> PERKS = new HashMap<String, Perk>();

	public Perk PERK_LUMINOUS;

	/*
	public final Perk PERK_STRENGTH = new PerkStrength(this, "perk.strength");
	public final Perk PERK_IMPACT = new PerkImpact(this, "perk.impact");
	public final Perk PERK_ROBUST = new PerkRobust(this, "perk.robust");
	public final Perk PERK_UNRELIABLE = new PerkUnrelieable(this, "perk.unreliable").setDisplayColor(TextFormatting.RED);
	public final Perk PERK_LUMINOUS = new PerkLuminous(this, "perk.luminous").setDisplayColor(TextFormatting.YELLOW);
	public final Perk PERK_MASSPRODUCED = new PerkMassProduced(this, "perk.mass_produced").setDisplayColor(TextFormatting.DARK_GRAY);
	public final Perk PERK_GLOWING = new PerkGlowing(this, "perk.glowing").setDisplayColor(TextFormatting.YELLOW);
	public final Perk PERK_ACCELERATION = new PerkAcceleration(this, "perk.acceleration");
	public final Perk PERK_CHARGE = new PerkCharge(this, "perk.charge");
	public final Perk PERK_SOLAR_PANEL = new PerkSolarPanel(this, "perk.solar_panel");
	public final Perk PERK_COAL_GENERATOR = new PerkGenerator(this, "perk.coal_generator", 2000, (stack) -> stack.getItem().equals(Items.COAL) ? 8000 : 0);
	public final Perk PERK_BIO_GENERATOR = new PerkGenerator(this, "perk.bio_generator", 1500, (stack) -> {
		if(!(stack.getItem() instanceof ItemFood)) return 0;
		ItemFood food = (ItemFood) stack.getItem();
		int foodRestore = food.getHealAmount(stack);
		float saturationRestore = food.getSaturationModifier(stack);
		return (int) (foodRestore * 200 + Math.pow(saturationRestore, 1.5) * 100);
	});
	 */
	
	@Override
	public void load(File file) {
		Gson gson = ConfigJsonSerializer.initGson();
		if(!file.exists()) ConfigUtils.copyFromDefault("perks.json", file);
		Perk[] perks = (Perk[]) ConfigUtils.readJson(gson, file, Perk[].class);
		for(Perk perk : perks) {
			PERKS.put(perk.getUnlocalizedName(), perk);
		}
	}
	
	@Override
	public void toNetwork(FriendlyByteBuf buffer) {
		//TODO Write the perks individually here
		JsonArray array = new JsonArray();
		for(Perk perk : PERKS.values()) {
			array.add(Perk.serialize(perk));
		}
		buffer.writeBytes(array.toString().getBytes());
	}
	
	@Override
	public void fromNetwork(FriendlyByteBuf buffer) {
		//TODO Read the perks individually here
		JsonArray array = (JsonArray) JsonParser.parseString(new String(buffer.array()));
		for(int i = 0; i < array.size(); i++) {
			Perk perk = Perk.deserialize(array.get(i));
			PERKS.put(perk.getUnlocalizedName(), perk);
		}
	}

}
