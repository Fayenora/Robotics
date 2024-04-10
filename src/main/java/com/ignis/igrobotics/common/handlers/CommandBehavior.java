package com.ignis.igrobotics.common.handlers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.ai.FollowGoal;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.robot.SelectionType;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.function.Function;

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
        Entity entity = event.getEntity();
        for(EntitySearch search : SEARCHES.values()) {
            search.test(entity); // The Search will automatically notify any listeners that it found something
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

    @SubscribeEvent
    public static void onEntitySwitchedDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        if(entity.level().isClientSide) return;
        if(entity instanceof ItemEntity) return; // Save some runtime here by ignoring items
        ServerLevel level = (ServerLevel) entity.level();
        ServerLevel otherDimension = level.getServer().getLevel(event.getDimension());
        if(otherDimension == null) return;
        AABB area = AABB.ofSize(entity.position(), 16, 16, 16);

        // Make followers follow through dimensions
        for(Entity otherEntity : level.getEntities(entity, area, (ent) -> true)) {
            if(!(otherEntity instanceof Mob mob)) continue;
            if(!mob.canChangeDimensions()) continue;
            if(mob.goalSelector.getRunningGoals().anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof FollowGoal followGoal && entity.equals(followGoal.following()))) {
                followThroughDimension(mob, entity, otherDimension);
            }
        }
    }

    private static void followThroughDimension(Entity toTransport, Entity leader, ServerLevel dimension) {
        Vec3 newPosOfLeader = findDimensionEntryPoint(leader, leader.level(), dimension);
        if(newPosOfLeader == null) return;
        toTransport.changeDimension(dimension, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity transportedEntity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                transportedEntity.level().getProfiler().popPush("reloading");
                Entity clone = transportedEntity.getType().create(dimension);
                if (clone == null) return null;
                clone.restoreFrom(transportedEntity);
                clone.setPortalCooldown();
                clone.moveTo(newPosOfLeader.x, newPosOfLeader.y, newPosOfLeader.z, transportedEntity.getYRot(), transportedEntity.getXRot());

                clone.setDeltaMovement(leader.getDeltaMovement());
                dimension.addDuringTeleport(clone);
                return clone;
            }
        });
    }

    //NOTE: Largely copied from Entity#findDimensionEntryPoint. Keep up to date with vanilla!

    /**
     * Calculate where to place an entity changing dimensions
     * @param entity the entity changing dimensions. It needs to be currently inside a portal block
     * @param from dimension the entity is currently in
     * @param to dimension the entity is travelling to
     * @return the position the entity should be placed
     */
    @Nullable
    protected static Vec3 findDimensionEntryPoint(Entity entity, Level from, ServerLevel to) {
        if (to.dimension() == Level.END) {
            return Vec3.atBottomCenterOf(ServerLevel.END_SPAWN_POINT);
        }
        if(from.dimension() == Level.END && to.dimension() == Level.OVERWORLD) {
            BlockPos blockpos = to.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, to.getSharedSpawnPos());
            return Vec3.atBottomCenterOf(blockpos);
        }
        if(from.dimension() != Level.NETHER && to.dimension() != Level.NETHER) {
            return null;
        }

        WorldBorder worldborder = to.getWorldBorder();
        double d0 = DimensionType.getTeleportationScale(from.dimensionType(), to.dimensionType());
        BlockPos portalEntrancePos = entity.blockPosition();
        BlockPos dimensionEntrancePos = worldborder.clampToBounds(entity.getX() * d0, entity.getY(), entity.getZ() * d0);
        return to.getPortalForcer().findPortalAround(dimensionEntrancePos, to.dimension() == Level.NETHER, worldborder).map((p_258249_) -> {
            BlockState blockstate = from.getBlockState(portalEntrancePos);
            Direction.Axis axis;
            Vec3 vec3;
            if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                BlockUtil.FoundRectangle portalShape = BlockUtil.getLargestRectangleAround(portalEntrancePos, axis, 21, Direction.Axis.Y, 21, (p_185959_) -> {
                    return from.getBlockState(p_185959_) == blockstate;
                });
                vec3 = PortalShape.getRelativePosition(portalShape, axis, entity.position(), entity.getDimensions(entity.getPose()));
            } else {
                axis = Direction.Axis.X;
                vec3 = new Vec3(0.5D, 0.0D, 0.0D);
            }

            return PortalShape.createPortalInfo(to, p_258249_, axis, vec3, entity, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot()).pos;
        }).orElse(null);
    }

}
