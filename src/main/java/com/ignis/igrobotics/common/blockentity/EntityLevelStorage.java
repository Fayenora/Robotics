package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.ignis.igrobotics.core.util.EntityFinder;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Handle an entity contained in any block entity
 */
@MethodsReturnNonnullByDefault
public class EntityLevelStorage implements INBTSerializable<CompoundTag> {

    private Level level;
    @Nullable
    private Entity stored;
    @Nullable
    private IPartBuilt parts;
    private final Supplier<BlockPos> pos;
    /** Do not load this nbt during normal world loading, but just when entering the level */
    private CompoundTag entityNBT;

    public EntityLevelStorage(Level level, LivingEntity stored, Supplier<BlockPos> pos) {
        this.level = level;
        setEntity(stored);
        this.pos = pos;
    }

    public void enterStorage(Entity entity) {
        if(stored != null) exitStorage(); //Make the previous entity exit
        Optional<Entity> newEntity = copyEntity(entity);
        newEntity.ifPresent(ent -> {
            setEntity(ent);
            entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
        });
    }

    public Optional<Entity> exitStorage(Direction facing) {
        if(stored == null) return Optional.empty();
        //Clone entity
        Optional<Entity> opt = copyEntity(this.stored);
        if(opt.isEmpty()) return Optional.empty();
        Entity entity = opt.get();

        if(facing == null) {
            facing = Direction.DOWN;
        }
        //Determine spawn location
        switch (facing) {
            case NORTH -> {
                entity.setPos(Vec3.atBottomCenterOf(pos.get().north()));
                entity.lerpHeadTo((float) Math.toRadians(180), 0);
            }
            case WEST -> {
                entity.setPos(Vec3.atBottomCenterOf(pos.get().west()));
                entity.lerpHeadTo((float) Math.toRadians(90), 0);
            }
            case EAST -> {
                entity.setPos(Vec3.atBottomCenterOf(pos.get().east()));
                entity.lerpHeadTo((float) Math.toRadians(-90), 0);
            }
            case SOUTH -> {
                entity.setPos(Vec3.atBottomCenterOf(pos.get().south()));
                entity.lerpHeadTo(0, 0);
            }
            default -> {
                entity.setPos(Vec3.atBottomCenterOf(pos.get()));
                entity.lerpHeadTo(0, 0);
            }
        }
        level.addFreshEntity(entity);
        clearEntity();

        return Optional.of(entity);
    }

    public Optional<Entity> exitStorage() {
        return exitStorage(level.getBlockState(pos.get()).getValue(MachineBlock.FACING));
    }

    public Optional<Entity> createNewRobot(UUID owner) {
        if(level.isClientSide) return Optional.empty();

        //Limit amount of robots

        int ownedRobots = EntityFinder.getRobotics(level, robot -> owner.equals(robot.getOwner())).size();
        if(ownedRobots >= RoboticsConfig.general.robotAmountPerPlayerOnServer.get()) {
            Component warnMessage = Component.translatable("igrobotics.too_many_robots").setStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.YELLOW)));
            Player player = level.getPlayerByUUID(owner);
            if(player != null) player.sendSystemMessage(warnMessage);
            return Optional.empty();
        }

        Optional<Entity> entity = exitStorage();
        entity.ifPresent(ent -> ent.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> robot.setOwner(owner)));

        return entity;
    }

    public void setRobotPart(EnumRobotPart part, EnumRobotMaterial material) {
        if(parts == null) return;
        parts.setBodyPart(RobotPart.get(part, material));
    }

    public void clearEntity() {
        stored = null;
        parts = null;
    }

    public void setEntity(Entity entity) {
        if(entity == null) return;
        stored = entity;
        stored.setXRot(0);
        stored.setYRot(0);
        stored.setYBodyRot(0);
        stored.setYHeadRot(0);
        stored.getCapability(ModCapabilities.PARTS, null).ifPresent(parts -> this.parts = parts);
    }

    @Override
    public CompoundTag serializeNBT() {
        if(stored == null) return new CompoundTag();
        return stored.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if(level != null) {
            deserializeEntity(nbt);
        } else {
            entityNBT = nbt;
        }
    }

    public void setLevel(Level level) {
        this.level = level;
        deserializeEntity(entityNBT);
    }

    public void deserializeEntity(CompoundTag tag) {
        if(tag == null || level == null) return;
        Optional<Entity> entity = EntityType.create(tag, level);
        if(entity.isPresent() && entity.get() instanceof LivingEntity living) {
            setEntity(living);
        } else clearEntity();
    }

    public Optional<Entity> getEntity() {
        return stored != null ? Optional.of(stored) : Optional.empty();
    }

    public static Optional<Entity> copyEntity(Entity toCopy) {
        CompoundTag tag = toCopy.serializeNBT();
        return EntityType.create(tag, toCopy.level());
    }
}
