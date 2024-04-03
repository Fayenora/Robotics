package com.ignis.igrobotics.common.entity;

import com.ignis.igrobotics.definitions.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class StompedUpBlockEntity extends ThrowableProjectile {

    private float damage = 4f;
    private BlockState blockState;

    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(StompedUpBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public StompedUpBlockEntity(Level level) {
        super(ModEntityTypes.STOMPED_BLOCK.get(), level);
    }

    public StompedUpBlockEntity(Level level, BlockPos pos, float damage) {
        this(level);
        setStartPos(new BlockPos(pos));
        setPos(pos.getCenter());
        this.damage = damage;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitresult) {
        Entity target = hitresult.getEntity();
        if(target.isAttackable()) {
            target.hurt(target.damageSources().fallingBlock(getOwner()), damage);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitresult) {
        super.onHitBlock(hitresult);
        if(hitresult.isInside()) {
            kill();
        }
    }

    @Override
    public void tick() {
        if(blockState == null || blockState.isAir()) {
            discard();
        }
        //Collision
        HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if((hitresult.getType() == HitResult.Type.ENTITY || (hitresult.getType() == HitResult.Type.BLOCK && tickCount > 5))
                && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            this.onHit(hitresult);
        }
        //Movement
        Vec3 vec3 = this.getDeltaMovement();
        double d2 = this.getX() + vec3.x;
        double d0 = this.getY() + vec3.y;
        double d1 = this.getZ() + vec3.z;
        this.updateRotation();
        float f;
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float f1 = 0.25F;
                this.level.addParticle(ParticleTypes.BUBBLE, d2 - vec3.x * 0.25D, d0 - vec3.y * 0.25D, d1 - vec3.z * 0.25D, vec3.x, vec3.y, vec3.z);
            }

            f = 0.8F;
        } else {
            f = 0.99F;
        }

        this.setDeltaMovement(vec3.scale((double) f));
        if (!this.isNoGravity()) {
            Vec3 vec31 = this.getDeltaMovement();
            this.setDeltaMovement(vec31.x, vec31.y - (double) this.getGravity(), vec31.z);
        }

        this.setPos(d2, d0, d1);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("startPos", NbtUtils.writeBlockPos(getStartPos()));
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        setStartPos(NbtUtils.readBlockPos(compound.getCompound("startPos")));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if(data.equals(DATA_START_POS)) {
            this.blockState = level.getBlockState(getStartPos());
        }
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    protected void setStartPos(BlockPos pos) {
        entityData.set(DATA_START_POS, pos);
        this.blockState = level.getBlockState(pos);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false; //No fluid interaction behavior
    }
}
