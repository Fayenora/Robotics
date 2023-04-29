package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.ignis.igrobotics.core.Machine;
import com.ignis.igrobotics.core.MachineRecipe;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import com.ignis.igrobotics.core.capabilities.inventory.MachineInventory;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class MachineBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {

    protected EnergyStorage storage;
    protected MachineInventory inventory;
    private final Machine machine;
    private final int[] inputs;
    private final int[] outputs;

    private int runTime;
    private int currentRunTime;
    protected MachineRecipe currentRecipe;
    private ItemStack[] currentlyProcessedItems;

    private final List<MachineRecipe<?>> RECIPES;

    protected ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int id) {
            return switch (id) {
                case 0 -> inventory.getSlots();
                case 1 -> runTime;
                case 2 -> currentRunTime;
                case 3 -> storage.getEnergyStored();
                case 4 -> storage.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int id, int value) {
            switch (id) {
                case 0 -> inventory.setSize(value);
                case 1 -> runTime = value;
                case 2 -> currentRunTime = value;
                case 3 -> storage.setEnergy(value);
                case 4 -> storage.setMaxEnergyStored(value);
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public static boolean isRunning(ContainerData data) {
        return data.get(2) > 0;
    }

    protected MachineBlockEntity(Machine machine, BlockPos pos, BlockState state, int inventorySize, int[] inputs, int[] outputs) {
        super(machine.getBlockEntityType(), pos, state);
        this.machine = machine;
        storage = new EnergyStorage(machine.getEnergyCapacity(), machine.getEnergyTransfer());
        inventory = new MachineInventory(this, inventorySize);

        RECIPES = machine.getRecipes();
        currentlyProcessedItems = ItemStackUtils.full(inputs.length, ItemStack.EMPTY);
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(runTime == 0) {
            this.currentRunTime = 0;
        }
        //Apply config to existing machines
        storage.setMaxEnergyStored(machine.getEnergyCapacity());
    }

    @Override
    public void setLevel(Level p_155231_) {
        inventory.setLevel(level);
        super.setLevel(p_155231_);
    }

    public void sync() {
        level.sendBlockUpdated(getBlockPos(), level.getBlockState(getBlockPos()), level.getBlockState(getBlockPos()), 3);
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity machine) {
        boolean flag1 = false;
        boolean flag2 = machine.isRunning();

        if(machine.hasRecipe() && machine.hasEnoughEnergy(machine.currentRecipe)) {
            if(machine.isRunning()) {
                ++machine.currentRunTime;
                machine.consumeEnergy(machine.currentRecipe);

                if (machine.currentRunTime == machine.runTime) {
                    machine.craftItem(machine.currentRecipe);
                    machine.onItemCrafted();

                    //Evaluate the next recipe
                    machine.currentRecipe = machine.getRecipe();
                    machine.currentlyProcessedItems = ItemStackUtils.full(machine.inputs.length, ItemStack.EMPTY);
                    machine.currentRunTime = 0;
                    machine.runTime = machine.currentRecipe != null ? machine.currentRecipe.getProcessingTime() : 0;

                    flag1 = true;
                }
            } else {
                machine.currentRunTime = 1;

                machine.onMachineStart();
                machine.consumeEnergy(machine.currentRecipe);
                machine.consumeInputs(machine.currentRecipe);

                flag1 = true;
            }
        }

        if (flag2 != machine.isRunning()) {
            flag1 = true;
            state = state.setValue(MachineBlock.RUNNING, machine.isRunning());
            level.setBlock(pos, state, 3);
        }

        if (flag1) {
            setChanged(level, pos, state);
        }
    }

    protected abstract void onItemCrafted();

    protected abstract void onMachineStart();

    /**
     * @return the recipe this machine would craft, if the specified inputStacks would be inputted right now
     * @param inputStacks the inputted items
     */
    protected MachineRecipe getRecipe(ItemStack[] inputStacks) {
        ItemStack[] outputStacks = getStacks(outputs);

        if(!ItemStackUtils.areBeneathMaxStackSize(outputStacks) || inputStacks.length == 0) {
            return null;
        }

        for(MachineRecipe recipe : RECIPES) {
            if(recipe.matches(this, level) && (ItemStackUtils.areEmpty(outputStacks) || ItemStackUtils.areItemsEqual(outputStacks, recipe.getOutputs()))) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * @return the recipe this machine would craft if it starts now
     */
    private MachineRecipe getRecipe() {
        return getRecipe(getStacks(inputs));
    }

    public void startMachine(int from) {
        if(!level.isClientSide() && !isRunning()) {
            currentRecipe = getRecipe();
            runTime = hasRecipe() ? currentRecipe.getProcessingTime() : 0;
            sync();
        }
        setChanged();
    }

    protected void consumeInputs(MachineRecipe recipe) {
        for(int i = 0; i < inputs.length; i++) {
            int amount = 0;
            currentlyProcessedItems[i] = getStacks(inputs)[i].copy();

            if(recipe.getInputs()[i] != null && !(recipe.getInputs()[i].isEmpty())) {
                amount = (ItemStackUtils.getCount(recipe.getInputs()[i]));
                inventory.extractItem(inputs[i], amount, false);
            }

            currentlyProcessedItems[i].setCount(amount);
        }
    }

    protected void craftItem(MachineRecipe recipe) {
        int max = Math.min(recipe.getOutputs().length, outputs.length);

        for(int i = 0; i < max; i++) {

            ItemStack outputSlot = inventory.getStackInSlot(outputs[i]);
            ItemStack outputItem = recipe.getOutputs()[i];

            if(outputItem != null) {
                if(outputSlot.isEmpty()) {
                    inventory.setStackInSlot(outputs[i], outputItem.copy());
                } else if(outputSlot.getItem() == outputItem.getItem()) {
                    inventory.setStackInSlot(outputs[i], new ItemStack(outputItem.getItem(), outputSlot.getCount() + outputItem.getCount()));
                }
            }
        }
    }

    protected void consumeEnergy(@Nonnull MachineRecipe recipe) {
        this.storage.extractEnergy((recipe.getEnergyPerTick(machine.getEnergyConsumption(), machine.getProcessingSpeed())), false);
    }

    @Nonnull
    protected boolean hasEnoughEnergy(@Nonnull MachineRecipe recipe) {
        int extractable = this.storage.extractEnergy(recipe.getEnergyPerTick(machine.getEnergyConsumption(), machine.getProcessingSpeed()), true);
        int recipeRequirement = recipe.getEnergyPerTick(machine.getEnergyConsumption(), machine.getProcessingSpeed());
        return extractable == recipeRequirement;
    }

    public boolean isRunning() {
        return this.currentRunTime > 0;
    }

    public boolean hasRecipe() {
        return getRecipeUsed() != null;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return inventory.getSlotsForFace(direction);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction face) {
        return inventory.canPlaceItemThroughFace(slot, stack, face);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {
        return inventory.canTakeItemThroughFace(slot, stack, face);
    }

    //////////////////////////
    // Capabilities
    //////////////////////////

    LazyOptional<? extends IItemHandlerModifiable> inventory_cap = LazyOptional.of(() -> inventory);
    LazyOptional<? extends IItemHandlerModifiable>[] inventory_caps = SidedInvWrapper.create(this, Direction.values());
    LazyOptional<? extends IEnergyStorage> energy_cap = LazyOptional.of(() -> storage);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if(remove) return super.getCapability(cap, side);
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            if(side == null) return inventory_cap.cast();
            return inventory_caps[side.ordinal()].cast();
        }
        if(cap == ForgeCapabilities.ENERGY) {
            return energy_cap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy_cap.invalidate();
        for(LazyOptional cap : inventory_caps) {
            cap.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        inventory_caps = SidedInvWrapper.create(this, Direction.values());
        energy_cap = LazyOptional.of(() -> storage);
    }

    //////////////////////////
    // Saving & Syncing
    //////////////////////////

    private CompoundTag toSaveAndSync(CompoundTag compound) {
        compound.put("energy", storage.serializeNBT());
        compound.putInt("runTime", this.runTime);
        compound.putInt("currentRunTime", this.currentRunTime);

        if(!ItemStackUtils.areEmpty(currentlyProcessedItems)) {
            ListTag processedItems = new ListTag();
            for(ItemStack stack : currentlyProcessedItems) {
                processedItems.add(stack.serializeNBT());
            }
            compound.put("ProcessedItems", processedItems);
        }
        return compound;
    }

    private void fromSaveAndSync(CompoundTag compound) {
        storage.deserializeNBT(compound.getCompound("energy"));
        this.runTime = compound.getInt("runTime");
        this.currentRunTime = compound.getInt("currentRunTime");

        ListTag tagList = compound.getList("ProcessedItems", new CompoundTag().getId());
        if(tagList == null || tagList.size() == 0) {
            currentlyProcessedItems = ItemStackUtils.full(inputs.length, ItemStack.EMPTY);
        } else {
            for(int i = 0; i < Math.min(tagList.size(), currentlyProcessedItems.length); i++) {
                currentlyProcessedItems[i] = ItemStack.of(tagList.getCompound(i));
            }
        }
        currentRecipe = getRecipe(currentlyProcessedItems);
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("inventory", inventory.serializeNBT());
        toSaveAndSync(compound);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        inventory.deserializeNBT(compound.getCompound("inventory"));
        fromSaveAndSync(compound);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return toSaveAndSync(super.getUpdateTag());
    }

    //////////////////////////
    // Getters & Setters
    //////////////////////////

    @Override
    public void fillStackedContents(StackedContents contents) {
        for(int i = 0; i < inventory.getSlots(); i++) {
            contents.accountStack(inventory.getStackInSlot(i));
        }
    }

    @Nullable
    @Override
    public Recipe<?> getRecipeUsed() {
        return this.currentRecipe;
    }

    @Override
    public void setRecipeUsed(@Nullable Recipe<?> recipe) {
        if(!(recipe instanceof MachineRecipe)) return;
        this.currentRecipe = (MachineRecipe) recipe;
    }

    private ItemStack[] getStacks(int[] indices) {
        ItemStack[] inputStacks = new ItemStack[indices.length];

        for(int i = 0; i < indices.length; i++) {
            inputStacks[i] = inventory.getStackInSlot(indices[i]);
        }

        return inputStacks;
    }

    @Override
    public int getContainerSize() {
        return inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return inventory.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItem(int slot, int p_18943_) {
        return inventory.extractItem(slot, inventory.getSlotLimit(slot), false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, 0);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        inventory.setStackInSlot(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr(getBlockPos().getX() + 0.5D, getBlockPos().getY() + 0.5D, getBlockPos().getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }
}
