package com.ignis.igrobotics.common.content.blockentity;

import com.ignis.igrobotics.common.capabilities.impl.EnergyStorage;
import com.ignis.igrobotics.common.content.blocks.ChargerBlock;
import com.ignis.igrobotics.common.helpers.EntityFinder;
import com.ignis.igrobotics.definitions.ModMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChargerBlockEntity extends BlockEntity {

    protected EnergyStorage storage;

    public ChargerBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(ModMachines.CHARGER.get(), p_155229_, p_155230_);
        storage = new EnergyStorage(1000000, 2000);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChargerBlockEntity charger) {
        charger.getCapability(ForgeCapabilities.ENERGY).ifPresent(chargerEnergy -> {
            List<LivingEntity> entitiesToCharge = level.getEntities(EntityFinder.LIVING, new AABB(pos), ent -> ent.getCapability(ForgeCapabilities.ENERGY).isPresent());
            if(state.getValue(ChargerBlock.ACTIVE) != entitiesToCharge.size() > 0) {
                BlockState newState = state.setValue(ChargerBlock.ACTIVE, entitiesToCharge.size() > 0);
                level.setBlock(pos, newState, 3);
                setChanged(level, pos, newState);
            }

            for(Entity entity : entitiesToCharge) {
                entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energyStorage -> {
                    int extractedEnergy = chargerEnergy.extractEnergy(Integer.MAX_VALUE, false);
                    energyStorage.receiveEnergy(extractedEnergy, false);
                });
            }
        });

    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("energy", storage.serializeNBT());
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        storage.deserializeNBT(compound.getCompound("energy"));
    }

    LazyOptional<? extends IEnergyStorage> energy_cap = LazyOptional.of(() -> storage);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(remove) super.getCapability(cap, side);
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
        energy_cap = LazyOptional.of(() -> storage);
    }
}
