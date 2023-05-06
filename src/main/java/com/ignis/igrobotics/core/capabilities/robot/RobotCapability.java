package com.ignis.igrobotics.core.capabilities.robot;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.access.AccessConfig;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMapCap;
import com.ignis.igrobotics.core.robot.RobotModule;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.UUID;

public class RobotCapability implements IRobot {

    protected LivingEntity entity;
    protected SynchedEntityData dataManager;
    protected IPerkMapCap perkMap;
    private AccessConfig access = new AccessConfig();

    private NonNullList<ItemStack> modules;
    private UUID owner = Reference.DEFAULT_UUID;

    private static final EntityDataAccessor<Integer> RENDER_OVERLAYS = RobotEntity.RENDER_OVERLAYS;
    private static final EntityDataAccessor<Boolean> ACTIVATED = RobotEntity.ACTIVATED;
    private static final EntityDataAccessor<Boolean> MUTED = RobotEntity.MUTED;
    private static final EntityDataAccessor<Integer> LOAD_CHUNK = RobotEntity.LOAD_CHUNK;
    private static final EntityDataAccessor<Integer> PICKUP_STATE = RobotEntity.PICKUPSTATE;
    private static final EntityDataAccessor<Integer> COMMAND_GROUP = RobotEntity.COMMAND_GROUP;

    public RobotCapability(LivingEntity entity) {
        this.entity = entity;
        this.dataManager = entity.getEntityData();
        this.perkMap = entity.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS);
        modules = NonNullList.withSize(RoboticsConfig.general.moduleAmount.get(), ItemStack.EMPTY);

