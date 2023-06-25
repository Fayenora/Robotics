package com.ignis.igrobotics.integration.cc;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class EntityComputer extends ServerComputer {

    private final LivingEntity entity;

    public EntityComputer(ServerLevel level, LivingEntity entity, int computerID, @Nullable String label, ComputerFamily family, int terminalWidth, int terminalHeight) {
        super(level, entity.blockPosition(), computerID, label, family, terminalWidth, terminalHeight);
        this.entity = entity;
    }

    @Override
    public BlockPos getPosition() {
        return entity.blockPosition();
    }
}
