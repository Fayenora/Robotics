package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.client.SoundHandler;
import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.ignis.igrobotics.core.Machine;
import com.ignis.igrobotics.core.MachineRecipe;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import com.ignis.igrobotics.core.capabilities.inventory.MachineInventory;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MachineBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {

    protected EnergyStorage storage;
    protected MachineInventory inventory;
    private final Machine<?> machine;
    private final int[] inputs;
    private final int[] outputs;

    private int runTime;
    private int currentRunTime;
    protected MachineRecipe<?> currentRecipe;
    private ItemStack[] currentlyProcessedItems;

    private final List<MachineRecipe<?>> RECIPES;

    @OnlyIn(Dist.CLIENT)
    SoundInstance activeSound;
    int soundCooldown;

    public static final int DATA_INVENTORY = 0;
    protected ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int id) {
            return switch (id) {
                case DATA_INVENTORY -> inventory.getSlots();
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
                case DATA_INVENTORY -> inventory.setSize(value);
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

                if (machine.currentRunTime >= machine.runTime) {
                    machine.craftItem(machine.currentRecipe);
                    machine.onItemCrafted();

                    //Evaluate the next recipe
                    machine.currentRecipe = machine.getRecipe();
                    machine.currentlyProcessedItems = ItemStackUtils.full(machine.inputs.length, ItemStack.EMPTY);
                    if(machine.hasRecipe()) { //Directly head to the next recipe, if present
                        machine.onMachineStart();
                        machine.consumeEnergy(machine.currentRecipe);
                        machine.consumeInputs(machine.currentRecipe);
                        machine.runTime = machine.currentRecipe.getProcessingTime();
                        machine.currentRunTime = 1;
                    } else { //Shut down the machine
                        machine.runTime = 0;
                        machine.currentRunTime = 0;
                    }

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

    public static void clientTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity machine) {
        machine.updateSound();
    }

    protected abstract void onItemCrafted();

    protected abstract void onMachineStart();

    /**
     * @return the recipe this machine would craft, if the specified inputStacks would be inputted right now
     * @param inputStacks the inputted items
     */
    @Nullable
    protected MachineRecipe<?> getRecipe(ItemStack[] inputStacks) {
        ItemStack[] outputStacks = getStacks(outputs);

        if(!ItemStackUtils.areBeneathMaxStackSize(outputStacks) || inputStacks.length == 0) {
            return null;
        }

        for(MachineRecipe<?> recipe : RECIPES) {
            if(recipe.matches(this, level) && (ItemStackUtils.areEmpty(outputStacks) || ItemStackUtils.areItemsEqual(outputStacks, recipe.getOutputs()))) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * @return the recipe this machine would craft if it starts now
     */
    @Nullable
    private MachineRecipe<?> getRecipe() {
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

    protected void consumeInputs(MachineRecipe<?> recipe) {
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

    protected void craftItem(MachineRecipe<?> recipe) {
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

    protected void consumeEnergy(MachineRecipe<?> recipe) {
        this.storage.extractEnergy((recipe.getEnergyPerTick()), false);
    }

    protected boolean hasEnoughEnergy(MachineRecipe<?> recipe) {
        int extractable = this.storage.extractEnergy(recipe.getEnergyPerTick(), true);
        int recipeRequirement = recipe.getEnergyPerTick();
        return extractable == recipeRequirement;
    }

    public boolean isRunning() {
        return level != null && level.isClientSide() ? getBlockState().getValue(MachineBlock.RUNNING) : this.currentRunTime > 0;
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
    public <T> @NotNull LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
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
        for(LazyOptional<?> cap : inventory_caps) {
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
        if(tagList.size() == 0) {
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
    // Sounds
    //////////////////////////

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(level.isClientSide) {
            updateSound();
        }
    }

    protected boolean canPlaySound() {
        return isRunning() && !isRemoved();
    }

    @OnlyIn(Dist.CLIENT)
    private void updateSound() {
        if(!RoboticsConfig.client.machineSoundsEnabled.get() || getRunningSound() == null) return;
        if(canPlaySound()) {
            // If sounds are being muted, we can attempt to start them on every tick, only to have them
            // denied by the event bus, so use a cooldown period that ensures we're only trying once every
            // second or so to start a sound.
            if(--soundCooldown > 0) {
                return;
            }

            // If this machine isn't fully muffled, and we don't seem to be playing a sound for it, go ahead and
            // play it
            if(activeSound == null || !Minecraft.getInstance().getSoundManager().isActive(activeSound)) {
                activeSound = SoundHandler.startTileSound(getRunningSound(), SoundSource.BLOCKS, getVolume(), level.getRandom(), getBlockPos());
            }
            // Always reset the cooldown; either we just attempted to play a sound or we're fully muffled; either way
            // we don't want to try again
            soundCooldown = 20;
        } else if(activeSound != null) {
            SoundHandler.stopTileSound(getBlockPos());
            activeSound = null;
            soundCooldown = 0;
        }
    }

    public float getVolume() {
        return 1;
    }

    @Nullable
    public abstract SoundEvent getRunningSound();

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
        if(!(recipe instanceof MachineRecipe<?> machineRecipe)) return;
        this.currentRecipe = machineRecipe;
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
