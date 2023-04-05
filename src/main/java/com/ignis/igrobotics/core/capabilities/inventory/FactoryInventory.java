package com.ignis.igrobotics.core.capabilities.inventory;

import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.definitions.ModAttributes;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FactoryInventory extends MachineInventory {

    private final FactoryBlockEntity factory;

    public FactoryInventory(FactoryBlockEntity factory, int size) {
        super(factory, size);
        this.factory = factory;
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        if(factory.hasCraftedRobotReady()) return;
        super.setStackInSlot(index, stack);
        if(factory.getLevel().isClientSide()) return;
        IRobot robot = factory.getRobot().getCapability(ModCapabilities.ROBOT).orElse(null);
        if(robot == null) return;

        if(index < 6) {
            EnumRobotPart part = EnumRobotPart.byId(index);
            if(!stack.isEmpty()) {
                EnumRobotMaterial material = RobotPart.getFromItem(stack.getItem()).getMaterial();
                factory.setRobotPart(part, material);
            } else {
                factory.setRobotPart(part, EnumRobotMaterial.NONE);
            }
        } else {
            robot.setModules(stacks.subList(6, getSlots()));
        }
        setSize((int) (6 + factory.getRobot().getAttributeValue(ModAttributes.MODIFIER_SLOTS)));
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
