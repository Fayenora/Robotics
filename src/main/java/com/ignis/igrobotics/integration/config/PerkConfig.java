package com.ignis.igrobotics.integration.config;

import com.google.gson.Gson;
import com.ignis.igrobotics.common.perks.*;
import com.ignis.igrobotics.common.perks.modules.PerkFist;
import com.ignis.igrobotics.common.perks.modules.PerkGenerator;
import com.ignis.igrobotics.common.perks.modules.PerkSolarPanel;
import com.ignis.igrobotics.common.perks.modules.PerkUnarmedAttack;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.robot.JsonSerializers;
import com.ignis.igrobotics.core.util.FileUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;

import java.io.File;
import java.util.HashMap;

public class PerkConfig implements IJsonConfig {
	
	public final HashMap<String, Perk> PERKS = new HashMap<>();

	public Perk PERK_UNRELIABLE;
	public Perk PERK_MASS_PRODUCED;
	public Perk PERK_STRENGTH;
	public Perk PERK_IMPACT;
	public Perk PERK_ROBUST;
	public Perk PERK_LUMINOUS;

	public Perk PERK_GLOWING;
	public Perk PERK_ACCELERATION;
	public Perk PERK_CHARGE;
	public Perk PERK_SOLAR_PANEL;
	public Perk PERK_COAL_GENERATOR;
	public Perk PERK_BIO_GENERATOR;
	public Perk PERK_UNARMED_ATTACK;
	public Perk PERK_FIST;
	public Perk PERK_SHIELD;

	public PerkConfig() {
		PERK_UNRELIABLE = new PerkUnreliable("perk.unreliable").setDisplayColor(TextColor.fromLegacyFormat(ChatFormatting.RED));
		PERK_MASS_PRODUCED = new PerkMassProduced("perk.mass_produced").setDisplayColor(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY));
		PERK_STRENGTH = new PerkStrength("perk.strength");
		PERK_IMPACT = new PerkImpact("perk.impact");
		PERK_ROBUST = new PerkRobust("perk.robust");
		PERK_LUMINOUS = new PerkLuminous("perk.luminous").setDisplayColor(ChatFormatting.YELLOW);
		PERK_GLOWING = new PerkGlowing("perk.glowing").setDisplayColor(ChatFormatting.YELLOW);
		PERK_ACCELERATION = new PerkAcceleration("perk.acceleration");
		PERK_CHARGE = new PerkCharge("perk.charge");
		PERK_SOLAR_PANEL = new PerkSolarPanel("perk.solar_panel");
		PERK_COAL_GENERATOR = new PerkGenerator("perk.coal_generator", 2000, (stack, ent) -> stack.getItem().equals(Items.COAL) ? 8000 : 0);
		PERK_BIO_GENERATOR = new PerkGenerator("perk.bio_generator", 1500, (stack, entity) -> {
			FoodProperties stats = stack.getItem().getFoodProperties(stack, entity);
			if(stats == null) return 0;
			return (int) (stats.getNutrition() * 200 + Math.pow(stats.getSaturationModifier(), 1.5) * 100);
		});
		PERK_UNARMED_ATTACK = new PerkUnarmedAttack("perk.unarmed_attack");
		PERK_FIST = new PerkFist("perk.fist").setDisplayColor(ChatFormatting.BLUE);
		PERK_SHIELD = new Perk("perk.shield", 10).setStackable(true).setDisplayColor(ChatFormatting.AQUA);
	}

	public PerkConfig(File file) {
		load(file);
	}

	private void registerDefaultPerks() {
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
		register(PERK_UNARMED_ATTACK);
		register(PERK_FIST);
		register(PERK_SHIELD);
	}
	
	@Override
	public void load(File file) {
		PERKS.clear();
		registerDefaultPerks();
		Gson gson = JsonSerializers.initGson();
		if(!file.exists()) FileUtils.copyFromDefault("perks.json", file);
		Perk[] perks = (Perk[]) FileUtils.readJson(gson, file, Perk[].class);
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
