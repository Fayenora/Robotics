package com.ignis.igrobotics.core.capabilities.robot;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.core.access.AccessConfig;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.robot.RobotModule;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * Main Capability for identifying a robot
 */
// No pun intended
@AutoRegisterCapability
public interface IRobot extends INBTSerializable<CompoundTag> {

    boolean isActive();

    void setActivation(boolean active);

    boolean hasModule(RobotModule module);

    NonNullList<ItemStack> getModules();

    void setModules(List<ItemStack> items);

    void setMaxModules(int amount);

    boolean hasRenderLayer(int id);

    void setOwner(UUID newOwner);

    @NonNull
    UUID getOwner();

    void setAccess(AccessConfig access);

    AccessConfig getAccess();

    default boolean hasOwner() {
        return !getOwner().equals(Reference.DEFAULT_UUID);
    }

    default boolean hasAccess(Player player, EnumPermission permission) {
        if(getOwner().equals(Reference.DEFAULT_UUID)) return true;
        return player.getUUID().equals(getOwner()); //TODO
    }

    //////////////////////
    // Configuration Data
    //////////////////////

    /**
     * Set whether the robot should load chunks
     * @param state - possible values: 0 - no chunkloading, 1 - load current chunks, 2 - load adjacent chunks
     */
    void setChunkLoading(int state);

    /**
     * Whether the robot loads chunks
     * @return state - possible values: 0 - no chunkloading, 1 - loading current chunks, 2 - loading adjacent chunks
     * @implNote should always return 0 for players
     */
    int getChunkLoadingState();

    default boolean isChunkLoading() {
        return getChunkLoadingState() > 0;
    }

    void setCommandGroup(int group);

    int getCommandGroup();

    void setPickUpState(int state);

    int getPickUpState();

    void setMuffled(boolean muffled);

    boolean isMuffled();

    default void nextPickUpState() {
        setPickUpState((getPickUpState() + 1) % 3);
    }

    default void nextMuteState() {
        setMuffled(!isMuffled());
    }

    default void nextChunkLoadingState() {
        setChunkLoading((getChunkLoadingState() + 1) % 3);
    }
}
