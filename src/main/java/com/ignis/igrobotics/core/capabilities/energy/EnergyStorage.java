package com.ignis.igrobotics.core.capabilities.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class EnergyStorage extends net.minecraftforge.energy.EnergyStorage implements ModifiableEnergyStorage {
	
	public EnergyStorage(int capacity) {
        super(capacity, capacity, capacity, 0);
    }

    public EnergyStorage(int capacity, int maxTransfer) {
    	super(capacity, maxTransfer, maxTransfer, 0);
    }

	@Override
	public Tag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("Energy", this.energy);
		nbt.putInt("Capacity", this.capacity);
		nbt.putInt("MaxReceive", this.maxReceive);
		nbt.putInt("MaxExtract", this.maxExtract);
		return nbt;
	}

	@Override
	public void deserializeNBT(Tag tag) {
		if(!(tag instanceof CompoundTag nbt)) return;
		this.capacity = nbt.getInt("Capacity");
		this.maxReceive = nbt.getInt("MaxReceive");
		this.maxExtract = nbt.getInt("MaxExtract");
		setEnergy(nbt.getInt("Energy"));
	}

	@Override
    public void setEnergy(int value) {
    	this.energy = Math.max(0, Math.min(value, getMaxEnergyStored()));
    }

	@Override
    public void setMaxEnergyStored(int value) {
    	this.capacity = value;
    	this.energy = Math.min(energy, getMaxEnergyStored());
    }

}
