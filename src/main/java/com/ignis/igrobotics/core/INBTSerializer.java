package com.ignis.igrobotics.core;

import net.minecraft.nbt.CompoundTag;

public interface INBTSerializer {
	
	void writeToNBT(CompoundTag compound);
	
	void readFromNBT(CompoundTag compound);

}
