package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.ModMachines;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.FactoryMenu;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.EnumRobotMaterial;
import com.ignis.igrobotics.core.EnumRobotPart;
import com.ignis.igrobotics.core.MachineRecipe;
import com.ignis.igrobotics.core.capabilities.inventory.FactoryInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class BlockEntityFactory extends BlockEntityMachine {

    public static final int ENERGY_COST = 500000;
    public static final int WORK_TIME = 60;

    public static final MachineRecipe<BlockEntityFactory> DEFAULT_RECIPE = new MachineRecipe.Builder(ModMachines.ROBOT_FACTORY, new ResourceLocation(Robotics.MODID, "robot_construction"))
            .setEnergyRequirement(ENERGY_COST)
            .setProcessingTime(WORK_TIME)
            .build();

    private RobotLevelStorage storedRobot;

    /** Whether the contruction time is over */
    private boolean builtRobot;
    /** Tells the client whether it can press the start button */
    private boolean canStart = false;

    public BlockEntityFactory(BlockPos pos, BlockState state) {
        super(ModMachines.ROBOT_FACTORY, pos, state, 6 + Reference.MAX_MODULES, new int[] {}, new int[] {});
        inventory = new FactoryInventory(this, getContainerSize());
        inventory.setAllSlotsAccessibleByDefault();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new FactoryMenu(id, inv, this, this.dataAccess);
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
    protected boolean canStart() {
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

    public void clearRobot() {
        storedRobot.clearRobot();
    }

    public void setRobot(RobotEntity robot) {
        storedRobot.setRobot(robot);
    }

    public RobotEntity createNewRobot(UUID owner) {
        RobotEntity robot = storedRobot.createNewRobot(owner);
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        canStart = pkt.getTag().getBoolean("canStart");
        super.onDataPacket(net, pkt);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        builtRobot = nbt.getBoolean("builtRobot");
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putBoolean("builtRobot", builtRobot);
        return tag;
    }

    ////////////////////
    // Getter & Setter
    ////////////////////


    public RobotEntity getRobot() {
        return storedRobot.getRobot();
    }

    public boolean hasCraftedRobotReady() {
        return builtRobot;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.robot_factory");
    }

    @Override
    protected void onItemCrafted() {

    }

    @Override
    protected void onMachineStart() {

    }
}
