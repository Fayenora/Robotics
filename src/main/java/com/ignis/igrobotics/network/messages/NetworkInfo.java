package com.ignis.igrobotics.network.messages;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Common network info for guis to be used in packets
 * @author Ignis
 *
 */
public class NetworkInfo implements IBufferSerializable {
	
	private int dataX, dataY, dataZ;
	
	public NetworkInfo() {}
	
	public NetworkInfo(int dataX, int dataY, int dataZ) {
		this.dataX = dataX;
		this.dataY = dataY;
		this.dataZ = dataZ;
	}
	
	public NetworkInfo(BlockPos pos) {
		this.dataX = pos.getX();
		this.dataY = pos.getY();
		this.dataZ = pos.getZ();
	}
	
	public NetworkInfo(Entity ent) {
		this.dataX = ent.getId();
	}
	
	@Override
	public void read(FriendlyByteBuf buf) {
		dataX = buf.readInt();
		dataY = buf.readInt();
		dataZ = buf.readInt();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(dataX);
		buf.writeInt(dataY);
		buf.writeInt(dataZ);
	}
	
	public BlockPos getAsPos() {
		return new BlockPos(dataX, dataY, dataZ);
	}
	
	public BlockEntity getAsBlockEntity(Level level) {
		return level.getBlockEntity(getAsPos());
	}
	
	public Entity getAsEntity(Level level) {
		return level.getEntity(dataX);
	}

}
