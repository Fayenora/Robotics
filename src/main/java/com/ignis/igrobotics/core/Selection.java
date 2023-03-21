package com.ignis.igrobotics.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class Selection<A> implements INBTSerializable<CompoundTag> {
	
	private SelectionType type;
	private A target;
	
	public Selection(A value) {
		set(value);
	}
	
	public Selection(SelectionType type) {
		this.type = type;
		this.target = (A) type.defaultsTo();
	}
	
	public Selection(CompoundTag tag) {
		deserializeNBT(tag);
	}
	
	public static CompoundTag writeNBT(Selection sel) {
		return (CompoundTag) sel.getType().writer().apply(sel.get());
	}
	
	public static Selection readNBT(CompoundTag tag) {
		Selection sel = new Selection(SelectionType.byId(tag.getInt("type")));
		sel.target = sel.getType().reader().apply(tag);
		return sel;
	}
	
	public SelectionType getType() {
		return type;
	}

	public A get() {
		return target;
	}

	public void set(A value) {
		this.type = SelectionType.byClass(value.getClass());
		this.target = value;
	}
	
	@Override
	public Selection clone() {
		return new Selection<A>(target);
	}

	@Override
	public CompoundTag serializeNBT() {
		return writeNBT(this);
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.type = readNBT(nbt).getType();
		this.target = (A) readNBT(nbt).target;
	}
	
}
