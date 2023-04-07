package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.definitions.ModMachines;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.Machine;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

    private RobotLevelStorage storedRobot;
    private final EnergyStorage energy;

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
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        setChanged();
    }

    public LivingEntity exitStorage() {
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        setChanged();
        return storedRobot.exitStorage();
    }

    public LivingEntity getEntity() {
        return storedRobot.getRobot();
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
        storedRobot.setLevel(level);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("energy", energy.serializeNBT());
        nbt.put("entity", storedRobot.serializeNBT());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        energy.deserializeNBT(nbt.getCompound("energy"));
        storedRobot.deserializeNBT(nbt.getCompound("entity"));
        super.load(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        nbt.put("energy", energy.serializeNBT());
        nbt.put("entity", storedRobot.serializeNBT());
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
