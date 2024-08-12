package com.ignis.norabotics.common.handlers;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.IChunkLoader;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.helpers.types.Tuple;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkLoadingHandler {

    @SubscribeEvent
    public static void onEntityEntersChunk(EntityEvent.EnteringSection event) {
        if(!event.didChunkChange()) return;
        if(event.getEntity().level().isClientSide()) return;
        event.getEntity().getCapability(ModCapabilities.CHUNK_LOADER).ifPresent(loader -> {
            loader.unloadChunks(event.getOldPos().chunk());
            loader.loadChunks(event.getNewPos().chunk());
        });
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if(event.getEntity().level().isClientSide()) return;
        event.getEntity().getCapability(ModCapabilities.CHUNK_LOADER).ifPresent(loader -> {
            loader.unloadChunks(event.getEntity().chunkPosition());
        });
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if(!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        serverLevel.getCapability(ModCapabilities.CHUNK_TRACKER).ifPresent(tracker -> {
            for(Tuple<ChunkPos,UUID> pair : tracker.pending) {
                Entity entity = serverLevel.getEntity(pair.getSecond());
                if(entity == null) continue;
                entity.getCapability(ModCapabilities.CHUNK_LOADER).ifPresent(loader -> tracker.add(pair.getFirst(), loader));
            }
            tracker.pending.clear();
        });
    }

    /**
     * Keeps track of which Entities(UUIDs) are attempting to load which chunks, so Chunks are not removed to eagerly if multiple Entities are currently loading it
     */
    @AutoRegisterCapability
    public static class ChunkTracker implements INBTSerializable<CompoundTag> {

        private final ServerLevel world;
        private final Map<ChunkPos, List<UUID>> chunks = new HashMap<>();
        final Deque<Tuple<ChunkPos, UUID>> pending = new LinkedList<>();

        public ChunkTracker(ServerLevel world){
            this.world = world;
        }

        public ChunkTracker() {
            this(null);
        }

        public void add(ChunkPos chunk, IChunkLoader loader) {
            if(this.chunks.containsKey(chunk) && this.chunks.get(chunk).contains(loader.getUUID()))
                return;

            //The world might not have loaded the entity yet. ForgeChunkManager usually ensures that entities with tickets are loaded
            if(world.getEntity(loader.getUUID()) == null) return;

            if(!this.chunks.containsKey(chunk)) {
                this.chunks.put(chunk, new LinkedList<>());
                ForgeChunkManager.forceChunk(world, Robotics.MODID, loader.getUUID(), chunk.x, chunk.z, true, false);
            }

            this.chunks.get(chunk).add(loader.getUUID());
        }

        public void remove(ChunkPos chunk, IChunkLoader loader) {
            if(!this.chunks.containsKey(chunk) || !this.chunks.get(chunk).contains(loader.getUUID()))
                return;

            if(this.chunks.get(chunk).size() == 1){
                ForgeChunkManager.forceChunk(world, Robotics.MODID, loader.getUUID(), chunk.x, chunk.z, false, false);
                this.chunks.remove(chunk);
            } else this.chunks.get(chunk).remove(loader.getUUID());
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            for(Map.Entry<ChunkPos,List<UUID>> entry : this.chunks.entrySet()) {
                CompoundTag chunkTag = new CompoundTag();
                chunkTag.putInt("chunkX", entry.getKey().x);
                chunkTag.putInt("chunkY", entry.getKey().z);


                ListTag uuids = new ListTag();
                entry.getValue().forEach(uuid -> uuids.add(StringTag.valueOf(uuid.toString())));
                chunkTag.put("uuids", uuids);

                nbt.put(entry.getKey().x + ";" + entry.getKey().z, chunkTag);
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            for(String key : nbt.getAllKeys()) {
                CompoundTag chunkTag = nbt.getCompound(key);
                ChunkPos chunk = new ChunkPos(chunkTag.getInt("chunkX"), chunkTag.getInt("chunkY"));

                ListTag uuids = chunkTag.getList("uuids", Tag.TAG_STRING);
                for (Tag value : uuids) {
                    StringTag tag = (StringTag) value;
                    UUID uuid = UUID.fromString(tag.getAsString());
                    pending.add(new Tuple<>(chunk, uuid));
                }
            }
        }
    }
}
