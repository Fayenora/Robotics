package com.ignis.igrobotics.integration.cc;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.commands.ICommandable;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.integration.cc.apis.CommandAPI;
import com.ignis.igrobotics.integration.cc.apis.InventoryAPI;
import com.ignis.igrobotics.integration.cc.apis.RobotAPI;
import com.ignis.igrobotics.integration.cc.apis.SensorAPI;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EntityComputer extends ServerComputer {

    private final LivingEntity entity;

    public EntityComputer(ServerLevel level, LivingEntity entity, int computerID, @Nullable String label, ComputerFamily family, int terminalWidth, int terminalHeight) {
        super(level, entity.blockPosition(), computerID, label, family, terminalWidth, terminalHeight);
        this.entity = entity;
        //Hook up all the apis
        if(entity instanceof Mob mob) {
            addAPI(new SensorAPI(getAPIEnvironment(), mob));
        }
        Optional<IRobot> robot = entity.getCapability(ModCapabilities.ROBOT).resolve();
        Optional<IItemHandler> items = entity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        Optional<IEnergyStorage> energy = entity.getCapability(ForgeCapabilities.ENERGY).resolve();
        Optional<ICommandable> commands = entity.getCapability(ModCapabilities.COMMANDS).resolve();
        if(robot.isPresent() && energy.isPresent()) {
            addAPI(new RobotAPI(getAPIEnvironment(), robot.get(), energy.get()));
        }
        if(commands.isPresent()) {
            addAPI(new CommandAPI(getAPIEnvironment(), commands.get()));
        }
        if(items.isPresent() && items.get() instanceof IItemHandlerModifiable handler) {
            addAPI(new InventoryAPI(getAPIEnvironment(), handler));
        }
    }

    @Override
    public BlockPos getPosition() {
        return entity.blockPosition();
    }
}
