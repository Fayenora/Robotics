package com.ignis.igrobotics.common.entity.ai;


import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.ignis.igrobotics.common.blocks.StorageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class EnterStorageGoal extends MoveToBlockGoal {

	BlockPos storage;

	public EnterStorageGoal(PathfinderMob creature, BlockPos destinationBlock) {
		//destionationBlock is set to the front of the storage, so the robot walks there
		super(creature, getEnterPosition(creature.level.getBlockState(destinationBlock), destinationBlock));
		storage = creature.level.getBlockState(destinationBlock).getValue(StorageBlock.HALF) == DoubleBlockHalf.UPPER ? destinationBlock.below() : destinationBlock; //but can still enter the storage
	}
	
	@Override
	public void tick() {
		if(mob.distanceToSqr(storage.getCenter()) < 4) {
			BlockEntity tile = mob.level.getBlockEntity(storage);
			if(tile instanceof StorageBlockEntity storage) {
				storage.enterRobot(mob);
			}
		}
		super.tick();
	}

	public static BlockPos getEnterPosition(BlockState state, BlockPos storage) {
		return storage.below(state.getValue(StorageBlock.HALF) == DoubleBlockHalf.UPPER ? 2 : 1).relative(state.getValue(MachineBlock.FACING));
	}

}
