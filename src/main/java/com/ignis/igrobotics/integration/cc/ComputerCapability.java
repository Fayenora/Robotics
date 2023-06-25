package com.ignis.igrobotics.integration.cc;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.util.IDAssigner;
import net.minecraft.nbt.IntTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

public class ComputerCapability implements IComputerized, INBTSerializable<IntTag> {

    private final LivingEntity entity;
    private EntityComputer computer;

    int computerID = -1;
    int instanceID = -1;

    public ComputerCapability(LivingEntity entity) {
        this.entity = entity;
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
                (ServerLevel) entity.level,
                entity, computerId,
                entity.getName().getString(),
                ComputerFamily.ADVANCED,
                39,
                13);
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
    public IntTag serializeNBT() {
        return IntTag.valueOf(instanceID);
    }

    @Override
    public void deserializeNBT(IntTag nbt) {
        instanceID = nbt.getAsInt();
    }
}
