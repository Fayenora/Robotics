package com.ignis.igrobotics.core.capabilities.inventory;

import com.google.common.primitives.Ints;
import com.ignis.igrobotics.common.blockentity.MachineBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MachineInventory extends BaseInventory {

    protected MachineBlockEntity machine;
    List<Integer> outputSlots = new LinkedList<>();
    Map<Direction, List<Integer>> accessibleSlotPerFace = new HashMap<>();

    public MachineInventory(MachineBlockEntity machine, int size) {
        super(() -> machine.getBlockPos(), size);
        this.machine = machine;
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        super.setStackInSlot(index, stack);
        machine.startMachine(1);
    }

    public int[] getSlotsForFace(@Nullable Direction side) {
        List<Integer> defaultSlots = accessibleSlotPerFace.get(null); //The default slots can be retrieved with the null key
        if(defaultSlots == null) {
            defaultSlots = new LinkedList<>();
            defaultSlots.addAll(outputSlots);
        }
        return Ints.toArray(accessibleSlotPerFace.getOrDefault(side, defaultSlots));
    }

    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction face) {
        if(face != null && outputSlots.contains(slot)) {
            return false;
        }
        return IntStream.of(getSlotsForFace(face)).anyMatch(x -> x == slot);
    }

    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {
        return IntStream.of(getSlotsForFace(face)).anyMatch(x -> x == slot);
    }

    public void setOutputSlots(int... outputs) {
        outputSlots = Ints.asList(outputs);
    }

    public void setValidSlotsForFace(@Nullable Direction side, int... slots) {
        accessibleSlotPerFace.put(side, Ints.asList(slots));
    }

    public void setDefaultAccessibleSlots(int... slots) {
        setValidSlotsForFace(null, slots);
    }

    public void setAllSlotsAccessibleByDefault() {
        int[] allSlots = new int[getSlots()];
        for(int i = 0; i < allSlots.length; i++) {
            allSlots[i] = i;
        }
        setDefaultAccessibleSlots(allSlots);
    }
}
