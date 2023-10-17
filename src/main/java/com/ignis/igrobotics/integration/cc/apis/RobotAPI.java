package com.ignis.igrobotics.integration.cc.apis;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.robot.SelectionType;
import com.ignis.igrobotics.core.util.StringUtil;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.apis.IAPIEnvironment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.server.ServerLifecycleHooks;

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
    public final boolean hasAccess(String player, String permission) throws LuaException {
        EnumPermission p;
        UUID uuid;
        try {
            p = EnumPermission.valueOf(permission.trim().toUpperCase());
        } catch(IllegalArgumentException e) {
            throw new LuaException("\"" + permission + "\" is not a valid permission type. Viable types are " + StringUtil.enumToString(EnumPermission.values()));
        }
        try {
            uuid = UUID.fromString(player);
        } catch(IllegalArgumentException e) {
            List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayersWithAddress(player);
            if(players.isEmpty()) throw new LuaException("\"" + player + "\" refers to neither a UUID nor a player");
            if(players.size() > 2) throw new LuaException("\"" + player + "\" is ambiguous and might refer to multiple players");
            uuid = players.get(0).getUUID();
        }
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
    public final void setPickupState(int pickupState) throws LuaException {
        if(pickupState < 1 || pickupState > 3) {
            throw new LuaException("The only valid pickup states are: 1 - don't pickup anything, 2 - pick up anything thrown at the robot, 3 - actively seek out items");
        }
        robot.setPickUpState(pickupState - 1);
    }

    @LuaFunction
    public final void setChunkLoadingState(int chunkLoadingState) throws LuaException {
        if(chunkLoadingState < 1 || chunkLoadingState > 3) {
            throw new LuaException("The only valid chunkloading states are: 1 - don't load, 2 - load the chunk the robot is in, 3 - also load surrounding chunks");
        }
        robot.setChunkLoading(chunkLoadingState - 1);
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
