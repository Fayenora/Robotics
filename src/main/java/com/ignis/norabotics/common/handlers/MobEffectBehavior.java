package com.ignis.norabotics.common.handlers;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.content.entity.StompedUpBlockEntity;
import com.ignis.norabotics.definitions.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobEffectBehavior {

    @SubscribeEvent
    public static void fall(LivingFallEvent event) {
        int impactLevel = impactEffect(event.getEntity(), event.getDistance());
        float percDamageReduction = Math.min(impactLevel + 1, 3) / 3f;
        event.setDamageMultiplier(event.getDamageMultiplier() * (1 - percDamageReduction));
    }

    @SubscribeEvent
    public static void creativeFall(PlayerFlyableFallEvent event) {
        impactEffect(event.getEntity(), event.getDistance());
    }

    @SubscribeEvent
    public static void onDamage(LivingHurtEvent event) {
        Entity causingEntity = event.getSource().getEntity();
        LivingEntity targetEntity = event.getEntity();
        if(causingEntity != null && targetEntity.hasEffect(ModMobEffects.ARMOR_SHRED.get()) && !causingEntity.equals(targetEntity.getLastHurtByMob())) {
            int currentAmplifier = targetEntity.getEffect(ModMobEffects.ARMOR_SHRED.get()).getAmplifier();
            targetEntity.addEffect(new MobEffectInstance(ModMobEffects.ARMOR_SHRED.get(), 200, currentAmplifier + 1));
        }
    }

    private static int impactEffect(LivingEntity entity, float fallingDistance) {
        if(fallingDistance < 4) return 0;
        if(entity.level().isClientSide) return 0;
        if(!entity.hasEffect(ModMobEffects.IMPACTFUL.get())) return 0;
        int effectLevel = entity.getEffect(ModMobEffects.IMPACTFUL.get()).getAmplifier();

        float damage = (float) (effectLevel + fallingDistance * 0.3);

        createShockWave(entity, entity.position().add(entity.getDeltaMovement()), damage, 5, 8, 0.1f);
        entity.removeEffect(ModMobEffects.IMPACTFUL.get());
        return effectLevel;
    }

    public static void createShockWave(Entity owner, Vec3 location, float damage, float radius, float depth, float intensity) {
        Level level = owner.level();
        List<StompedUpBlockEntity> stompedUpBlocks = new ArrayList<>();
        // Consider all blocks with manhattan distance r to the center.
        // Only looking from the top, consider how much of the block area is covered by the impact circle.
        // Based on how much area is covered and the distance to the impact center, spawn a stomped up block with the given velocity
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int x = (int) (location.x - radius); x < location.x + radius; x++) {
            pos.setX(x);
            for(int z = (int) (location.z - radius); z < location.z + radius; z++) {
                pos.setZ(z);
                pos.setY((int) location.y);
                while(pos.getY() > location.y - radius && !level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
                    pos.move(Direction.DOWN);
                }
                double distToEpicenter = location.distanceTo(pos.getCenter());
                if(distToEpicenter > radius - 1 || distToEpicenter < 2) continue;
                StompedUpBlockEntity stompedUpBlock = new StompedUpBlockEntity(level, pos, damage);
                Vec3 direction = pos.getCenter().subtract(location.relative(Direction.DOWN, depth)).normalize();
                direction = direction.scale(intensity * distToEpicenter);
                stompedUpBlock.addDeltaMovement(direction);
                stompedUpBlocks.add(stompedUpBlock);
            }
        }

        for(Entity ent : owner.level().getEntities(owner, owner.getBoundingBox().inflate(radius), e -> e.distanceTo(owner) < radius)) {
            ent.hurt(ent.damageSources().explosion(owner, ent), damage);
        }
        for(StompedUpBlockEntity stompedBlock : stompedUpBlocks) {
            level.addFreshEntity(stompedBlock);
        }
    }
}