        dataManager.define(RENDER_OVERLAYS, 0);
        dataManager.define(ACTIVATED, true);
        dataManager.define(MUTED, false);
        dataManager.define(LOAD_CHUNK, 0);
        dataManager.define(PICKUP_STATE, 0);
        dataManager.define(COMMAND_GROUP, 0);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ItemStackUtils.saveAllItems(nbt, modules, "modules");
        nbt.putBoolean("active", isActive());
        nbt.putInt("overlays", dataManager.get(RENDER_OVERLAYS));
        nbt.putBoolean("muted", isMuffled());
        nbt.putInt("load_chunks", getChunkLoadingState());
        nbt.putInt("pickup_state", getPickUpState());
        nbt.putInt("command_group", getCommandGroup());
        nbt.putUUID("owner", getOwner());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        modules = NonNullList.withSize(RoboticsConfig.general.moduleAmount.get(), ItemStack.EMPTY);
        ItemStackUtils.loadAllItems(nbt, modules, "modules");
        setModules(modules);
        setActivation(nbt.getBoolean("active"));
        dataManager.set(RENDER_OVERLAYS, nbt.getInt("overlays"));
        setMuffled(nbt.getBoolean("muted"));
        setChunkLoading(nbt.getInt("load_chunks"));
        setPickUpState(nbt.getInt("pickup_state"));
        setCommandGroup(nbt.getInt("command_group"));
        setOwner(nbt.getUUID("owner"));
    }

    private void applyPickupTask() {
        //TODO
    }

    @Override
    public boolean isActive() {
        return dataManager.get(ACTIVATED);
    }

    @Override
    public void setActivation(boolean activation) {
        IEnergyStorage storage = entity.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if(storage != null && storage.getEnergyStored() <= 0) {
            activation = false;
        }
        if(activation == isActive()) return;

        dataManager.set(ACTIVATED, activation);

        if(!activation && RoboticsConfig.general.chunkLoadShutdown.get()) {
            setChunkLoading(0);
        }

        // NOTE Maybe make this fire as an event?
        /* TODO Command Capability:
        if(activation) {
                robot.commands.removeAllTasks();
                entity.initEntityAI();
            } else {
                robot.commands.removeAllTasks();
                entity.tasks.addTask(0, TASK_LOOK_DOWN);
         */
        IPartBuilt parts = entity.getCapability(ModCapabilities.PARTS).orElse(null);
        if(parts == null) return;
        if(activation) {
            parts.setTemporaryColor(parts.getColor());
        } else {
            parts.setTemporaryColor(DyeColor.GRAY);
        }
    }

    @Override
    public boolean hasModule(RobotModule module) {
        for(ItemStack stack : modules) {
            if(module.getItems().test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getModules() {
        return modules;
    }

    @Override
    public void setModules(List<ItemStack> items) {
        for(int i = 0; i < Math.min(items.size(), modules.size()); i++) {
            setModule(items.get(i), i);
        }
        entity.getCapability(ModCapabilities.PERKS).ifPresent(IPerkMapCap::updateAttributeModifiers);
    }

    private void setModule(ItemStack item, int slot) {
        //Remove the modifiers and texture of the old module
        if(!modules.get(slot).isEmpty()) {
            RobotModule oldModule = RoboticsConfig.current().modules.get(modules.get(slot));
            perkMap.diff(oldModule.getPerks());
            if(RoboticsConfig.current().modules.overlays.contains(oldModule)) {
                int overlayId = RoboticsConfig.current().modules.overlays.indexOf(oldModule);
                removeRenderLayer(overlayId);
            }
        }
        modules.set(slot, item);
        //Add modifiers and texture of the new module
        if(!item.isEmpty()) {
            RobotModule module = RoboticsConfig.current().modules.get(item);
            perkMap.merge(module.getPerks());
            if(RoboticsConfig.current().modules.overlays.contains(module)) {
                int overlayId = RoboticsConfig.current().modules.overlays.indexOf(module);
                addRenderLayer(overlayId);
            }
        }
    }

    @Override
    public void setMaxModules(int amount) {
        NonNullList<ItemStack> newModules = NonNullList.<ItemStack>withSize(amount, ItemStack.EMPTY);
        //Copy old modules over to the new list
        for(int i = 0; i < Math.min(modules.size(), amount); i++) {
            newModules.set(i, modules.get(i));
        }
        //If some old modules didn't fit, remove their perks and drop them
        for(int i = amount; i < modules.size(); i++) {
            if(modules.get(i).isEmpty()) continue;
            perkMap.diff(RoboticsConfig.current().modules.get(modules.get(i)).getPerks());
            ItemStackUtils.dropItem(entity.level, entity.xOld, entity.yOld, entity.zOld, modules.get(i));
        }
        modules = newModules;

        perkMap.updateAttributeModifiers();
    }

    @Override
    public void setChunkLoading(int state) {
        entity.getCapability(ModCapabilities.CHUNK_LOADER).ifPresent(loader -> {
            loader.unloadChunks(new ChunkPos(entity.blockPosition()));
        });
        dataManager.set(LOAD_CHUNK, state);
        entity.getCapability(ModCapabilities.CHUNK_LOADER).ifPresent(loader -> {
            loader.loadChunks(new ChunkPos(entity.blockPosition()));
        });
    }

    @Override
    public int getChunkLoadingState() {
        return dataManager.get(LOAD_CHUNK);
    }

    @Override
    public void setCommandGroup(int group) {
        dataManager.set(COMMAND_GROUP, group);
    }

    @Override
    public int getCommandGroup() {
        return dataManager.get(COMMAND_GROUP);
    }

    @Override
    public void setPickUpState(int state) {
        dataManager.set(PICKUP_STATE, state);
        applyPickupTask();
    }

    @Override
    public int getPickUpState() {
        return dataManager.get(PICKUP_STATE);
    }

    @Override
    public void setMuffled(boolean muffled) {
        dataManager.set(MUTED, muffled);
    }

    @Override
    public boolean isMuffled() {
        return dataManager.get(MUTED);
    }

    public void addRenderLayer(int id) {
        if(id >= Integer.BYTES || id < 0) return;
        int currentOverlays = dataManager.get(RENDER_OVERLAYS);
        this.dataManager.set(RENDER_OVERLAYS, currentOverlays | (1 << id));
    }

    public void removeRenderLayer(int id) {
        if(id >= Integer.BYTES || id < 0) return;
        int currentOverlays = dataManager.get(RENDER_OVERLAYS);
        this.dataManager.set(RENDER_OVERLAYS, currentOverlays & ~(1 << id));
    }

    @Override
    public boolean hasRenderLayer(int id) {
        if(id >= Integer.BYTES || id < 0) return false;
        return ((dataManager.get(RENDER_OVERLAYS) >> id) & 1) == 1;
    }

    @Override
    public void setOwner(UUID newOwner) {
        owner = newOwner;
    }

    @Override
    public @NonNull UUID getOwner() {
        return owner;
    }

    @Override
    public void setAccess(AccessConfig access) {
        this.access = access;
    }

    @Override
    public AccessConfig getAccess() {
        return access; //TODO: Culminate across scopes
    }
}
