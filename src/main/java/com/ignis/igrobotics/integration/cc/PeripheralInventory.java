package com.ignis.igrobotics.integration.cc;

import com.ignis.igrobotics.core.capabilities.inventory.BaseInventory;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PeripheralInventory extends BaseInventory {

    private final Supplier<ServerComputer> computer;

    public PeripheralInventory(Supplier<ServerComputer> computer) {
        super(() -> computer.get().getPosition(), 6);
        this.computer = computer;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return super.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        super.setStackInSlot(slot, stack);
    }
}
