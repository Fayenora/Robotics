package com.ignis.igrobotics.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.brewing.BrewingRecipe;

public class MachineRecipe implements Recipe {

	protected final Machine machine;
	protected final RecipeType<?> type;
	protected final ResourceLocation id;
	private final Ingredient[] inputs;
	private final ItemStack[] outputs;
	private final int processingTime;
	private int energy;
	
	private MachineRecipe(Machine machine, ResourceLocation id, int processingTime, Ingredient[] inputs, ItemStack[] outputs) {
		this.machine = machine;
		this.type = machine.getRecipeType();
		this.id = id;
		this.inputs = inputs;
		this.outputs = outputs;
		this.processingTime = processingTime;
	}
	
	public MachineRecipe(Machine machine, ResourceLocation id, int processingTime, int energy, Ingredient[] inputs, ItemStack[] outputs) {
		this(machine, id, processingTime, inputs, outputs);
		this.energy = energy;
	}
	
	public MachineRecipe(Machine machine, ResourceLocation id, int processingTime, int energy) {
		this(machine, id, processingTime, energy, new Ingredient[] {}, new ItemStack[] {});
	}
	
	public Ingredient[] getInputs() {
		return inputs;
	}
	
	public ItemStack[] getOutputs() {
		return outputs;
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
		return null; //TODO
	}

	@Override
	public RecipeType<?> getType() {
		return type;
	}

	public int getProcessingTime() {
		return (int) (processingTime / machine.getProcessingSpeed());
	}

	public int getEnergyPerTick(float energy_multiplier, float processing_speed) {
		return (int) (energy / processingTime * energy_multiplier * processing_speed);
	}

	public int getEnergyPerTick() {
		return getEnergyPerTick(machine.getEnergyConsumption(), machine.getProcessingSpeed());
	}
}
