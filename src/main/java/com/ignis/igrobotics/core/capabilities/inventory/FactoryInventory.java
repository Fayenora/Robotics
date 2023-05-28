package com.ignis.igrobotics.core.capabilities.inventory;

import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotModule;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.definitions.ModAttributes;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
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
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        EnumRobotPart part = RobotPart.getFromItem(stack.getItem()).getPart();
        return switch(slot) {
            case 0 -> part == EnumRobotPart.HEAD;
            case 1 -> part == EnumRobotPart.BODY;
            case 2 -> part == EnumRobotPart.LEFT_ARM;
            case 3 -> part == EnumRobotPart.RIGHT_ARM;
            case 4 -> part== EnumRobotPart.LEFT_LEG;
            case 5 -> part == EnumRobotPart.RIGHT_LEG;
            default -> RobotModule.isModule(stack);
        };
    }

    @Override
    public void setStackInSlot(int index, @NotNull ItemStack stack) {
        if(factory.hasCraftedRobotReady()) return;
        super.setStackInSlot(index, stack);
    }

    @Override
    protected void onContentsChanged(int index) {
        deriveEntity(index);
        factory.sync();
    }

    private void deriveEntity(int slotIndex) {
        if(factory.getEntity().isEmpty() || !(factory.getEntity().get() instanceof LivingEntity living)) return;
        if(slotIndex < 6) {
            EnumRobotPart part = EnumRobotPart.byId(slotIndex);
            if(!getStackInSlot(slotIndex).isEmpty()) {
                EnumRobotMaterial material = RobotPart.getFromItem(getStackInSlot(slotIndex).getItem()).getMaterial();
                factory.setRobotPart(part, material);
            } else {
                factory.setRobotPart(part, EnumRobotMaterial.NONE);
            }
        } else if(!factory.getLevel().isClientSide()) {
            living.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> robot.setModules(stacks.subList(6, getSlots())));
        }
        if(factory.getLevel().isClientSide()) return;
        setSize((int) (6 + living.getAttributeValue(ModAttributes.MODIFIER_SLOTS)));
    }

    public void deriveEntity() {
        for(int i = 0; i < getSlots(); i++) {
            deriveEntity(i);
        }
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
        if(factory.hasCraftedRobotReady()) return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(factory.hasCraftedRobotReady()) return ItemStack.EMPTY;
        return super.extractItem(slot, amount, simulate);
    }
}
