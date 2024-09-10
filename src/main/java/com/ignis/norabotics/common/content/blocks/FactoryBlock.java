package com.ignis.norabotics.common.content.blocks;

import com.ignis.norabotics.common.content.blockentity.FactoryBlockEntity;
import com.ignis.norabotics.common.content.blockentity.MachineBlockEntity;
import com.ignis.norabotics.definitions.ModMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FactoryBlock extends StorageBlock {

    public static final VoxelShape TOP = Shapes.or(
            Block.box(3, -16, 3, 13, 16, 13),
            Block.box(0, -16, 0, 16, -15, 16)
    );
    public static final VoxelShape BOTTOM = Shapes.or(
            Block.box(3, 0, 3, 13, 32, 13),
            Block.box(0, 0, 0, 16, 1, 16)
    );

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new FactoryBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState pState, BlockEntityType<T> type) {
        if(level.isClientSide()) return null;
        return createTickerHelper(type, ModMachines.ROBOT_FACTORY.getBlockEntityType(), MachineBlockEntity::serverTick);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? TOP : BOTTOM;
    }
}
