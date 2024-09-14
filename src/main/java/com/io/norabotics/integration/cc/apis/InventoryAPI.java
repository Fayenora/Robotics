package com.io.norabotics.integration.cc.apis;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.core.apis.IAPIEnvironment;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Optional;

public class InventoryAPI implements ILuaAPI {

    private final IAPIEnvironment environment;
    private final IItemHandlerModifiable inventory;

    public InventoryAPI(IAPIEnvironment environment, IItemHandlerModifiable inventory) {
        this.environment = environment;
        this.inventory = inventory;
    }

    @LuaFunction
    public final MethodResult getItem(ILuaContext context, Optional<Integer> slot, Optional<Boolean> detailed) throws LuaException {
        int actualSlot = normalizeSlotIndex(slot);
        if (detailed.orElse(false)) {
            return context.executeMainThreadTask(() -> {
                var stack = inventory.getStackInSlot(actualSlot);
                return new Object[]{ stack.isEmpty() ? null : VanillaDetailRegistries.ITEM_STACK.getDetails(stack) };
            });
        } else {
            //TODO: Make this more efficient by keeping a copy of the inventory which does not need to be accessed from the main thread
            // See {@link TurtleApi#getItemDetail}
            return context.executeMainThreadTask(() -> {
                var stack = inventory.getStackInSlot(actualSlot);
                return new Object[]{ stack.isEmpty() ? null : VanillaDetailRegistries.ITEM_STACK.getBasicDetails(stack) };
            });
        }
    }

    @LuaFunction
    public final void switchSlots(ILuaContext context, Optional<Integer> index1, Optional<Integer> index2) throws LuaException {
        int ind1 = normalizeSlotIndex(index1);
        int ind2 = normalizeSlotIndex(index2);
        context.executeMainThreadTask(() -> {
            var stack1 = inventory.getStackInSlot(ind1);
            var stack2 = inventory.getStackInSlot(ind2);
            if(!inventory.isItemValid(ind2, stack1)) throw new LuaException(stack1.getItem() + " is not allowed in slot " + ind2);
            if(!inventory.isItemValid(ind1, stack2)) throw new LuaException(stack2.getItem() + " is not allowed in slot " + ind1);
            inventory.setStackInSlot(ind2, stack1);
            inventory.setStackInSlot(ind1, stack2);
            return new Object[0];
        });
    }

    private int normalizeSlotIndex(Optional<Integer> index) {
        return Math.min(inventory.getSlots(), Math.max(index.orElse(1), 1)) - 1;
    }

    @Override
    public String[] getNames() {
        return new String[] {"inventory"};
    }
}
