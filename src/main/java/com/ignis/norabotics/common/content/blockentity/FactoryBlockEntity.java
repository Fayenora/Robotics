package com.ignis.norabotics.common.content.blockentity;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.IPartBuilt;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.capabilities.impl.inventory.FactoryInventory;
import com.ignis.norabotics.common.content.entity.RobotEntity;
import com.ignis.norabotics.common.content.menu.FactoryMenu;
import com.ignis.norabotics.common.content.recipes.MachineRecipe;
import com.ignis.norabotics.common.robot.EnumModuleSlot;
import com.ignis.norabotics.definitions.ModMachines;
import com.ignis.norabotics.integration.config.RoboticsConfig;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FactoryBlockEntity extends MachineBlockEntity {

    public static final MachineRecipe<?> DEFAULT_RECIPE = new MachineRecipe.Builder(ModMachines.ROBOT_FACTORY, new ResourceLocation(Robotics.MODID, "robot_construction"))
            .setEnergyRequirement(RoboticsConfig.general.constructionEnergyCost.get())
            .setProcessingTime(RoboticsConfig.general.constructionTime.get())
            .build();

    private final EntityLevelStorage storedRobot;

    /** Whether the contruction time is over */
    private boolean builtRobot;
    /** Tells the client whether it can press the start button */
    private boolean canStart = false;

    private BlockPos weldingArmPositive, weldingArmNegative;

    public FactoryBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.ROBOT_FACTORY, pos, state, 6 + Reference.MAX_MODULES * EnumModuleSlot.nonPrimaries().length, new int[] {}, new int[] {});
        storedRobot = new EntityLevelStorage(level, null, this::getBlockPos);
        inventory = new FactoryInventory(this, getContainerSize());
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new FactoryMenu(id, inv, this);
    }

    @Override
    public void setLevel(Level p_155231_) {
        super.setLevel(p_155231_);
        storedRobot.setLevel(level);
        setDefaultRobot();
    }

    private void setDefaultRobot() {
        if(storedRobot.getEntity().isEmpty() && level != null) {
            RobotEntity robot = new RobotEntity(level);
            robot.getCapability(ModCapabilities.PARTS).ifPresent(IPartBuilt::clear);
            storedRobot.setEntity(robot);
        }
    }

    @Nullable
    public Direction.AxisDirection assignWeldingArm(BlockPos pos) {
        if(pos.equals(weldingArmPositive)) return Direction.AxisDirection.POSITIVE;
        if(pos.equals(weldingArmNegative)) return Direction.AxisDirection.NEGATIVE;
        Direction.Axis orthogonal = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise().getAxis();
        BlockPos relative = pos.subtract(getBlockPos());
        if(orthogonal.choose(relative.getX(), relative.getY(), relative.getZ()) < 0) {
            if(weldingArmNegative == null || !(level.getBlockEntity(weldingArmNegative) instanceof MachineArmBlockEntity)) {
                weldingArmNegative = pos;
                return Direction.AxisDirection.NEGATIVE;
            }
        } else if(weldingArmPositive == null || !(level.getBlockEntity(weldingArmPositive) instanceof MachineArmBlockEntity)) {
            weldingArmPositive = pos;
            return Direction.AxisDirection.POSITIVE;
        }
        return null;
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
    protected MachineRecipe<?> getRecipe(ItemStack[] inputStacks) {
        return canStart() || (isRunning() && !hasCraftedRobotReady()) ? DEFAULT_RECIPE : null;
    }

    @Override
    protected void craftItem(MachineRecipe<?> recipe) {
        builtRobot = true;
    }

    @Override
    protected void consumeInputs(MachineRecipe<?> recipe) {
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
                        this.storage.getEnergyStored() >= DEFAULT_RECIPE.getEnergy() &&
                        !inventory.getStackInSlot(0).isEmpty() &&
                        !inventory.getStackInSlot(1).isEmpty() &&
                        !(inventory.getStackInSlot(4).isEmpty() && inventory.getStackInSlot(5).isEmpty());
    }

    ////////////////////
    // Robot Logic
    ////////////////////

    public void setRobotParts(EnumModuleSlot slotType, NonNullList<ItemStack> components) {
        storedRobot.setRobotParts(slotType, components);
    }

    public void setRobot(RobotEntity robot) {
        storedRobot.setEntity(robot);
    }

    public Optional<Entity> createNewRobot(UUID owner) {
        Optional<Entity> robot = storedRobot.createNewRobot(owner);
        builtRobot = false;
        this.inventory.clear();
        setDefaultRobot();
        sync();
        return robot;
    }

    ////////////////////
    // Saving & Syncing
    ////////////////////

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("canStart", canStart());
        tag.putBoolean("builtRobot", builtRobot);
        tag.put("inventory", inventory.serializeNBT());
        return tag;
    }

    @Override
    public @Nullable SoundEvent getRunningSound() {
        return null;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        canStart = tag.getBoolean("canStart");
        builtRobot = tag.getBoolean("builtRobot");
        inventory.deserializeNBT(tag.getCompound("inventory"));
        requestModelDataUpdate();
    }

    @Override
    public void requestModelDataUpdate() {
        if(isRemoved()) return;
        if(inventory instanceof FactoryInventory inv) {
            inv.deriveEntity();
        }
        super.requestModelDataUpdate();
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

    public boolean isRunningOrFinished() {
        return isRunning() || hasCraftedRobotReady();
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
