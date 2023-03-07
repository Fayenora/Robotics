package com.ignis.igrobotics.core.capabilities.perks;

import com.ignis.igrobotics.core.IEntityHook;
import com.ignis.igrobotics.core.SimpleDataManager;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability //TODO: Implement
public interface IPerkMapCap extends IPerkMap, IEntityHook {
	
	/**
	 *  Updates attribute modifiers applied by perks. Should be called when perks changed in any way <br>
	 *  <b>Requirement</b>: This function needs be be <a href=https://en.wikipedia.org/wiki/Idempotence#Idempotent_functions>idempotent</a>
	 */
	void updateAttributeModifiers();
	
	SimpleDataManager values();

}
