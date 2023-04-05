package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import software.bernie.shadowed.eliotlash.mclib.utils.MathHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class RobotLevelStorage implements INBTSerializable<CompoundTag> {

    private Level level;
    @Nullable
    private LivingEntity stored;
    @Nullable
    private IPartBuilt parts;
    private final Supplier<BlockPos> pos;
    /** Do not load this nbt during normal world loading, but just when entering the level */
    private CompoundTag entityNBT;

    public RobotLevelStorage(Level level, LivingEntity stored, Supplier<BlockPos> pos) {
        this.level = level;
        setRobot(stored);
        this.pos = pos;
    }

    public void enterStorage(LivingEntity robot) {
        if(containsRobot()) exitStorage(); //Make the previous robot exit
        setRobot(robot);
        robot.remove(Entity.RemovalReason.CHANGED_DIMENSION);
    }

    @Nullable
    public LivingEntity exitStorage() {
        if(stored == null) return null;
        //Clone entity
        RobotEntity robot = new RobotEntity(level);
        robot.deserializeNBT(this.stored.serializeNBT());

        //Determine spawn location
        Direction facing = level.getBlockState(pos.get()).getValue(MachineBlock.FACING);
        switch (facing) {
            case NORTH -> {
                robot.setPos(Vec3.atCenterOf(pos.get().north()));
                robot.lerpHeadTo(MathHelper.wrapDegrees(180.0F), 0);
            }
            case WEST -> {
                robot.setPos(Vec3.atCenterOf(pos.get().west()));
                robot.lerpHeadTo(MathHelper.wrapDegrees(90.0F), 0);
            }
            case EAST -> {
                robot.setPos(Vec3.atCenterOf(pos.get().east()));
                robot.lerpHeadTo(MathHelper.wrapDegrees(-90.0F), 0);
            }
            case SOUTH -> {
                robot.setPos(Vec3.atCenterOf(pos.get().south()));
                robot.lerpHeadTo(0, 0);
            }
            default -> {
                robot.setPos(Vec3.atCenterOf(pos.get()));
                robot.lerpHeadTo(0, 0);
            }
        }

        //Prepare Spawning
        robot.setUUID(UUID.randomUUID()); //Give the robot a new UUID to prevent a bug when str-middle clicking the block in creative mode (those entities would have the same UUID and consequently won't spawn)
        robot.level = level;

        //Spawn
        level.addFreshEntity(robot);
        clearRobot();

        return robot;
    }

    public LivingEntity createNewRobot(UUID owner) {
        if(!containsRobot() || level.isClientSide) return null;

        //Limit amount of robots
        int ownedRobots = 0; //TODO: level.getEntities(RobotEntity.class, (robot) -> owner.equals(robot.getOwner())).size();
        if(ownedRobots >= RoboticsConfig.general.robotAmountPerPlayerOnServer.get()) {
            Component warnMessage = Component.translatable("igrobotics.too_many_robots").setStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.YELLOW)));
            level.getPlayerByUUID(owner).sendSystemMessage(warnMessage);
            return null;
        }

        LivingEntity entity = exitStorage();
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> robot.setOwner(owner));

        return entity;
    }

    public void setRobotPart(EnumRobotPart part, EnumRobotMaterial material) {
        if(parts == null) return;
        parts.setBodyPart(RobotPart.get(part, material));
    }

    public void clearRobot() {
        RobotEntity robot = new RobotEntity(level);
        setRobot(robot);
        parts.clear();
    }

    public void setRobot(LivingEntity robot) {
        if(robot == null) return;
        stored = robot;
        stored.setXRot(0);
        stored.setYRot(0);
        stored.setYBodyRot(0);
        stored.setYHeadRot(0);
        parts = stored.getCapability(ModCapabilities.PARTS, null).orElse(null);
    }

    @Override
    public CompoundTag serializeNBT() {
        if(stored == null) return new CompoundTag();
        return stored.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        entityNBT = nbt;
    }

    public void setLevel(Level level) {
        this.level = level;
        deserializeEntity(entityNBT);
    }

    public void deserializeEntity(CompoundTag tag) {
        if(tag == null || level == null) return;
        Optional<Entity> entity = EntityType.create(tag, level);
        if(entity.isPresent() && entity.get() instanceof LivingEntity living) {
            setRobot(living);
        }
    }

    public LivingEntity getRobot() {
        return stored;
    }

    public boolean containsRobot() {
        return stored != null && (parts == null || parts.hasAnyBodyPart());
    }
}
