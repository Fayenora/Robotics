package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.common.blocks.BlockMachine;
import com.ignis.igrobotics.core.Machine;
import com.ignis.igrobotics.core.MachineRecipe;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import com.ignis.igrobotics.core.capabilities.inventory.MachineInventory;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public abstract class BlockEntityMachine extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {

    protected EnergyStorage storage;
    protected MachineInventory inventory;
    private final Machine machine;
    private final int[] inputs;
    private final int[] outputs;

    private int runTime;
    private int currentRunTime;
    protected MachineRecipe currentRecipe;
    private ItemStack[] currentlyProcessedItems;

    private final MachineRecipe[] RECIPES;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int id) {
            switch(id) {
                case 0: return inventory.getSlots();
                case 1: return runTime;
                case 2: return currentRunTime;
                case 3: return storage.getEnergyStored();
                case 4: return storage.getMaxEnergyStored();
                default: return 0;
            }
        }

        @Override
        public void set(int id, int value) {
            switch(id) {
                case 0: inventory.setSize(value); break;
                case 1: runTime = value; break;
                case 2: currentRunTime = value; break;
                case 3: storage.setEnergy(value); break;
                case 4: storage.setMaxEnergyStored(value); break;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    protected BlockEntityMachine(Machine machine, BlockPos pos, BlockState state, int inventorySize, int[] inputs, int[] outputs) {
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
        onEnteredWorld();
    }

    protected void onEnteredWorld() {}

    public void dropInventory() {
        Containers.dropContents(level, getBlockPos(), this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityMachine machine) {
        boolean flag1 = false;
        boolean flag2 = machine.isRunning();

        if(machine.isRunning() && machine.hasEnoughEnergy(machine.currentRecipe)) {
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
        } else if (machine.hasRecipe() && machine.hasEnoughEnergy(machine.currentRecipe)) {
            machine.currentRunTime = 1;

            machine.onMachineStart();
            machine.consumeEnergy(machine.currentRecipe);
            machine.consumeInputs(machine.currentRecipe);

            flag1 = true;
        }

        if (flag2 != machine.isRunning()) {
            flag1 = true;
            state = state.setValue(BlockMachine.RUNNING, machine.isRunning());
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
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction face) {
        return IntStream.of(getSlotsForFace(face)).anyMatch(x -> x == slot);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {
        return IntStream.of(getSlotsForFace(face)).anyMatch(x -> x == slot);
    }

    //////////////////////////
    // Capabilities
    //////////////////////////

    LazyOptional<? extends IItemHandler>[] inventory_cap = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);
    LazyOptional<? extends IEnergyStorage> energy_cap = LazyOptional.of(() -> storage);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if(remove) return super.getCapability(cap, side);
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            switch(side) {
                case UP: return inventory_cap[0].cast();
                case DOWN: return inventory_cap[1].cast();
                default: return inventory_cap[2].cast();
            }
        }
        if(cap == CapabilityEnergy.ENERGY) {
            return energy_cap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy_cap.invalidate();
        for(LazyOptional cap : inventory_cap) {
            cap.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        inventory_cap = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);
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
    public boolean stillValid(Player p_18946_) {
        return true;
    } //TODO Only players in a certain range are still valid

    @Override
    public void clearContent() {
        inventory.clear();
    }
}
