package com.ignis.igrobotics.common.content.perks.modules;

import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;
import com.ignis.igrobotics.common.helpers.types.SimpleDataManager;
import com.ignis.igrobotics.common.helpers.util.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PerkGenerator extends Perk {
	
	BiFunction<ItemStack, Mob, Integer> validInputs;
	public final String COOLDOWN;
	public final Supplier<Integer> energyGenerationRate;
	
	public PerkGenerator(String name, Supplier<Integer> energyGeneration, BiFunction<ItemStack, Mob, Integer> validInputs) {
		super(name, 1);
		this.validInputs = validInputs;
		COOLDOWN = name + ".cooldown";
		energyGenerationRate = energyGeneration;
	}
	
	@Override
	public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
		Optional<IEnergyStorage> optionalEnergy = entity.getCapability(ForgeCapabilities.ENERGY).resolve();
		Optional<IItemHandler> optionalInventory = entity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
		if(!entity.getCapability(ForgeCapabilities.ENERGY).isPresent()) return;
		if(!entity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) return;
		IEnergyStorage energy = optionalEnergy.get();
		IItemHandler inventory = optionalInventory.get();
		values.decrement(COOLDOWN);
		if(values.get(COOLDOWN) > 0) return;

		for(int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.extractItem(i, 1, true);
			if(stack.isEmpty()) continue;
			int energyToRestore = validInputs.apply(stack, entity);
			if(energyToRestore == 0) continue;
			if(energy.receiveEnergy(energyToRestore, true) != energyToRestore) continue; //Don't waste any precious energy!
			
			energy.receiveEnergy(energyToRestore, false);
			inventory.extractItem(i, 1, false);
			values.set(COOLDOWN, energyToRestore / this.energyGenerationRate.get()); //Put in some cooldown
			break;
		}
	}

	@Override
	public Component getDisplayText(int level) {
		return localized().withStyle(Style.EMPTY.withColor(displayColor));
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.generator.desc", energyGenerationRate.get());
	}
}
