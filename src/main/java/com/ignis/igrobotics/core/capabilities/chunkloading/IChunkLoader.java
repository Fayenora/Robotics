package com.ignis.igrobotics.core.capabilities.chunkloading;

import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.UUID;

@AutoRegisterCapability
public interface IChunkLoader {
	
	UUID getUUID();
	
	UUID getOwner();
	
	void loadChunks(ChunkPos chunk);
	
	void unloadChunks(ChunkPos chunk);

}
