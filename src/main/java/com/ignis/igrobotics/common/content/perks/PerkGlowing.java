package com.ignis.igrobotics.common.content.perks;

import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;

public class PerkGlowing extends Perk {

	public PerkGlowing(String name) {
		super(name, 1);
	}

    /*
	@Override
	public void onEntityUpdate(int level, Entity entity, SimpleDataManager values) {
		entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, PerkBehavior.PERK_TICK_RATE * 2, 0, true, false));
		entity.world.setLightFor(EnumSkyBlock.BLOCK, entity.getPosition(), 15);
		entity.world.markBlockRangeForRenderUpdate(entity.getPosition().add(-6, -6, -6), entity.getPosition().add(6, 6, 6));
		entity.world.checkLightFor(EnumSkyBlock.BLOCK, entity.getPosition().add(1, 0, 0));
		entity.world.checkLightFor(EnumSkyBlock.BLOCK, entity.getPosition().add(-1, 0, 0));
		entity.world.checkLightFor(EnumSkyBlock.BLOCK, entity.getPosition().add(0, 1, 0));
		entity.world.checkLightFor(EnumSkyBlock.BLOCK, entity.getPosition().add(0, -1, 0));
		entity.world.checkLightFor(EnumSkyBlock.BLOCK, entity.getPosition().add(0, 0, 1));
		entity.world.checkLightFor(EnumSkyBlock.BLOCK, entity.getPosition().add(0, 0, -1));
	}
	*/
}
