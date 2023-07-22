package com.ignis.igrobotics.integration.cc;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.commands.ICommandable;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.integration.cc.apis.CommandAPI;
import com.ignis.igrobotics.integration.cc.apis.RobotAPI;
import com.ignis.igrobotics.integration.cc.apis.SensorAPI;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.util.IDAssigner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Optional;

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
        if(entity instanceof Mob mob) {
            computer.addAPI(new SensorAPI(computer.getAPIEnvironment(), mob));
        }
        Optional<IRobot> robot = entity.getCapability(ModCapabilities.ROBOT).resolve();
        Optional<IEnergyStorage> energy = entity.getCapability(ForgeCapabilities.ENERGY).resolve();
        Optional<ICommandable> commands = entity.getCapability(ModCapabilities.COMMANDS).resolve();
        if(robot.isPresent() && energy.isPresent()) {
            computer.addAPI(new RobotAPI(computer.getAPIEnvironment(), robot.get(), energy.get()));
        }
        if(commands.isPresent()) {
            computer.addAPI(new CommandAPI(computer.getAPIEnvironment(), commands.get()));
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
