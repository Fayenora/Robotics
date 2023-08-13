package com.ignis.igrobotics.integration.cc;

import com.ignis.igrobotics.definitions.ModMachines;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RedstoneIntegrator extends BlockEntity {

    private ServerComputer computer;

    public RedstoneIntegrator(BlockPos p_155229_, BlockState p_155230_) {
        super(ModMachines.REDSTONE_INTEGRATOR.get(), p_155229_, p_155230_);
    }

    public int getSignalStrengthForSide(Direction dir) {
        return computer != null ? computer.getRedstoneOutput(ComputerizedBehavior.toSide(dir)) : 0;
    }

    public boolean isValid() {
        return computer != null && Vec3.upFromBottomCenterOf(computer.getPosition(), 1).closerThan(getBlockPos().getCenter(), 0.6);
    }

    public void setComputer(ServerComputer computer) {
        this.computer = computer;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return AABB.ofSize(getBlockPos().getCenter(), 0, 0, 0);
    }

    @Override
    public void onChunkUnloaded() {
        level.removeBlock(getBlockPos(), false);
        super.onChunkUnloaded();
    }
}
