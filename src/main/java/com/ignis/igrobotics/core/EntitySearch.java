package com.ignis.igrobotics.core;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.network.messages.IBufferSerializable;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A search for an entity by specific means (by UUID, name, etc.).
 */
public class EntitySearch implements Predicate<Entity>, IBufferSerializable, INBTSerializable<CompoundTag> {

    //Which criteria to use
    private byte flags;

    @Nullable
    private UUID uuid;
    @Nullable
    private String name;
    private int entityId;

    private Entity cache;
    private final Collection<SearchListener> listeners = new HashSet<>();

    // For future use?
    private boolean searchAllLevels;
    private EntityTypeTest<?, ?> typeTest;

    public EntitySearch() {}

    public EntitySearch(@NotNull UUID uuid) {
        setUUID(uuid);
    }

    public EntitySearch(@NotNull String name) {
        setName(name);
    }

    public EntitySearch(int entityId) {
        setEntityId(entityId);
    }

    /**
     * Commence the entity search in the specified level
     * @param level the dimension to search in
     * @param origin used to determine the result in case multiple entities come into question
     * @return an entity matching the search, if one was found
     */
    @Nullable
    public Entity commence(ServerLevel level, BlockPos origin) {
        if(cache != null) {
            return cache;
        }
        if((flags & 1) == 1 && uuid != null) return level.getEntity(uuid);
        if((flags & 2) == 2 && name != null) {
            //Greedily search for the closest entity
            //FIXME: If the client requires an EntityLiving, but a not living entity matching the search is closer to the player, the search will yield the not living entity, causing the client to believe no entity matches the search
            double min_distance = Double.MAX_VALUE;
            Entity result = null;
            for(Entity ent : level.getAllEntities()) {
                if(ent.getName().getString().equals(name)) {
                    double distance = ent.distanceToSqr(Vec3.atLowerCornerOf(origin));
                    if(distance < min_distance) {
                        result = ent;
                        min_distance = distance;
                    }
                }
            }
            return result;
        }
        if((flags & 4) == 4) return level.getEntity(entityId);

        //If nothing was found up to now, the search might reference a player that left. Look in the profile cache
        GameProfileCache cache = level.getServer().getProfileCache();
        Optional<GameProfile> profile = Optional.empty();
        if((flags & 1) == 1 && uuid != null) profile = cache.get(uuid);
        if((flags & 2) == 2 && name != null) profile = cache.get(name);
        // No way to look the player up if he left in case of entity ids
        // Just return a fake player if we found something
        if(profile.isPresent() && profile.get().isComplete()) {
            return Robotics.proxy.createFakePlayer(level, profile.get()).get();
        }
        return null;
    }

    @Override
    public boolean test(Entity livingEntity) {
        if((flags & 1) == 1 && !livingEntity.getUUID().equals(uuid)) {
            return false;
        }
        if((flags & 2) == 2 && !livingEntity.getName().getString().equals(name)) {
            return false;
        }
        if((flags & 4) == 4 && livingEntity.getId() != entityId) {
            return false;
        }
        // The entity matches our search! Notify any listeners and return true
        for(SearchListener listener : listeners) {
            listener.onSearchFoundNewResult(livingEntity);
        }
        return true;
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        flags = buf.readByte();
        if((flags & 1) == 1) uuid = buf.readUUID();
        if((flags & 2) == 2) name = buf.readUtf();
        if((flags & 4) == 4) entityId = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(flags);
        if(uuid != null) buf.writeUUID(uuid);
        if(name != null) buf.writeUtf(name);
        if(entityId != 0) buf.writeInt(entityId);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if(uuid != null) nbt.putUUID("uuid", uuid);
        if(name != null) nbt.putString("name", name);
        if(entityId != 0) nbt.putInt("entityId", entityId);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if(nbt.contains("uuid")) setUUID(nbt.getUUID("uuid"));
        if(nbt.contains("name")) setName(nbt.getString("name"));
        if(nbt.contains("entityId")) setEntityId(nbt.getInt("entityId"));
    }

    public static EntitySearch of(CompoundTag nbt) {
        EntitySearch search = new EntitySearch();
        search.deserializeNBT(nbt);
        return search;
    }

    public void setUUID(@NotNull UUID uuid) {
        this.uuid = uuid;
        flags |= 1;
    }

    public void setName(@NotNull String name) {
        this.name = name;
        flags |= 2;
    }

    public void setEntityId(int entityId) {
        if(entityId == 0) return;
        this.entityId = entityId;
        flags |= 4;
    }

    public void addListener(SearchListener listener) {
        listeners.add(listener);
    }

    public void setCache(Entity entity) {
        this.cache = entity;
    }

    public interface SearchListener {
        void onSearchFoundNewResult(Entity newResult);
    }

    @Override
    public String toString() {
        if(name != null) return name;
        if(uuid != null) return uuid.toString();
        return super.toString();
    }
}
