package com.io.norabotics.integration.cc;

import com.io.norabotics.Reference;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.util.IDAssigner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class ComputerCapability implements IComputerized, INBTSerializable<CompoundTag> {

    private final LivingEntity entity;
    private EntityComputer computer;

    int computerID = -1;
    UUID instanceID = Reference.DEFAULT_UUID;

    public ComputerCapability(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean hasComputer() {
        return !instanceID.equals(Reference.DEFAULT_UUID);
    }

    @Override
    public EntityComputer getComputer() {
        if(computer == null) {
            computer = createEntityComputer();
        }
        return computer;
    }

    private EntityComputer createComputer(int computerId) {
        return new EntityComputer(
                (ServerLevel) entity.level(),
                entity, computerId,
                entity.getName().getString(),
                ComputerFamily.ADVANCED,
                39,
                13);
    }

    private EntityComputer createEntityComputer() {
        var server = entity.level().getServer();
        if (server == null) throw new IllegalStateException("Cannot access server computer on the client.");

        var computer = ServerContext.get(server).registry().get(instanceID);
        if(computer == null) {
            if(computerID < 0) {
                computerID = ComputerCraftAPI.createUniqueNumberedSaveDir(server, IDAssigner.COMPUTER);
            }

            computer = createComputer(computerID);
            instanceID = computer.register();
        }
        if(!(computer instanceof EntityComputer)) throw new IllegalStateException(".");

        return (EntityComputer) computer;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putUUID("instanceID", instanceID);
        compound.putInt("computerID", computerID);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        instanceID = compound.getUUID("instanceID");
        computerID = compound.getInt("computerID");
    }
}
