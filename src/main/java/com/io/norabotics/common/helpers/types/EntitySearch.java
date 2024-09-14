package com.io.norabotics.common.helpers.types;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.helpers.EntityFinder;
import com.io.norabotics.common.helpers.util.NBTUtil;
import com.io.norabotics.network.messages.IBufferSerializable;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * A serializable search for an entity by specific means (by UUID, name, etc.) for a specifiable level.
 * For instant search across all levels without constrains see {@link EntityFinder}
 */
public class EntitySearch implements Predicate<Entity>, IBufferSerializable, INBTSerializable<CompoundTag> {

    //Which criteria to use
    //Note that UUID and ID are special in the regard that if an entity matches along one of them, it matches the entire search
    private EnumSet<SearchFlags> flags = EnumSet.noneOf(SearchFlags.class);

    @Nullable
    private UUID uuid;
    @Nullable
    private String name;
    @Nullable
    private EntityType<?> type;
    private int entityId;
    private int range;

    private Entity cache;
    private final Collection<SearchListener> listeners = new HashSet<>();

    private EntitySearch() {}

    public EntitySearch(@NotNull UUID uuid) {
        setUUID(uuid);
    }

    public EntitySearch(@NotNull String name) {
        setName(name);
    }

    public EntitySearch(@NotNull EntityType<?> type) {
        setType(type);
    }

    public EntitySearch(int entityId) {
        setEntityId(entityId);
    }

    /**
     * Commence the entity search across all levels
     * @param preferredLevel used to determine the result in case multiple entities come into question
     * @param origin used to determine the result in case multiple entities come into question
     * @return an entity matching the search, if one was found
     */
    @Nullable
    public Entity commence(ServerLevel preferredLevel, Vec3 origin) {
        if(cache != null && cache.isAlive()) return cache;
        // Commence a search across the preferred level
        Entity result = commenceForLevel(preferredLevel, origin);
        if(result != null) return result;
        // Commence across all other levels
        for(ServerLevel level : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            if(level.equals(preferredLevel)) continue;
            double coordinateScale = 1 / level.dimensionType().coordinateScale();
            Vec3 searchStart = new Vec3(origin.x, 0, origin.y);
            searchStart = searchStart.scale(coordinateScale).add(0, origin.y, 0);
            result = commenceForLevel(level, searchStart);
            if(result != null) return result;
        }
        // Commence a player search
        result = commenceForPlayer(preferredLevel);
        return result;
    }

    /**
     * Commence the entity search in the specified level
     * @param level the dimension to search in
     * @param origin used to determine the result in case multiple entities come into question
     * @return an entity matching the search, if one was found
     */
    @Nullable
    private Entity commenceForLevel(ServerLevel level, Vec3 origin) {
        if(flags.contains(SearchFlags.UUID) && uuid != null) return level.getEntity(uuid);
        if(flags.contains(SearchFlags.ID)) return level.getEntity(entityId);
        if(flags.containsAll(List.of(SearchFlags.TYPE, SearchFlags.RANGE))) return EntityFinder.getClosestTo(level, origin, type, range, this);
        if(flags.contains(SearchFlags.TYPE)) return EntityFinder.getClosestTo(level, origin, type, this);
        if(flags.contains(SearchFlags.RANGE)) return EntityFinder.getClosestTo(level, origin, range, this);
        if(!flags.isEmpty()) return EntityFinder.getClosestTo(level, origin, this);
        return null;
    }

    @Nullable
    private Entity commenceForPlayer(ServerLevel level) {
        GameProfileCache cache = level.getServer().getProfileCache();
        if(cache == null || type != EntityType.PLAYER) return null;
        Optional<GameProfile> profile = Optional.empty();
        if(flags.contains(SearchFlags.UUID) && uuid != null) profile = cache.get(uuid);
        if(flags.contains(SearchFlags.NAME) && name != null) profile = cache.get(name);
        // No way to look the player up if he left in case of entity ids
        // Just return a fake player if we found something
        if(profile.isPresent() && profile.get().isComplete()) {
            return Robotics.proxy.createFakePlayer(level, profile.get()).get();
        }
        return null;
    }

