package com.ignis.igrobotics.integration.cc.apis;

import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.robot.SelectionType;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.apis.IAPIEnvironment;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;
import java.util.UUID;

public class RobotAPI implements ILuaAPI {

    private final IAPIEnvironment environment;
    private final IRobot robot;
    private final IEnergyStorage energyStorage;

    public RobotAPI(IAPIEnvironment environment, IRobot robot, IEnergyStorage energyStorage) {
        this.environment = environment;
        this.robot = robot;
        this.energyStorage = energyStorage;
    }

    @LuaFunction
    public final boolean isActive() {
        return robot.isActive();
    }

    /**
     *
     * @param player UUID of the player to check
     * @param permission type of permission to query. Viable options: <br>
     *                   - "view": whether the player can view information about this robot <br>
     *                   - "inventory": whether the player has access to this robots inventory <br>
     *                   - "configuration": whether the player can configure this robot <br>
     *                   - "commands": whether the player can access commands and the terminal of this robot <br>
     *                   - "ally": whether the player is considered an ally to this robot
     * @return whether the player has the given permission
     */
    @LuaFunction
    public final boolean hasAccess(String player, String permission) {
        EnumPermission p = EnumPermission.valueOf(permission.trim().toUpperCase());
        UUID uuid = UUID.fromString(player);
        return robot.hasAccess(uuid, p);
    }

    @LuaFunction
    public final int getEnergy() {
        return energyStorage.getEnergyStored();
    }

    @LuaFunction
    public final int getMaxEnergy() {
        return energyStorage.getMaxEnergyStored();
    }

    @LuaFunction
    public final List<String> getModules() {
        return robot.getModules().stream().map(SelectionType.ITEM.stringifier()).toList();
    }

    @LuaFunction
    public final int getPickupState() {
        return robot.getPickUpState();
    }

    @LuaFunction
    public final int isChunkLoadingActive() {
        return robot.getChunkLoadingState();
    }

    @LuaFunction
    public final boolean isMuffled() {
        return robot.isMuffled();
    }

    @LuaFunction
    public final int getCommandGroup() {
        return robot.getCommandGroup();
    }

    @LuaFunction
    public final void setPickupState(int pickupState) {
        robot.setPickUpState(pickupState);
    }

    @LuaFunction
    public final void setChunkLoadingState(int chunkLoadingState) {
        robot.setChunkLoading(chunkLoadingState);
    }

    @LuaFunction
    public final void setMuffled(boolean muffled) {
        robot.setMuffled(muffled);
    }

    /**
     * Get the owner of this robot
     * @return the UUID of the owning player
     */
    @LuaFunction
    public final String getOwner() {
        return robot.getOwner().toString();
    }

    @Override
    public String[] getNames() {
        return new String[] {"robot"};
    }
}
