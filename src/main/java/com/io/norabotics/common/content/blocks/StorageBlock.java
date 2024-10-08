package com.io.norabotics.common.content.blocks;

import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.content.blockentity.StorageBlockEntity;
import com.io.norabotics.definitions.ModItems;
import com.io.norabotics.definitions.ModMachines;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageBlock extends MachineBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public static final VoxelShape NORTH_TOP = Block.box(0, -16, 1, 16, 16, 16);
    public static final VoxelShape NORTH_BOTTOM = Block.box(0, 0, 1, 16, 32, 16);

    public static final VoxelShape EAST_TOP = Block.box(0, -16, 0, 15, 16, 16);
    public static final VoxelShape EAST_BOTTOM = Block.box(0, 0, 0, 15, 32, 16);

    public static final VoxelShape SOUTH_TOP = Block.box(0, -16, 0, 16, 16, 15);
    public static final VoxelShape SOUTH_BOTTOM = Block.box(0, 0, 0, 16, 32, 15);

    public static final VoxelShape WEST_TOP = Block.box(1, -16, 0, 16, 16, 16);
    public static final VoxelShape WEST_BOTTOM = Block.box(1, 0, 0, 16, 32, 16);

    public static final VoxelShape TOP = Block.box(0, -16, 0, 16, 16, 16);
    public static final VoxelShape BOTTOM = Block.box(0, 0, 0, 16, 32, 16);

    public StorageBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new StorageBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState pState, BlockEntityType<T> type) {
        if(level.isClientSide()) return null;
        return createTickerHelper(type, ModMachines.ROBOT_STORAGE.getBlockEntityType(), StorageBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.getItem() == ModItems.COMMANDER.get()) return InteractionResult.PASS;
        if(state.getValue(HALF) == DoubleBlockHalf.UPPER) pos = pos.below();
        return super.use(level.getBlockState(pos), level, pos, player, hand, hit);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        if(level.hasNeighborSignal(pos)) {
            if(!state.getValue(POWERED)) {
                if(level.getBlockEntity(pos) instanceof StorageBlockEntity storage) {
                    if(storage.getEntity().isEmpty()) {
                        AABB upTakeArea = AABB.ofSize(Vec3.atCenterOf(pos)
                                .relative(state.getValue(MachineBlock.FACING), 0.5)
                                .relative(state.getValue(HALF) == DoubleBlockHalf.UPPER ? Direction.DOWN : Direction.UP, 0.5),
                                1, 2, 1);
                        List<Entity> availableEntities = level.getEntities((Entity) null, upTakeArea, e -> e.getCapability(ModCapabilities.ROBOT).isPresent());
                        if(!availableEntities.isEmpty()) storage.enterStorage(availableEntities.get(0));
                    } else storage.exitStorage(state.getValue(MachineBlock.FACING));
                }
                level.setBlock(pos, state.setValue(POWERED, true), 2);
            }
        } else {
            level.setBlock(pos, state.setValue(POWERED, false), 2);
        }
        super.neighborChanged(state, level, pos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if(state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            level.destroyBlock(pos.above(), false);
        } else level.destroyBlock(pos.below(), false);
        super.destroy(level, pos, state);
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        super.spawnDestroyParticles(level, player, pos, state);
        super.spawnDestroyParticles(level, player, pos.above(), state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        level.setBlock(pos.above(), defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(FACING, state.getValue(FACING)), 3);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch(state.getValue(MachineBlock.FACING)) {
            case NORTH -> state.getValue(HALF) == DoubleBlockHalf.UPPER ? NORTH_TOP : NORTH_BOTTOM;
            case EAST -> state.getValue(HALF) == DoubleBlockHalf.UPPER ? EAST_TOP : EAST_BOTTOM;
            case SOUTH -> state.getValue(HALF) == DoubleBlockHalf.UPPER ? SOUTH_TOP : SOUTH_BOTTOM;
            case WEST -> state.getValue(HALF) == DoubleBlockHalf.UPPER ? WEST_TOP : WEST_BOTTOM;
            default -> state.getValue(HALF) == DoubleBlockHalf.UPPER ? TOP : BOTTOM;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        if (blockpos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(context)) {
            return super.getStateForPlacement(context).setValue(HALF, DoubleBlockHalf.LOWER).setValue(POWERED, level.hasNeighborSignal(blockpos));
        } else {
            return null;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> definition) {
        super.createBlockStateDefinition(definition);
        definition.add(HALF, POWERED);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }
}