    /**
     * Note that we cannot test the range condition
     * @param livingEntity the input argument
     * @return Whether the entity fulfills all criteria of this search
     */
    @Override
    public boolean test(Entity livingEntity) {
        if(flags.contains(SearchFlags.UUID)) return livingEntity.getUUID().equals(uuid);
        if(flags.contains(SearchFlags.ID)) return livingEntity.getId() == entityId;
        if(flags.contains(SearchFlags.NAME) && !livingEntity.getName().getString().equals(name)) {
            return false;
        }
        if(flags.contains(SearchFlags.TYPE) && livingEntity.getType() != type) {
            return false;
        }
        return true;
    }

    public void testAndNotify(Entity entity) {
        if(!test(entity)) return;
        for(SearchListener listener : listeners) {
            listener.onSearchFoundNewResult(entity);
        }
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        flags = buf.readEnumSet(SearchFlags.class);
        if(flags.contains(SearchFlags.UUID)) uuid = buf.readUUID();
        if(flags.contains(SearchFlags.NAME)) name = buf.readUtf();
        if(flags.contains(SearchFlags.ID)) entityId = buf.readInt();
        if(flags.contains(SearchFlags.TYPE)) type = buf.readRegistryId();
        range = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnumSet(flags, SearchFlags.class);
        if(uuid != null) buf.writeUUID(uuid);
        if(name != null) buf.writeUtf(name);
        if(entityId != 0) buf.writeInt(entityId);
        if(type != null) buf.writeRegistryId(ForgeRegistries.ENTITY_TYPES, type);
        buf.writeInt(range);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if(uuid != null) nbt.putUUID("uuid", uuid);
        if(name != null) nbt.putString("name", name);
        if(entityId != 0) nbt.putInt("entityId", entityId);
        if(type != null) nbt.put("kind", NBTUtil.serializeEntry(ForgeRegistries.ENTITY_TYPES, type));
        if(range != 0) nbt.putInt("range", range);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if(nbt.contains("uuid")) setUUID(nbt.getUUID("uuid"));
        if(nbt.contains("name")) setName(nbt.getString("name"));
        if(nbt.contains("entityId")) setEntityId(nbt.getInt("entityId"));
        if(nbt.contains("kind")) setType(NBTUtil.deserializeEntry(ForgeRegistries.ENTITY_TYPES, nbt.get("kind")));
        if(nbt.contains("range")) setRange(nbt.getInt("range"));
    }

    public static EntitySearch from(FriendlyByteBuf buf) {
        EntitySearch search = new EntitySearch();
        search.read(buf);
        return search;
    }

    public static EntitySearch of(CompoundTag nbt) {
        EntitySearch search = new EntitySearch();
        search.deserializeNBT(nbt);
        return search;
    }

    public void setUUID(@NotNull UUID uuid) {
        this.uuid = uuid;
        flags.add(SearchFlags.UUID);
    }

    public void setName(@NotNull String name) {
        this.name = name;
        flags.add(SearchFlags.NAME);
    }

    public void setRange(int range) {
        this.range = range;
        if(range > 0) {
            flags.add(SearchFlags.RANGE);
        }
    }

    public void setEntityId(int entityId) {
        if(entityId == 0) return;
        this.entityId = entityId;
        flags.add(SearchFlags.ID);
    }

    public void setType(EntityType<?> type) {
        this.type = type;
        flags.add(SearchFlags.TYPE);
    }

    public static EntitySearch searchForNone() {
        return new EntitySearch();
    }

    public boolean isSearchForNone() {
        return flags.isEmpty();
    }

    public boolean searchesFor(SearchFlags flag) {
        return flags.contains(flag);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<EntityType<?>> getType() {
        return Optional.ofNullable(type);
    }

    public int getRange() {
        return range;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntitySearch that)) return false;
        return entityId == that.entityId && range == that.range && Objects.equals(uuid, that.uuid) && Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, type, entityId, range);
    }

    public enum SearchFlags {
        NAME,
        UUID,
        ID,
        RANGE,
        TYPE
    }
}
