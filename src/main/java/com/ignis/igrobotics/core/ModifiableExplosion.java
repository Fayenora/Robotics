package com.ignis.igrobotics.core;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;
import java.util.Set;

public class ModifiableExplosion extends Explosion {

    protected final Level level;
    protected final Entity source;
    protected final double x, y, z;
    protected final float radius, damage;

    public ModifiableExplosion(Entity entity, float damage, float radius, boolean fire, BlockInteraction blockInteraction) {
        super(entity.level, entity, entity.getX(), entity.getY(), entity.getZ(), radius, fire, blockInteraction);
        this.level = entity.level;
        this.source = entity;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.radius = radius;
        this.damage = damage;
    }

    //Mostly the vanilla code with modified damage & knockback
    @Override
    public void explode() {
        level.gameEvent(source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.newHashSet();

        for(int j = 0; j < 16; ++j) {
            for(int k = 0; k < 16; ++k) {
                for(int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (float)j / 15.0F * 2.0F - 1.0F;
                        double d1 = (float)k / 15.0F * 2.0F - 1.0F;
                        double d2 = (float)l / 15.0F * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double d4 = this.x;
                        double d6 = this.y;
                        double d8 = this.z;

                        for(float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                            BlockState blockstate = this.level.getBlockState(blockpos);
                            FluidState fluidstate = this.level.getFluidState(blockpos);
                            if (!this.level.isInWorldBounds(blockpos)) {
                                break;
                            }

                            f -= (Math.max(blockstate.getExplosionResistance(level, blockpos, this), fluidstate.getExplosionResistance(level, blockpos, this)) + 0.3F) * 0.3F;

                            if (f > 0.0F) {
                                set.add(blockpos);
                            }

                            d4 += d0 * (double)0.3F;
                            d6 += d1 * (double)0.3F;
                            d8 += d2 * (double)0.3F;
                        }
                    }
                }
            }
        }
        getToBlow().addAll(set);

        List<Entity> affectedEntities = level.getEntities(source, source.getBoundingBox().inflate(radius), ent -> ent.distanceTo(source) < radius);
        ForgeEventFactory.onExplosionDetonate(this.level, this, affectedEntities, radius * 2);
        for(Entity entity : affectedEntities) {
            if(entity.ignoreExplosion()) continue;
            if(entity.isInvulnerableTo(getDamageSource())) continue;

            // Hurt
            double relDistanceToCenter = entity.distanceTo(source) / (radius * 2d);
            double percEntExposedToExpl = getSeenPercent(source.position(), entity);
            double d10 = (1.0D - Math.pow(relDistanceToCenter, 3)) * percEntExposedToExpl;
            entity.hurt(this.getDamageSource(), (int)(d10 * 7.0D * radius + damage));

            // Knockback
            double knockbackForce;
            if (entity instanceof LivingEntity livingentity) {
                knockbackForce = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingentity, d10);
            } else {
                knockbackForce = d10;
            }
            Vec3 center = (entity instanceof PrimedTnt ? entity.position() : entity.getEyePosition());
            Vec3 knockback = center.subtract(source.position()).normalize().scale(knockbackForce);
            entity.addDeltaMovement(knockback);
            if (entity instanceof Player player && !player.isSpectator() && !player.isCreative()) {
                getHitPlayers().put(player, knockback);
            }
        }
    }

    @Override
    public void finalizeExplosion(boolean p_46076_) {
        super.finalizeExplosion(p_46076_);
        source.discard();
    }
}
