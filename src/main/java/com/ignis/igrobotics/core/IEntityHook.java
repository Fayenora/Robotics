package com.ignis.igrobotics.core;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;

/**
 * About to be replaced with event hooks
 */
@Deprecated
public interface IEntityHook {
	
	public default void applyEntityAttributes() {}
	
	public default void onInitialSpawn() {}
	
	public default void onEntityUpdate() {}
	
	public default void jump() {}
	
	public default void onStruckByLightning(LightningBolt lightningBolt) {}
	
	public default void onDeath(DamageSource cause) {}
	
	/**
	 * Called when the entity attacks another entity
	 * @param entityIn
	 * @return knockback to add
	 */
	public default int attackEntityAsMob(Entity entityIn) { return 0; }
	
	/**
	 * Called when the entity receives damage
	 * @param damageSrc
	 * @param damageAmount
	 * @return altered damage
	 */
	public default float damageEntity(DamageSource damageSrc, float damageAmount) { return damageAmount; }

}
