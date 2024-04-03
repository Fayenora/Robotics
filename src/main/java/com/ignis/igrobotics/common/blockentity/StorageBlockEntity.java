package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.client.menu.StorageMenu;
import com.ignis.igrobotics.common.WorldData;
import com.ignis.igrobotics.common.blocks.MachineBlock;
import com.ignis.igrobotics.core.Machine;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import com.ignis.igrobotics.definitions.ModMachines;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MACHINE_TO_ROBOT_ENERGY_TRANSFER = 1000;
    private static final Machine<?> MACHINE = ModMachines.ROBOT_STORAGE;

    private final EntityLevelStorage storedRobot;
    private final EnergyStorage energy;

    protected ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int id) {
            switch (id) {
                case 0: return energy.getEnergyStored();
                case 1: return energy.getMaxEnergyStored();
                case 2:
                        if(storedRobot.getEntity().isEmpty()) return 0;
                        return storedRobot.getEntity().get().getCapability(ForgeCapabilities.ENERGY).orElse(ModCapabilities.NO_ENERGY).getEnergyStored();
                case 3:
                        if(storedRobot.getEntity().isEmpty()) return 0;
                        return storedRobot.getEntity().get().getCapability(ForgeCapabilities.ENERGY).orElse(ModCapabilities.NO_ENERGY).getMaxEnergyStored();
                default: return 0;
            }
        }

        @Override
        public void set(int id, int value) {
            switch (id) {
                case 0 -> energy.setEnergy(value);
                case 1 -> energy.setMaxEnergyStored(value);
                case 2 ->
                    storedRobot.getEntity().ifPresent(ent -> ent.getCapability(ForgeCapabilities.ENERGY).ifPresent(robotEnergy -> {
                        if (robotEnergy instanceof EnergyStorage energyStorage) {
                            energyStorage.setEnergy(value);
                        }
                    }));
                case 3 ->
                    storedRobot.getEntity().ifPresent(ent -> ent.getCapability(ForgeCapabilities.ENERGY).ifPresent(robotEnergy -> {
                        if (robotEnergy instanceof EnergyStorage energyStorage) {
                            energyStorage.setMaxEnergyStored(value);
                        }
                    }));
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public StorageBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(MACHINE.getBlockEntityType(), pPos, pBlockState);
        storedRobot = new EntityLevelStorage(level, null, this::getBlockPos);
        energy = new EnergyStorage(MACHINE.getEnergyCapacity(), MACHINE.getEnergyTransfer());
        energy_cap = LazyOptional.of(() -> energy);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StorageBlockEntity storage) {
        //Transfer energy from the block to the robot
        storage.storedRobot.getEntity().ifPresent(ent -> ent.getCapability(ForgeCapabilities.ENERGY).ifPresent((robotEnergy) -> {
            int to_transfer = Math.min(robotEnergy.receiveEnergy(MACHINE_TO_ROBOT_ENERGY_TRANSFER, true), storage.energy.extractEnergy(MACHINE_TO_ROBOT_ENERGY_TRANSFER, true));
            robotEnergy.receiveEnergy(to_transfer, false);
            storage.energy.extractEnergy(to_transfer, false);
        }));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.robot_storage");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player pPlayer) {
        return new StorageMenu(id, inv, this, this.dataAccess);
    }

    public void clearEntity() {
        storedRobot.getEntity().ifPresent(ent -> WorldData.get().releaseRobotFromCommandGroup(ent));
        storedRobot.clearEntity();
        sync();
    }

    public void enterStorage(Entity entity) {
        WorldData.get().rememberRobotStorage(getBlockPos(), entity);
        storedRobot.enterStorage(entity);
        sync();
    }

    public Optional<Entity> exitStorage() {
        return exitStorage(getBlockState().getValue(MachineBlock.FACING));
    }

    public Optional<Entity> exitStorage(@Nullable Direction direction) {
        Optional<Entity> exitedEntity = storedRobot.exitStorage(direction);
        sync();
        return exitedEntity;
    }

    public Optional<Entity> getEntity() {
        return storedRobot.getEntity();
    }

    @Override
    public void saveToItem(ItemStack p_187477_) {
        CompoundTag tag = saveWithoutMetadata();
        tag.getCompound("entity").remove("UUID");
        BlockItem.setBlockEntityData(p_187477_, this.getType(), tag);
    }

    /////////////////////
    // Capabilities
    /////////////////////

    LazyOptional<? extends IEnergyStorage> energy_cap;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) return energy_cap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy_cap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energy_cap = LazyOptional.of(() -> energy);
    }

    /////////////////////
    // Saving & Syncing
    /////////////////////

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        storedRobot.setLevel(level);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("energy", energy.serializeNBT());
        nbt.put("entity", storedRobot.serializeNBT());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        energy.deserializeNBT(nbt.getCompound("energy"));
        storedRobot.deserializeNBT(nbt.getCompound("entity"));
        super.load(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        nbt.put("energy", energy.serializeNBT());
        nbt.put("entity", storedRobot.serializeNBT());
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void sync() {
        if(level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        setChanged();
    }
}
