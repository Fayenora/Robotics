package com.ignis.igrobotics.core.capabilities.perks;

import com.ignis.igrobotics.core.IEntityHook;
import com.ignis.igrobotics.core.SimpleDataManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPerkMapCap extends IPerkMap, IEntityHook {
	
	/**
	 *  Updates attribute modifiers applied by perks. Should be called when perks changed in any way <br>
	 *  <b>Requirement</b>: This function needs to be <a href=https://en.wikipedia.org/wiki/Idempotence#Idempotent_functions>idempotent</a>
	 * @see DefaultAttributes#getSupplier(EntityType) for getting the base attribute values
	 */
	void updateAttributeModifiers();
	
	SimpleDataManager values();

}
