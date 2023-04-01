package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.definitions.ModMachines;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.Machine;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageBlockEntity extends BlockEntity {

    public static final int MACHINE_TO_ROBOT_ENERGY_TRANSFER = 1000;
    private static final Machine MACHINE = ModMachines.ROBOT_STORAGE;

    private final RobotLevelStorage storedRobot;
    private final EnergyStorage energy;

    /** Do not load this nbt during normal world loading, but just when entering the level */
    private CompoundTag entityNBT;

    public StorageBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(MACHINE.getBlockEntityType(), pPos, pBlockState);
        storedRobot = new RobotLevelStorage(level, null, this::getBlockPos);
        energy = new EnergyStorage(MACHINE.getEnergyCapacity(), MACHINE.getEnergyTransfer());
        energy_cap = LazyOptional.of(() -> energy);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StorageBlockEntity storage) {
        if(!storage.containsRobot()) return;
        //Transfer energy from the block to the robot
        storage.storedRobot.getRobot().getCapability(ForgeCapabilities.ENERGY).ifPresent((robotEnergy) -> {
            int to_transfer = Math.min(robotEnergy.receiveEnergy(MACHINE_TO_ROBOT_ENERGY_TRANSFER, true), storage.energy.extractEnergy(MACHINE_TO_ROBOT_ENERGY_TRANSFER, true));
            robotEnergy.receiveEnergy(to_transfer, false);
            storage.energy.extractEnergy(to_transfer, false);
        });
    }

    public boolean containsRobot() {
        return storedRobot.containsRobot();
    }

    public void clearRobot() {
        storedRobot.clearRobot();
    }

    public void enterRobot(LivingEntity robot) {
        storedRobot.enterStorage(robot);
    }

    public LivingEntity exitStorage() {
        return storedRobot.exitStorage();
    }

    /////////////////////
    // Capabilities
    /////////////////////

    LazyOptional<? extends IEnergyStorage> energy_cap;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) return energy_cap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy_cap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energy_cap = LazyOptional.of(() -> energy);
    }

    /////////////////////
    // Saving & Syncing
    /////////////////////

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        deserializeEntity(entityNBT);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.put("energy", energy.serializeNBT());
        nbt.put("entity", storedRobot.getRobot().serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        energy.deserializeNBT(nbt.getCompound("energy"));
        entityNBT = nbt.getCompound("entity");
        super.deserializeNBT(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        nbt.put("energy", energy.serializeNBT());
        nbt.put("entity", storedRobot.getRobot().serializeNBT());
        return serializeNBT();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        energy.deserializeNBT(tag.getCompound("energy"));
        deserializeEntity(tag.getCompound("entity"));
    }

    public void deserializeEntity(CompoundTag nbt) {
        if(nbt == null) return;
        RobotEntity robot = new RobotEntity(level);
        robot.deserializeNBT(nbt);
        storedRobot.setRobot(robot);
    }

}
