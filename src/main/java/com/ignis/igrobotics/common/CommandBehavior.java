package com.ignis.igrobotics.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.robot.SelectionType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandBehavior {

    /**
     * List of Entity Searches (Predicates), usually from {@link RobotCommand RobotCommands}
     * Because commands might refer to entities that do not exist (yet), we need to keep a list of these references/searches.
     * In case a new entity gets added/an entity gets changed to match the search, we notify the associated commands.
     * <br>
     * NOTE: A EntitySearch is added to the level of the robot which tries to conduct this search.
     * This does not need to mean that the search only searches this level, it might search across all dimensions.
     */
    public static final Multimap<Level, EntitySearch> SEARCHES = HashMultimap.create();

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide) return;
        if(event.getEntity() instanceof ItemEntity) return; // Save some runtime here by ignoring items
        for(EntitySearch search : SEARCHES.values()) {
            search.test(event.getEntity()); // The Search will automatically notify any listeners that it found something
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if(event.getLevel().isClientSide) return;
        // The search is not relevant to the level anymore -> Remove it
        event.getEntity().getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> {
            for(RobotCommand command : commands.getCommands()) {
                for(Selection<?> selection : command.getSelectors()) {
                    if(selection.getType().equals(SelectionType.ENTITY_PREDICATE)) {
                        EntitySearch search = (EntitySearch) selection.get();
                        SEARCHES.remove(event.getLevel(), search);
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if(event.getLevel().isClientSide()) return;
        SEARCHES.removeAll(event.getLevel());
    }

}
