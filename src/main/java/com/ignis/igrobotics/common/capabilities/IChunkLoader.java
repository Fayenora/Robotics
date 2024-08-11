package com.ignis.igrobotics.common.capabilities;

import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.UUID;

@AutoRegisterCapability
public interface IChunkLoader {
	
	UUID getUUID();
	
	void loadChunks(ChunkPos chunk);
	
	void unloadChunks(ChunkPos chunk);

}
