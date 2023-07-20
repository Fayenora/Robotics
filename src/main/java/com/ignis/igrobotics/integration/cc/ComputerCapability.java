package com.ignis.igrobotics.integration.cc;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.util.IDAssigner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

public class ComputerCapability implements IComputerized, INBTSerializable<CompoundTag> {

    private final LivingEntity entity;
    private EntityComputer computer;

    int computerID = -1;
    int instanceID = -1;

    public ComputerCapability(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean hasComputer() {
        return computer != null;
    }

    @Override
    public EntityComputer getComputer() {
        if(computer == null) {
            computer = createEntityComputer();
        }
        return computer;
    }

    private EntityComputer createComputer(int computerId) {
        EntityComputer computer = new EntityComputer(
                (ServerLevel) entity.level,
                entity, computerId,
                entity.getName().getString(),
                ComputerFamily.ADVANCED,
                39,
                13);
        if(entity.getCapability(ModCapabilities.COMMANDS).isPresent()) {
            computer.addAPI(new CommandAPI(computer.getAPIEnvironment(), entity.getCapability(ModCapabilities.COMMANDS).resolve().get()));
        }
        return computer;
    }

    private EntityComputer createEntityComputer() {
        var server = entity.getLevel().getServer();
        if (server == null) throw new IllegalStateException("Cannot access server computer on the client.");

        var changed = false;

        var computer = ServerContext.get(server).registry().get(instanceID);
        if(computer == null) {
            if(computerID < 0) {
                computerID = ComputerCraftAPI.createUniqueNumberedSaveDir(server, IDAssigner.COMPUTER);
            }

            computer = createComputer(computerID);
            instanceID = computer.register();
            changed = true;
        }
        if(!(computer instanceof EntityComputer)) throw new IllegalStateException(".");

        computer.turnOn();
        // TODO if (changed) update peripherals
        return (EntityComputer) computer;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putInt("instanceID", instanceID);
        compound.putInt("computerID", computerID);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        instanceID = compound.getInt("instanceID");
        computerID = compound.getInt("computerID");
    }
}
