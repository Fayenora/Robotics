package com.ignis.igrobotics.integration.cc;

import com.google.common.collect.ImmutableMap;
import com.ignis.igrobotics.common.blockentity.RedstoneIntegrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class RedstoneInterface extends Block implements EntityBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RedstoneInterface() {
        super(Properties.of(Material.AIR).air().noCollission().noOcclusion().dynamicShape());
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public int getSignal(BlockState p_60483_, BlockGetter level, BlockPos pos, Direction dir) {
        if(!(level.getBlockEntity(pos) instanceof RedstoneIntegrator integrator)) return 0;
        if(!integrator.isValid() && level instanceof ServerLevel server) server.removeBlock(pos, true);
        return integrator.getSignalStrengthForSide(dir);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneIntegrator(pos, state);
    }

    @Override
    protected ImmutableMap<BlockState, VoxelShape> getShapeForEachState(Function<BlockState, VoxelShape> func) {
        return super.getShapeForEachState(ign -> Shapes.empty());
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return Shapes.empty();
    }
}
