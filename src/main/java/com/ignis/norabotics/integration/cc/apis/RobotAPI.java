package com.ignis.norabotics.integration.cc.apis;

import com.ignis.norabotics.common.access.EnumPermission;
import com.ignis.norabotics.common.capabilities.IRobot;
import com.ignis.norabotics.common.helpers.types.SelectionType;
import com.ignis.norabotics.common.helpers.util.StringUtil;
import com.ignis.norabotics.common.robot.EnumModuleSlot;
import com.ignis.norabotics.common.robot.RobotModule;
import com.ignis.norabotics.definitions.ModModules;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.apis.IAPIEnvironment;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class RobotAPI implements ILuaAPI {

    private final IAPIEnvironment environment;
    private final LivingEntity entity;
    private final IRobot robot;
    private final IEnergyStorage energyStorage;

    public RobotAPI(IAPIEnvironment environment, LivingEntity entity, IRobot robot, IEnergyStorage energyStorage) {
        this.environment = environment;
        this.entity = entity;
        this.robot = robot;
        this.energyStorage = energyStorage;
    }

    @LuaFunction
    public final void deactivate() {
        robot.setActivation(false);
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
    public final float getHealth() {
        return entity.getHealth();
    }

    /**
     * A list of objects representing the MobEffects the entity is currently affected by
     * @return the list
     */
    @LuaFunction
    public final List<LuaMobEffect> getEffects() {
        return entity.getActiveEffects().stream().map(LuaMobEffect::new).toList();
    }

    @LuaFunction
    public final double getAttribute(String attributeName) throws LuaException {
        try {
            ResourceLocation loc = ResourceLocation.tryParse(attributeName);
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(loc);
            if(attribute == null) {
                throw new LuaException(attributeName + " is not a valid attribute. Call " + getNames()[0] + ".getValidAttributes() for a list of valid attributes");
            }
            return entity.getAttributeValue(attribute);
        } catch(ResourceLocationException e) {
            throw new LuaException(e.getMessage());
        }
    }

    @LuaFunction
    public final List<String> getValidAttributes() {
        Stream<Attribute> attributes = ForgeRegistries.ATTRIBUTES.getValues().stream();
        return attributes.map(Attribute::getDescriptionId).toList();
    }

    @LuaFunction
    public final List<String> getModules(Optional<String> slotType) throws LuaException {
        if(slotType.isPresent()) {
            try {
                EnumModuleSlot slot = EnumModuleSlot.valueOf(slotType.get().toUpperCase());
                return robot.getModules(slot).stream().map(SelectionType.ITEM.stringifier()).toList();
            } catch(IllegalArgumentException exc) {
                throw new LuaException("\"" + slotType.get() + "\" is not a valid module slot type. Viable arguments are: " + StringUtil.enumToString(EnumModuleSlot.values()));
            }
        }
        return getModules().stream().map(SelectionType.ITEM.stringifier()).toList();
    }

    @LuaFunction
    public final boolean activate(int slot, Optional<String> slotType) throws LuaException {
        ItemStack moduleItem;
        if(slotType.isPresent()) {
            try {
                EnumModuleSlot moduleSlot = EnumModuleSlot.valueOf(slotType.get().toUpperCase());
                moduleItem = robot.getModules(moduleSlot).get(slot - 1);
            } catch(IllegalArgumentException exc) {
                throw new LuaException("\"" + slotType.get() + "\" is not a valid module slot type. Viable arguments are: " + StringUtil.enumToString(EnumModuleSlot.values()));
            }
        } else {
            moduleItem = getModules().get(slot - 1);
        }
        RobotModule module = ModModules.get(moduleItem);
        if(module == null) return false;
        return module.activate(entity);
    }

    private List<ItemStack> getModules() {
        List<ItemStack> concatenatedModules = new ArrayList<>();
        for(EnumModuleSlot slot : EnumModuleSlot.values()) {
            concatenatedModules.addAll(robot.getModules(slot));
        }
        return concatenatedModules;
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

    /*
        A list of potential methods to expose for the future
        getRobotsOfSameCommandGroup()
            Respect access rights!
        entity.getLastAttacker();
        entity.getLastHurtByMob();
        entity.getLastDamageSource();
        entity.getLastHurtMob();
        entity.getBoundingBox();
        entity.getMobType();
        entity.getAirSupply();
        entity.getMaxAirSupply();
     */
}
