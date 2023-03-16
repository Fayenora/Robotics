package com.ignis.igrobotics.core.capabilities.inventory;

import com.ignis.igrobotics.common.blockentity.BlockEntityFactory;
import com.ignis.igrobotics.core.EnumRobotMaterial;
import com.ignis.igrobotics.core.EnumRobotPart;
import com.ignis.igrobotics.core.RobotPart;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FactoryInventory extends MachineInventory {

    private final BlockEntityFactory factory;

    public FactoryInventory(BlockEntityFactory factory, int size) {
        super(factory, size);
        this.factory = factory;
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        if(factory.hasCraftedRobotReady()) return;
        super.setStackInSlot(index, stack);
        if(factory.getLevel().isClientSide()) return;

        if(index < 6) {
            EnumRobotPart part = EnumRobotPart.getByID(index);
            if(!getStackInSlot(index).isEmpty()) {
                EnumRobotMaterial material = RobotPart.getFromItem(getStackInSlot(index).getItem()).getMaterial();
                factory.setRobotPart(part, material);
            } else {
                factory.setRobotPart(part, EnumRobotMaterial.NONE);
            }
        } else {
            //TODO factory.getRobot().setModules(inventory.subList(6, getSizeInventory()));
        }
        //TODO setSize(6 + factory.getRobot().getModuleCount());
        factory.sync();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {
        if(factory.hasCraftedRobotReady()) return false;
        return super.canTakeItemThroughFace(slot, stack, face);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction face) {
        if(factory.hasCraftedRobotReady()) return false;
        return super.canPlaceItemThroughFace(slot, stack, face);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(factory.hasCraftedRobotReady()) return ItemStack.EMPTY;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(factory.hasCraftedRobotReady()) return ItemStack.EMPTY;
        return super.extractItem(slot, amount, simulate);
    }
}
