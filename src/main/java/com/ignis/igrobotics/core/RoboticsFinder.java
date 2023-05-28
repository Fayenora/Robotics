package com.ignis.igrobotics.core;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A set of common functions for finding entities
 */
public class RoboticsFinder {

    /** Search radius to use on client side */
    public static final int SEARCH_RADIUS = 100;

    /**
     * Find an entity with specified UUID across all dimensions. Only call on server-side
     * @param level is only used for determining the side
     * @param uuid UUID
     * @return Entity with specified UUID, if it is loaded anywhere on the server
     */
    @Nullable
    public static Entity getEntity(Level level, UUID uuid) {
        if(level.isClientSide()) {
            Player player = Minecraft.getInstance().player;
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
     * Find all robotic entities which fulfill the condition. Only call on server-side
     * @param level is only used for determining the side
     * @param condition which should apply to all robots
     * @return all loaded entities matching the condition
     */
    public static Collection<LivingEntity> getRobotics(Level level, Predicate<IRobot> condition) {
        if(level.isClientSide()) {
            Player player = Minecraft.getInstance().player;
            if(player == null) return null;
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
