package com.ignis.igrobotics.common.modules;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.shield.IShielded;
import net.minecraft.world.entity.LivingEntity;

public class ShieldModule implements IModuleAction {

    @Override
    public boolean execute(LivingEntity caster, int duration) {
        if(caster.level.isClientSide || !caster.isAlive()) return false;
        if(!caster.getCapability(ModCapabilities.SHIELDED).isPresent()) return false;
        IShielded shield = caster.getCapability(ModCapabilities.SHIELDED).resolve().get();
        return shield.setActive(!shield.isShielded());
    }
}
