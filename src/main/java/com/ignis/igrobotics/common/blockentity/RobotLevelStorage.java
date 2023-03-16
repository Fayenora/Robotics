package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.common.blocks.BlockMachine;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.RobotPart;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.shadowed.eliotlash.mclib.utils.MathHelper;

import java.util.UUID;
import java.util.function.Supplier;

public class RobotLevelStorage {

    private final Level level;
    private RobotEntity storedRobot;
    private IPartBuilt parts;
    private Supplier<BlockPos> pos;

    public RobotLevelStorage(Level level, RobotEntity storedRobot, Supplier<BlockPos> pos) {
        this.level = level;
        this.storedRobot = storedRobot;
        this.pos = pos;
    }

    public void setRobotPart(RobotPart.EnumRobotPart part, RobotPart.EnumRobotMaterial material) {
        parts.setBodyPart(RobotPart.get(part, material));
    }

    public void clearRobot() {
        if(parts == null) return;
        RobotEntity robot = new RobotEntity(level);
        parts.clear();
        setRobot(robot);
    }

    public void setRobot(RobotEntity robot) {
        storedRobot = robot;
        storedRobot.setXRot(0);
        storedRobot.setYRot(0);
        storedRobot.setYBodyRot(0);
        storedRobot.setYHeadRot(0);
        parts = storedRobot.getCapability(ModCapabilities.PART_BUILT_CAPABILITY, null).orElse(null);
    }

    public RobotEntity createNewRobot(UUID owner) {
        if(!parts.hasAnyBodyPart() || level.isClientSide) return null;

        //Limit amount of robots
        int ownedRobots = 0; //TODO: level.getEntities(RobotEntity.class, (robot) -> owner.equals(robot.getOwner())).size();
        if(ownedRobots >= RoboticsConfig.current().general.robotAmountPerPlayerOnServer.get()) {
            Component warnMessage = Component.translatable("igrobotics.too_many_robots").setStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.YELLOW)));
            level.getPlayerByUUID(owner).sendSystemMessage(warnMessage);
            return null;
        }

        //Clone entity
        RobotEntity robot = new RobotEntity(level);
        robot.deserializeNBT(this.storedRobot.serializeNBT());

        //Determine spawn location
        Direction facing = level.getBlockState(pos.get()).getValue(BlockMachine.FACING);
        switch(facing) {
            case NORTH:
                robot.setPos(Vec3.atCenterOf(pos.get().north()));
                robot.lerpHeadTo(MathHelper.wrapDegrees(180.0F), 0);
                break;
            case WEST:
                robot.setPos(Vec3.atCenterOf(pos.get().west()));
                robot.lerpHeadTo(MathHelper.wrapDegrees(90.0F), 0);
                break;
            case EAST:
                robot.setPos(Vec3.atCenterOf(pos.get().east()));
                robot.lerpHeadTo(MathHelper.wrapDegrees(-90.0F), 0);
                break;
            case SOUTH:
                robot.setPos(Vec3.atCenterOf(pos.get().south()));
                robot.lerpHeadTo(0, 0);
                break;
            default:
                robot.setPos(Vec3.atCenterOf(pos.get()));
                robot.lerpHeadTo(0, 0);
                break;
        }

        //Prepare Spawning
        robot.setUUID(UUID.randomUUID()); //Give the robot a new UUID to prevent a bug when str-middleclicking the block in creative mode (those entities would have the same UUID and consequently wont spawn)
        robot.level = level;

        //Spawn
        level.addFreshEntity(robot);
        clearRobot();

        //robot.setOwner(owner); TODO
        return robot;
    }

    public RobotEntity getRobot() {
        return storedRobot;
    }
}
