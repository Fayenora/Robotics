package com.ignis.igrobotics.common;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.shield.IShielded;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShieldBehavior {

    @SubscribeEvent
    public static void shieldedEntityHit(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if(!entity.getCapability(ModCapabilities.SHIELDED).isPresent()) return;
        if(entity.invulnerableTime > 0) {
            event.setCanceled(true);
            return;
        }
        IShielded shield = entity.getCapability(ModCapabilities.SHIELDED).resolve().get();
        if(!shield.isShielded()) return;
        DamageSource source = event.getSource();
        if(     source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ||
                source.is(DamageTypeTags.BYPASSES_EFFECTS) ||
                source.is(DamageTypeTags.NO_IMPACT)) return;

        shield.damage(event.getAmount());
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void projectileHit(ProjectileImpactEvent event) {
        Level level = event.getProjectile().level;
        if(event.getRayTraceResult().getType() != HitResult.Type.ENTITY) return;
        Projectile projectile = event.getProjectile();
        Predicate<Entity> predicate = EntitySelector.NO_SPECTATORS.and(Entity::canBeHitByProjectile);
        List<Entity> list = level.getEntities(projectile, projectile.getBoundingBox().expandTowards(projectile.getDeltaMovement()), predicate);
        if(list.size() == 0) return;
        Entity primaryTarget = list.get(0);
        for(Entity ent : list) {
            if(event.getRayTraceResult().distanceTo(ent) < event.getRayTraceResult().distanceTo(primaryTarget)) {
                primaryTarget = ent;
            }
        }
        if(!primaryTarget.getCapability(ModCapabilities.SHIELDED).isPresent()) return;
        IShielded shield = primaryTarget.getCapability(ModCapabilities.SHIELDED).resolve().get();
        if(!shield.isShielded()) return;
        Vec3 forcePush = projectile.position().subtract(primaryTarget.getBoundingBox().getCenter()).scale(1);
        projectile.addDeltaMovement(forcePush);

        if(!level.isClientSide) {
            shield.damage(1);
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void shieldRecharge(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.level.isClientSide()) return;
        if(entity.tickCount % 4 != 0) return;
        entity.getCapability(ModCapabilities.SHIELDED).ifPresent(IShielded::recharge);
    }

}
