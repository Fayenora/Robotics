package com.ignis.igrobotics.core;

import net.minecraft.nbt.CompoundTag;

/**
 * Replace with {@link net.minecraftforge.common.util.INBTSerializable}
 */
@Deprecated
public interface INBTSerializer {
	
	void writeToNBT(CompoundTag compound);
	
	void readFromNBT(CompoundTag compound);

}
