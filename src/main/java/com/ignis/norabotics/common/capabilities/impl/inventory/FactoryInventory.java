package com.ignis.norabotics.common.capabilities.impl.inventory;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.content.blockentity.FactoryBlockEntity;
import com.ignis.norabotics.common.robot.EnumModuleSlot;
import com.ignis.norabotics.common.robot.EnumRobotMaterial;
import com.ignis.norabotics.common.robot.EnumRobotPart;
import com.ignis.norabotics.common.robot.RobotPart;
import com.ignis.norabotics.definitions.robotics.ModModules;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class FactoryInventory extends MachineInventory {

    private final FactoryBlockEntity factory;

    public FactoryInventory(FactoryBlockEntity factory, int size) {
        super(factory, size);
        this.factory = factory;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        RobotPart part = RobotPart.getFromItem(stack.getItem());
        if(part == null) {
            if(slot >= 6 && ModModules.isModule(stack)) {
                EnumSet<EnumModuleSlot> allowedSlots = ModModules.get(stack).getViableSlots();
                EnumModuleSlot slotType = typeFromSlotId(slot);
                return allowedSlots.contains(slotType);
            }
            return false;
        }
        return switch(slot) {
            case 0 -> part.getPart() == EnumRobotPart.HEAD;
            case 1 -> part.getPart() == EnumRobotPart.BODY;
            case 2 -> part.getPart() == EnumRobotPart.LEFT_ARM;
            case 3 -> part.getPart() == EnumRobotPart.RIGHT_ARM;
            case 4 -> part.getPart() == EnumRobotPart.LEFT_LEG;
            case 5 -> part.getPart() == EnumRobotPart.RIGHT_LEG;
            default -> false;
        };
    }

    /**
     * The module slot type associated with this slot id
     * @param slot the slot
     * @see #typeToSlotId(EnumModuleSlot, int) 
     * @return the module type which can fit in the slot
     */
    public static EnumModuleSlot typeFromSlotId(int slot) {
        return EnumModuleSlot.values()[Math.floorDiv(slot - 6, Reference.MAX_MODULES) + 6];
    }

    public static int typeToSlotId(EnumModuleSlot slotType, int offset) {
        return (slotType.ordinal() - 6) * Reference.MAX_MODULES + Math.min(Reference.MAX_MODULES - 1, offset) + 6;
    }

    @Override
    public void setStackInSlot(int index, @NotNull ItemStack stack) {
        if(factory.isRunningOrFinished()) return;
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
                RobotPart robotPart = RobotPart.getFromItem(getStackInSlot(slotIndex).getItem());
                factory.setRobotPart(part, robotPart == null ? EnumRobotMaterial.NONE : robotPart.getMaterial());
            } else {
                factory.setRobotPart(part, EnumRobotMaterial.NONE);
            }
        } else if(!factory.getLevel().isClientSide()) {
            living.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                for(EnumModuleSlot slot : EnumModuleSlot.nonPrimaries()) {
                    robot.setModules(slot, stacks.subList(typeToSlotId(slot, 0), typeToSlotId(slot, Reference.MAX_MODULES)));
                }
            });
        }
    }

    public void deriveEntity() {
        for(int i = 0; i < getSlots(); i++) {
            deriveEntity(i);
        }
        factory.sync();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {
        if(factory.isRunningOrFinished()) return false;
        return super.canTakeItemThroughFace(slot, stack, face);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction face) {
        if(factory.isRunningOrFinished()) return false;
        return super.canPlaceItemThroughFace(slot, stack, face);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(factory.isRunningOrFinished()) return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(factory.isRunningOrFinished()) return ItemStack.EMPTY;
        return super.extractItem(slot, amount, simulate);
    }
}
