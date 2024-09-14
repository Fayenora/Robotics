package com.io.norabotics.common.content.blocks;

import com.io.norabotics.common.content.blockentity.AssemblerBlockEntity;
import com.io.norabotics.common.content.blockentity.MachineBlockEntity;
import com.io.norabotics.definitions.ModMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AssemblerBlock extends MachineBlock {
    public AssemblerBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AssemblerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide()) return createTickerHelper(type, ModMachines.ASSEMBLER.getBlockEntityType(), MachineBlockEntity::clientTick);
        return createTickerHelper(type, ModMachines.ASSEMBLER.getBlockEntityType(), MachineBlockEntity::serverTick);
    }
}
