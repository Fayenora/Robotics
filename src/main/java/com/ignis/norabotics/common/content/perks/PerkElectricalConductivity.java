package com.ignis.norabotics.common.content.perks;

import com.ignis.norabotics.common.capabilities.impl.perk.Perk;
import com.ignis.norabotics.common.helpers.types.SimpleDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class PerkElectricalConductivity extends Perk {

    public PerkElectricalConductivity(String name) {
        super(name);
    }

    @Override
    public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
        Level world = entity.level();
        BlockPos pos = entity.blockPosition();
        if(world.isThundering() && world.isRainingAt(pos) && entity.getRandom().nextInt(100000) < level * 2) {
            DifficultyInstance difficultyinstance = world.getCurrentDifficultyAt(pos);
            boolean isHarmless = world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && world.random.nextDouble() < (double)difficultyinstance.getEffectiveDifficulty() * 0.01D && !world.getBlockState(pos.below()).is(Blocks.LIGHTNING_ROD);
            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(world);
            if (lightningbolt != null) {
                lightningbolt.moveTo(Vec3.atBottomCenterOf(pos));
                lightningbolt.setVisualOnly(isHarmless);
                world.addFreshEntity(lightningbolt);
            }
        }
    }
}
