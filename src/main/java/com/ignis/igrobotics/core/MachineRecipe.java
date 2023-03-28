package com.ignis.igrobotics.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class MachineRecipe<T extends Container> implements Recipe<T> {

	protected final Machine machine;
	protected final ResourceLocation id;
	public Ingredient[] inputs;
	public ItemStack[] outputs;
	public int processingTime = 20;
	public int energy = 0;
	
	public MachineRecipe(Machine machine, ResourceLocation id) {
		this.machine = machine;
		this.id = id;
	}

	@Override
	public boolean matches(Container inv, Level worldIn) {
		for (int i = 0; i < inputs.length; i++) {
			if (!this.inputs[i].test(inv.getItem(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(Container inv) {
		return outputs[0].copy();
	}

	@Override
	public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
		return true;
	}

	@Override
	public ItemStack getResultItem() {
		return outputs[0];
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return machine.getRecipeSerializer();
	}

	@Override
	public RecipeType<?> getType() {
		return machine.getRecipeType();
	}

	public int getProcessingTime() {
		return (int) (processingTime / machine.getProcessingSpeed());
	}

	public int getEnergy() {
		return energy;
	}

	public int getEnergyPerTick(float energy_multiplier, float processing_speed) {
		return (int) (energy / processingTime * energy_multiplier * processing_speed);
	}

	public int getEnergyPerTick() {
		return getEnergyPerTick(machine.getEnergyConsumption(), machine.getProcessingSpeed());
	}

	public Ingredient[] getInputs() {
		return inputs;
	}

	public ItemStack[] getOutputs() {
		return outputs;
	}

	public static class Builder {
		private final MachineRecipe recipe;
		public Builder(Machine machine, ResourceLocation id) {
			recipe = new MachineRecipe(machine, id);
		}
		public Builder setOutputs(ItemStack[] outputs) {
			recipe.outputs = outputs;
			return this;
		}
		public Builder setOutput(ItemStack output) {
			return this.setOutputs(new ItemStack[] {output});
		}
		public Builder setInputs(Ingredient[] inputs) {
			recipe.inputs = inputs;
			return this;
		}
		public Builder setEnergyRequirement(int energy) {
			recipe.energy = energy;
			return this;
		}
		public Builder setProcessingTime(int processingTime) {
			recipe.processingTime = processingTime;
			return this;
		}
		public MachineRecipe build() {
			return recipe;
		}
	}
}
