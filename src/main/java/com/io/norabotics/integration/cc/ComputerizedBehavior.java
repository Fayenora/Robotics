package com.io.norabotics.integration.cc;

import com.io.norabotics.common.capabilities.IRobot;
import com.io.norabotics.definitions.ModBlocks;
import dan200.computercraft.core.computer.ComputerSide;
import dan200.computercraft.impl.BundledRedstone;
import dan200.computercraft.impl.Peripherals;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.util.RedstoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ComputerizedBehavior {

    public static void onComputerTick(LivingEntity entity, IRobot robot, ServerComputer computer) {
        if(robot.isActive()) {
            if(!computer.isOn()) {
                computer.turnOn();
            }
        } else computer.shutdown();

        computer.keepAlive();

        //Update peripherals & redstone. Only do this when standing still
        if(entity.getDeltaMovement().length() > 0.1) return;
        ServerLevel level = computer.getLevel();
        BlockPos pos = computer.getPosition();

        for(Direction direction : Direction.values()) {
            ComputerSide side = toSide(direction);
            computer.setRedstoneInput(side, RedstoneUtil.getRedstoneInput(level, pos.relative(direction), direction));
            computer.setBundledRedstoneInput(side, BundledRedstone.getOutput(level, pos.relative(direction), direction));
            computer.setPeripheral(side, Peripherals.getPeripheral(level, pos, direction, () -> {}));
        }
        placeRedstoneIntegrator(level, pos, computer);
        placeRedstoneIntegrator(level, pos.above(), computer);
        if(computer.pollAndResetChanges() > 0) {
            level.updateNeighborsAt(pos, ModBlocks.REDSTONE_INTEGRATOR.get());
            level.updateNeighborsAt(pos.above(), ModBlocks.REDSTONE_INTEGRATOR.get());
        }
    }

    private static void placeRedstoneIntegrator(Level level, BlockPos pos, ServerComputer computer) {
        if(!level.getBlockState(pos).isAir() || level.getBlockState(pos).getBlock().equals(ModBlocks.REDSTONE_INTEGRATOR.get())) return;
        BlockState block = ModBlocks.REDSTONE_INTEGRATOR.get().defaultBlockState();
        level.setBlockAndUpdate(pos, block);
        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof RedstoneIntegrator integrator) {
            integrator.setComputer(computer);
        }
    }

    public static ComputerSide toSide(Direction dir) {
        return switch(dir) {
            case DOWN -> ComputerSide.BOTTOM;
            case UP -> ComputerSide.TOP;
            case NORTH -> ComputerSide.FRONT;
            case SOUTH -> ComputerSide.BACK;
            case WEST -> ComputerSide.LEFT;
            case EAST -> ComputerSide.RIGHT;
        };
    }

    public static Direction toDirection(ComputerSide side) {
        return switch(side) {
            case BOTTOM -> Direction.DOWN;
            case TOP -> Direction.UP;
            case FRONT -> Direction.NORTH;
            case BACK -> Direction.SOUTH;
            case LEFT -> Direction.WEST;
            case RIGHT -> Direction.EAST;
        };
    }
}
