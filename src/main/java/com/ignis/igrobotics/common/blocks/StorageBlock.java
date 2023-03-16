package com.ignis.igrobotics.common.blocks;

import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StorageBlock extends MachineBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public static final VoxelShape TOP = Block.box(0, -16, 0, 16, 16, 16);
    public static final VoxelShape BOTTOM = Block.box(0, 0, 0, 16, 32, 16);

    public StorageBlock() {
        super(Properties.of(Material.HEAVY_METAL).strength(5f));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new StorageBlockEntity(pos, state) : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        if(state.getValue(HALF) == DoubleBlockHalf.UPPER) pos = pos.below(); //Reassign pos here to only handle the tile-entity origin block pos

        //TODO Logic:
        /*
        //If the player holds anything else than the Commander, simply open the gui
		if(playerIn.getHeldItem(hand).getItem() != ModItems.commander) return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);

		EntityLiving living = ItemCommander.getRememberedEntity(worldIn, stack);
		BlockPos pos_other = ItemCommander.getRememberedPos(stack);
		//Move a robot into storage
		if(living != null && living instanceof EntityRobot) {
			EntityRobot robot = (EntityRobot) living;
			ItemCommander.rememberPos(stack, pos);
			robot.targetTasks.addTask(2, new EntityAIEnterStorage(robot, pos));
			robot.playSound(RegisterSounds.ENTITY_ROBOT_COMMAND, 1, 1);
			return true;
		}
		//Move a robot out of another storage into this one
		if(pos_other != null && !pos.equals(pos_other) && worldIn.getBlockState(pos_other).getBlock() == ModBlocks.robot_storage) {
			TileEntityRobotStorage storage = (TileEntityRobotStorage) worldIn.getTileEntity(pos_other);
			//Selected storage needs to contain a robot
			if(storage.containsRobot()) {
				ItemCommander.clearRememberedPos(stack);
				ItemCommander.rememberPos(stack, pos);
				EntityRobot robot_spawned = storage.createRobot();
				robot_spawned.targetTasks.addTask(2, new EntityAIEnterStorage(robot_spawned, pos));
				robot_spawned.playSound(RegisterSounds.ENTITY_ROBOT_COMMAND, 1, 1);
				return true;
			}
		}
		//Remember the position if sneaking, otherwise just open the gui as default
		if(playerIn.isSneaking()) {
			ItemCommander.rememberPos(stack, pos);
			playerIn.sendMessage(new TextComponentTranslation("commandGroup.selected.pos"));
			return true;
		}
         */
        return InteractionResult.CONSUME;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if(state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            level.destroyBlock(pos.above(), false);
        } else level.destroyBlock(pos.below(), false);
        super.destroy(level, pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        level.setBlock(pos.above(), defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(FACING, state.getValue(FACING)), 3);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? TOP : BOTTOM;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        if (blockpos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(context)) {
            return super.getStateForPlacement(context).setValue(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> definition) {
        super.createBlockStateDefinition(definition);
        definition.add(HALF);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }
}
