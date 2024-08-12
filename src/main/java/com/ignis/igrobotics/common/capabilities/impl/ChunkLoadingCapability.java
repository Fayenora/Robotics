package com.ignis.igrobotics.common.capabilities.impl;

import com.ignis.igrobotics.common.capabilities.IChunkLoader;
import com.ignis.igrobotics.common.capabilities.IRobot;
import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.handlers.ChunkLoadingHandler;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.util.Optional;
import java.util.UUID;

public class ChunkLoadingCapability implements IChunkLoader {

    private final Entity entity;

    public ChunkLoadingCapability(Entity entity) {
        this.entity = entity;
    }

    @Override
    public UUID getUUID() {
        return entity.getUUID();
    }

    @Override
    public void loadChunks(ChunkPos chunk) {
        if(entity.level().isClientSide()) return;
        Optional<ChunkLoadingHandler.ChunkTracker> opt = entity.level().getCapability(ModCapabilities.CHUNK_TRACKER).resolve();
        if(opt.isEmpty()) return;
        Optional<IRobot> robot = entity.getCapability(ModCapabilities.ROBOT).resolve();
        if(robot.isEmpty()) return;
        if(RoboticsConfig.general.chunkLoadShutdown.get() && !robot.get().isActive()) return;

        ChunkLoadingHandler.ChunkTracker tracker = opt.get();
        switch(robot.get().getChunkLoadingState()) {
            case 0: break; //NO-OP
            case 1: tracker.add(chunk, this); break;
            case 2:
                for(int i = -1; i <= 1; i++) {
                    for(int j = -1; j <= 1; j++) {
                        tracker.add(new ChunkPos(chunk.x + i, chunk.z + j), this);
                    }
                }
                break;
        }
    }

    @Override
    public void unloadChunks(ChunkPos chunk) {
        if(entity.level().isClientSide()) return;
        entity.level().getCapability(ModCapabilities.CHUNK_TRACKER).ifPresent(tracker -> {
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    tracker.remove(new ChunkPos(chunk.x + i, chunk.z + j), this);
                }
            }
        });
    }
}
