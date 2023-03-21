package com.ignis.igrobotics.core.capabilities.robot;

import com.ignis.igrobotics.core.INBTSerializer;
import com.ignis.igrobotics.core.RobotModule;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Main Capability for identifying a robot
 */
// No pun intended
public interface IRobot extends INBTSerializer {

    boolean isActive();

    void setActivation(boolean active);

    boolean hasModule(RobotModule module);

    NonNullList<ItemStack> getModules();

    void setModules(List<ItemStack> items);

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
