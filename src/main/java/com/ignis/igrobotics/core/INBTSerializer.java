package com.ignis.igrobotics.core;

import net.minecraft.nbt.CompoundTag;

public interface INBTSerializer {
	
	public void writeToNBT(CompoundTag compound);
	
	public void readFromNBT(CompoundTag compound);

}
