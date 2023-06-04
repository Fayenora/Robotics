package com.ignis.igrobotics.integration.config;

import com.google.gson.Gson;
import com.ignis.igrobotics.common.perks.*;
import com.ignis.igrobotics.common.perks.modules.PerkGenerator;
import com.ignis.igrobotics.common.perks.modules.PerkSolarPanel;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;

import java.io.File;
import java.util.HashMap;

public class PerkConfig implements IJsonConfig {
	
	public final HashMap<String, Perk> PERKS = new HashMap<>();

	public final Perk PERK_UNRELIABLE = new PerkUnreliable("perk.unreliable").setDisplayColor(TextColor.fromLegacyFormat(ChatFormatting.RED));
	public final Perk PERK_MASS_PRODUCED = new PerkMassProduced("perk.mass_produced").setDisplayColor(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY));
	public final Perk PERK_STRENGTH = new PerkStrength("perk.strength");
	public final Perk PERK_IMPACT = new PerkImpact("perk.impact");
	public final Perk PERK_ROBUST = new PerkRobust("perk.robust");
	public final Perk PERK_LUMINOUS = new PerkLuminous("perk.luminous").setDisplayColor(ChatFormatting.YELLOW);

	public final Perk PERK_GLOWING = new PerkGlowing("perk.glowing").setDisplayColor(ChatFormatting.YELLOW);
	public final Perk PERK_ACCELERATION = new PerkAcceleration("perk.acceleration");
	public final Perk PERK_CHARGE = new PerkCharge("perk.charge");
	public final Perk PERK_SOLAR_PANEL = new PerkSolarPanel("perk.solar_panel");
	public final Perk PERK_COAL_GENERATOR = new PerkGenerator("perk.coal_generator", 2000, (stack, ent) -> stack.getItem().equals(Items.COAL) ? 8000 : 0);
	public final Perk PERK_BIO_GENERATOR = new PerkGenerator("perk.bio_generator", 1500, (stack, entity) -> {
		FoodProperties stats = stack.getItem().getFoodProperties(stack, entity);
		if(stats == null) return 0;
		return (int) (stats.getNutrition() * 200 + Math.pow(stats.getSaturationModifier(), 1.5) * 100);
	});

	{
		register(PERK_UNRELIABLE);
		register(PERK_MASS_PRODUCED);
		register(PERK_STRENGTH);
		register(PERK_IMPACT);
		register(PERK_ROBUST);
		register(PERK_LUMINOUS);
		register(PERK_GLOWING);
		register(PERK_ACCELERATION);
		register(PERK_CHARGE);
		register(PERK_SOLAR_PANEL);
		register(PERK_COAL_GENERATOR);
		register(PERK_BIO_GENERATOR);
	}
	
	@Override
	public void load(File file) {
		PERKS.clear();
		Gson gson = ConfigJsonSerializer.initGson();
		if(!file.exists()) ConfigUtils.copyFromDefault("perks.json", file);
		Perk[] perks = (Perk[]) ConfigUtils.readJson(gson, file, Perk[].class);
		if(perks != null) {
			for(Perk perk : perks) {
				PERKS.put(perk.getUnlocalizedName(), perk);
			}
		}
	}
	
	@Override
	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeInt(PERKS.size());
		for(Perk perk : PERKS.values()) {
			Perk.write(buffer, perk);
		}
	}
	
	@Override
	public void fromNetwork(FriendlyByteBuf buffer) {
		PERKS.clear();
		int perkAmount = buffer.readInt();
		for(int i = 0; i < perkAmount; i++) {
			Perk perk = Perk.read(buffer);
			PERKS.put(perk.getUnlocalizedName(), perk);
		}
	}

	private void register(Perk perk) {
		PERKS.put(perk.getUnlocalizedName(), perk);
	}

}
