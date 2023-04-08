package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.FactoryMenu;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.MachineRecipe;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.inventory.FactoryInventory;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.definitions.ModMachines;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FactoryBlockEntity extends MachineBlockEntity {

    public static final int ENERGY_COST = 500000;
    public static final int WORK_TIME = 60;

    public static final MachineRecipe<?> DEFAULT_RECIPE = new MachineRecipe.Builder(ModMachines.ROBOT_FACTORY, new ResourceLocation(Robotics.MODID, "robot_construction"))
            .setEnergyRequirement(ENERGY_COST)
            .setProcessingTime(WORK_TIME)
            .build();

    private final EntityLevelStorage storedRobot;

    /** Whether the contruction time is over */
    private boolean builtRobot;
    /** Tells the client whether it can press the start button */
    private boolean canStart = false;

    public FactoryBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.ROBOT_FACTORY, pos, state, 6 + Reference.MAX_MODULES, new int[] {}, new int[] {});
        storedRobot = new EntityLevelStorage(level, null, this::getBlockPos);
        inventory = new FactoryInventory(this, getContainerSize());
        inventory.setAllSlotsAccessibleByDefault();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new FactoryMenu(id, inv, this, this.dataAccess);
    }

    @Override
    public void setLevel(Level p_155231_) {
        super.setLevel(p_155231_);
        storedRobot.setLevel(level);
        if(storedRobot.getEntity().isEmpty()) {
            RobotEntity robot = new RobotEntity(level);
            robot.getCapability(ModCapabilities.PARTS).ifPresent(IPartBuilt::clear);
            storedRobot.setEntity(robot);
        }
    }

    ////////////////////
    // Altering default machine behavior
    ////////////////////


    @Override
    public void startMachine(int from) {
        if(from == 1) return; //This machine does not start automatically when provided with all inputs
        super.startMachine(from);
    }

    @Override
    @Nullable
    protected MachineRecipe getRecipe(ItemStack[] inputStacks) {
        return canStart() || (isRunning() && !hasCraftedRobotReady()) ? DEFAULT_RECIPE : null;
    }

    @Override
    protected void craftItem(MachineRecipe recipe) {
        builtRobot = true;
    }

    @Override
    protected void consumeInputs(MachineRecipe recipe) {
        //NO-OP: Items are consumed when constructing the robot
    }

    /**
     * If this should change, update the according description in com.ignis.robotics.client.container.GuiRobotFactory
     * @return whether the machine could start right now
     */
    public boolean canStart() {
        if(level == null) return false;
        if(level.isClientSide()) return canStart;
        return
                storedRobot != null &&
                        !hasCraftedRobotReady() &&
                        !isRunning() &&
                        this.storage.getEnergyStored() >= ENERGY_COST &&
                        !inventory.getStackInSlot(0).isEmpty() &&
                        !inventory.getStackInSlot(1).isEmpty() &&
                        !(inventory.getStackInSlot(4).isEmpty() && inventory.getStackInSlot(5).isEmpty());
    }

    ////////////////////
    // Robot Logic
    ////////////////////

    public void setRobotPart(EnumRobotPart part, EnumRobotMaterial material) {
        storedRobot.setRobotPart(part, material);
    }

    public void setRobot(RobotEntity robot) {
        storedRobot.setEntity(robot);
    }

    public Optional<Entity> createNewRobot(UUID owner) {
        Optional<Entity> robot = storedRobot.createNewRobot(owner);
        builtRobot = false;
        this.inventory.clear();
        return robot;
    }

    ////////////////////
    // Saving & Syncing
    ////////////////////

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("canStart", canStart());
        return tag;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        canStart = nbt.getBoolean("canStart");
        builtRobot = nbt.getBoolean("builtRobot");
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putBoolean("canStart", canStart());
        compound.putBoolean("builtRobot", builtRobot);
    }

    ////////////////////
    // Getter & Setter
    ////////////////////


    public Optional<Entity> getEntity() {
        return storedRobot.getEntity();
    }

    public boolean hasCraftedRobotReady() {
        return builtRobot;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.robot_factory");
    }

    @Override
    protected void onItemCrafted() {}

    @Override
    protected void onMachineStart() {}
}
