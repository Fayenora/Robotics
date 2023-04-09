package com.ignis.igrobotics.network.proxy;

import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Predicate;

public class ServerProxy implements IProxy {

    public static final EntityTypeTest<Entity, LivingEntity> LIVING = new EntityTypeTest<>() {
        @Nullable
        @Override
        public LivingEntity tryCast(Entity entity) {
            return LivingEntity.class.isInstance(entity) ? (LivingEntity) entity : null;
        }

        public Class<? extends LivingEntity> getBaseClass() {
            return LivingEntity.class;
        }
    };

    @Override
    public Level getLevel() {
        return ServerLifecycleHooks.getCurrentServer().overworld();
    }

    @Override
    public Entity getEntity(UUID uuid) {
        for(ServerLevel dimension : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            Entity entity = dimension.getEntity(uuid);
            if(entity != null) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public Collection<LivingEntity> getRobotics(Predicate<IRobot> condition) {
        Collection<LivingEntity> robots = new HashSet<>();
        for(ServerLevel dimension : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            robots.addAll(dimension.getEntities(LIVING, living ->
                    living.getCapability(ModCapabilities.ROBOT).isPresent() && condition.test(living.getCapability(ModCapabilities.ROBOT).resolve().get())));
        }
        return robots;
    }

    @Override
    public WeakReference<Player> createFakePlayer(Level level, GameProfile profile) {
        if(!(level instanceof ServerLevel serverLevel)) return new WeakReference<>(null);
        return new WeakReference<>(FakePlayerFactory.get(serverLevel, profile));
    }
}
