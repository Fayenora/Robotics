package com.ignis.norabotics.common.helpers.types;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class Selection<A> implements INBTSerializable<CompoundTag> {
	
	private SelectionType<A> type;
	private A target;

	private Selection(A value) {
		set(value);
	}
	
	private Selection(SelectionType<A> type) {
		this.type = type;
		this.target = type.defaultsTo().get();
	}
	
	private Selection(CompoundTag tag) {
		deserializeNBT(tag);
	}
	
	public static CompoundTag writeNBT(Selection<?> sel) {
		CompoundTag tag = (CompoundTag) sel.getType().writer().apply(sel.get());
		tag.putInt("type", sel.getType().getId());
		return tag;
	}
	
	public static Selection<?> readNBT(CompoundTag tag) {
		Selection sel = new Selection<>(SelectionType.byId(tag.getInt("type")));
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
		this.type = value == null ? null : SelectionType.byClass(value.getClass());
		this.target = value;
	}
	
	@Override
	public Selection<A> clone() {
		return new Selection<>(target);
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

	@Override
	public String toString() {
		return type.toString(target);
	}

	public static <F> Selection<F> of(F value) {
		return new Selection<>(value);
	}

	public static <F> Selection<F> ofType(SelectionType<F> type) {
		return new Selection<>(type);
	}

	public static <F> Selection<F> read(CompoundTag tag) {
		return new Selection<>(tag);
	}
}
