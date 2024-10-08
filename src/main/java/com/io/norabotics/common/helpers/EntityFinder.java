package com.io.norabotics.common.helpers;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.CommonSetup;
import com.io.norabotics.common.capabilities.IRobot;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.helpers.types.EntitySearch;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A set of common functions for finding entities, usually across all levels.
 * For a serializable entity search see {@link EntitySearch}
 */
@MethodsReturnNonnullByDefault
public class EntityFinder {

    /** Search radius to use on client side */
    public static final int SEARCH_RADIUS = 100;

    /**
     * Search for the closest entity in a specified level
     * @param level the level to search
     * @param origin which point the entity should be closest to
     * @param condition any condition the entity must fulfill
     * @return the entity fulfilling the condition which is closest to origin, if it exists
     */
    @Nullable
    public static Entity getClosestTo(Level level, Vec3 origin, Predicate<Entity> condition) {
        return getClosestTo(level, origin, condition, ServerLevel::getAllEntities);
    }

    /**
     * Search for the closest entity in a specified level
     * @param level the level to search
     * @param origin which point the entity should be closest to
     * @param type only search for entities of specified type
     * @param condition any additional conditions the entity must fulfill
     * @return the entity fulfilling the condition which is closest to origin, if it exists
     */
    @Nullable
    public static Entity getClosestTo(Level level, Vec3 origin, EntityType<?> type, Predicate<Entity> condition) {
        return getClosestTo(level, origin, condition, serverLevel ->
                serverLevel.getEntities(EntityTypeTest.forClass(CommonSetup.getClassOf(type)), condition));
    }

    /**
     * Search for the closest entity in a specified level
     * @param level the level to search
     * @param origin which point the entity should be closest to
     * @param range a maximum range
     * @param condition any additional conditions the entity must fulfill
     * @return the entity fulfilling the condition which is closest to origin, if it exists
     */
    @Nullable
    public static Entity getClosestTo(Level level, Vec3 origin, int range, Predicate<Entity> condition) {
        return getClosestTo(level, origin, condition, serverLevel ->
                serverLevel.getEntities((Entity) null, AABB.ofSize(origin, range, range, range), condition));
    }

    /**
     * Search for the closest entity in a specified level
     * @param level the level to search
     * @param origin which point the entity should be closest to
     * @param range a maximum range
     * @param type only search for entities of specified type
     * @param condition any additional conditions the entity must fulfill
     * @return the entity fulfilling the condition which is closest to origin, if it exists
     */
    @Nullable
    public static Entity getClosestTo(Level level, Vec3 origin, EntityType<?> type, int range, Predicate<Entity> condition) {
        return getClosestTo(level, origin, condition, serverLevel ->
                serverLevel.getEntities(EntityTypeTest.forClass(CommonSetup.getClassOf(type)), AABB.ofSize(origin, range, range, range), condition));
    }

    @Nullable
    private static Entity getClosestTo(Level level, Vec3 origin, Predicate<Entity> condition, Function<ServerLevel, Iterable<? extends Entity>> serverSelection) {
        Iterable<? extends Entity> toSearch;
        if(level instanceof ServerLevel serverLevel) {
            toSearch = serverSelection.apply(serverLevel);
        } else {
            Player player = Robotics.proxy.getPlayer();
            if(player == null) return null;
            toSearch = level.getEntities(player, player.getBoundingBox().deflate(SEARCH_RADIUS));
        }
        double min_distance = Double.MAX_VALUE;
        Entity result = null;
        for(Entity ent : toSearch) {
            double distance = ent.distanceToSqr(origin);
            if(ent.isAlive() && condition.test(ent) && distance < min_distance) {
                result = ent;
                min_distance = distance;
            }
        }
        return result;
    }

    /**
     * Find an entity with specified UUID across all dimensions on server-side. On client-side only search entities in the vicinity
     * @param level is only used for determining the side
     * @param uuid UUID
     * @return Entity with specified UUID, if it is loaded anywhere on the server
     */
    @Nullable
    public static Entity getEntity(Level level, UUID uuid) {
        if(level.isClientSide()) {
            Player player = Robotics.proxy.getPlayer();
            if(player == null) return null;
            for (Entity ent : level.getEntities(player, player.getBoundingBox().deflate(SEARCH_RADIUS))) {
                if (ent.getUUID().equals(uuid)) {
                    return ent;
                }
            }
            return null;
        }
        return getEntity(uuid);
    }

    /**
     * Find all robotic entities which fulfill the condition on the server-side. On client-side only search entities in the vicinity
     * @param level is only used for determining the side
     * @param condition which should apply to all robots
     * @return all loaded entities matching the condition
     */
    public static Collection<LivingEntity> getRobotics(Level level, Predicate<IRobot> condition) {
        if(level.isClientSide()) {
            Player player = Robotics.proxy.getPlayer();
            if(player == null) return Collections.emptySet();
            Collection<LivingEntity> robots = new HashSet<>();
            for(Entity ent : level.getEntities(player, player.getBoundingBox().deflate(SEARCH_RADIUS))) {
                if(ent instanceof LivingEntity living && testCondition(living, condition)) {
                    robots.add(living);
                }
            }
            return robots;
        }
        return getRobotics(condition);
    }

    @Nullable
    private static Entity getEntity(UUID uuid) {
        for(ServerLevel dimension : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            Entity entity = dimension.getEntity(uuid);
            if(entity != null) {
                return entity;
            }
        }
        return null;
    }

    private static Collection<LivingEntity> getRobotics(Predicate<IRobot> condition) {
        Collection<LivingEntity> robots = new HashSet<>();
        for(ServerLevel dimension : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            robots.addAll(dimension.getEntities(LIVING, living -> testCondition(living, condition)));
        }
        return robots;
    }

    private static boolean testCondition(LivingEntity living, Predicate<IRobot> predicate) {
        Optional<IRobot> robot = living.getCapability(ModCapabilities.ROBOT).resolve();
        return robot.filter(predicate).isPresent();
    }

    public static final EntityTypeTest<Entity, LivingEntity> LIVING = new EntityTypeTest<>() {
        @Nullable
        @Override
        public LivingEntity tryCast(@NotNull Entity entity) {
            return entity instanceof LivingEntity ? (LivingEntity) entity : null;
        }

        public @NotNull Class<? extends LivingEntity> getBaseClass() {
            return LivingEntity.class;
        }
    };

}
