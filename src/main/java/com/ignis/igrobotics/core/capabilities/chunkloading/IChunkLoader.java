package com.ignis.igrobotics.core.capabilities.chunkloading;

import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public interface IChunkLoader {
	
	UUID getUUID();
	
	UUID getOwner();
	
	void loadChunks(ChunkPos chunk);
	
	void unloadChunks(ChunkPos chunk);

}
