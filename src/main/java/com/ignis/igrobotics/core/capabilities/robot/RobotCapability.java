package com.ignis.igrobotics.core.capabilities.robot;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.WorldData;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.common.entity.ai.LookDownGoal;
import com.ignis.igrobotics.common.entity.ai.PickupGoal;
import com.ignis.igrobotics.core.access.AccessConfig;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.commands.CommandCapability;
import com.ignis.igrobotics.core.capabilities.commands.ICommandable;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMapCap;
import com.ignis.igrobotics.core.robot.EnumModuleSlot;
import com.ignis.igrobotics.core.robot.RobotModule;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class RobotCapability implements IRobot {

    protected LivingEntity entity;
    protected SynchedEntityData dataManager;
    private AccessConfig access = new AccessConfig();

    private NonNullList<ItemStack> modules;
    /** Amount of slots for each module type. These are not enforced and just info for the module menu*/
    private final Map<EnumModuleSlot, Integer> slotSizes;

    private static final EntityDataAccessor<Integer> RENDER_OVERLAYS = RobotEntity.RENDER_OVERLAYS;
    private static final EntityDataAccessor<Boolean> ACTIVATED = RobotEntity.ACTIVATED;
    private static final EntityDataAccessor<Boolean> MUTED = RobotEntity.MUTED;
    private static final EntityDataAccessor<Integer> LOAD_CHUNK = RobotEntity.LOAD_CHUNK;
    private static final EntityDataAccessor<Integer> PICKUP_STATE = RobotEntity.PICKUP_STATE;
    private static final EntityDataAccessor<Integer> COMMAND_GROUP = RobotEntity.COMMAND_GROUP;

    public PickupGoal pickUpGoal;

    public RobotCapability(Mob entity) {
        this.entity = entity;
        this.dataManager = entity.getEntityData();
        modules = NonNullList.withSize(EnumModuleSlot.values().length * Reference.MAX_MODULES, ItemStack.EMPTY);
        slotSizes = new HashMap<>(6);
        for(EnumModuleSlot slotType : EnumModuleSlot.values()) {
            slotSizes.put(slotType, RoboticsConfig.general.moduleAmount[slotType.ordinal()].get());
        }

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
        modules = NonNullList.withSize(EnumModuleSlot.values().length * Reference.MAX_MODULES, ItemStack.EMPTY);
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

    @Override
    public boolean isActive() {
        return dataManager.get(ACTIVATED);
    }

    @Override
    public void setActivation(boolean activation) {
        Optional<IEnergyStorage> storage = entity.getCapability(ForgeCapabilities.ENERGY).resolve();
        if(storage.isPresent() && storage.get().getEnergyStored() <= 0) {
            activation = false;
        }
        if(activation == isActive()) return;
        Optional<IPartBuilt> parts = entity.getCapability(ModCapabilities.PARTS).resolve();
        Optional<ICommandable> commands = entity.getCapability(ModCapabilities.COMMANDS).resolve();

        // NOTE Maybe make this fire as an event?
        dataManager.set(ACTIVATED, activation);

        if(!activation) {
            if(RoboticsConfig.general.pickUpShutdown.get() && entity instanceof Mob mob) {
                mob.setCanPickUpLoot(false);
            }
            if(RoboticsConfig.general.chunkLoadShutdown.get()) {
                setChunkLoading(0);
            }
        } else {
            setPickUpState(getPickUpState());//Reapply the rule the robot had when active (if there is such a rule saved)
        }

        if(commands.isPresent() && entity instanceof Mob mob) {
            if(activation) {
                mob.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
                mob.goalSelector.removeAllGoals(goal -> goal instanceof LookDownGoal);
                commands.get().reapplyAllTasks();
            } else {
                commands.get().removeAllTasks();
                mob.goalSelector.addGoal(0, new LookDownGoal(mob));
            }
        }

        if(parts.isPresent()) {
            if(activation) {
                parts.get().setTemporaryColor(parts.get().getColor());
            } else {
                parts.get().setTemporaryColor(DyeColor.GRAY);
            }
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
            entity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> perks.diff(oldModule.getPerks()));
            if(RoboticsConfig.current().modules.overlays.contains(oldModule)) {
                int overlayId = RoboticsConfig.current().modules.overlays.indexOf(oldModule);
                removeRenderLayer(overlayId);
            }
        }
        modules.set(slot, item);
        //Add modifiers and texture of the new module
        if(!item.isEmpty()) {
            RobotModule module = RoboticsConfig.current().modules.get(item);
            entity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> perks.merge(module.getPerks()));
            if(RoboticsConfig.current().modules.overlays.contains(module)) {
                int overlayId = RoboticsConfig.current().modules.overlays.indexOf(module);
                addRenderLayer(overlayId);
            }
        }
    }

    @Override
    public void setMaxModules(EnumModuleSlot slotType, int amount) {
        //TODO Rewrite
        if(amount == slotSizes.get(slotType)) return;
        //Remove any modules occupying these slots
        int startIndex = slotType.ordinal() * EnumModuleSlot.values().length;
        for(int i = startIndex + amount; i < startIndex + EnumModuleSlot.values().length; i++) {
            if(modules.get(i).isEmpty()) continue;
            IPerkMap oldPerks = RoboticsConfig.current().modules.get(modules.get(i)).getPerks();
            entity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> perks.diff(oldPerks));
            ItemStackUtils.dropItem(entity.level, entity.xOld, entity.yOld, entity.zOld, modules.get(i));
        }
        slotSizes.put(slotType, amount);

        entity.getCapability(ModCapabilities.PERKS).ifPresent(IPerkMapCap::updateAttributeModifiers);
    }

    @Override
    public Map<EnumModuleSlot, Integer> getModuleSlots() {
        return slotSizes;
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
        WorldData data = WorldData.get();
        if(group == 0) {
            data.releaseRobotFromCommandGroup(entity);
        } else {
            data.cacheRobotForCommandGroup(group, entity);
        }
    }

    @Override
    public int getCommandGroup() {
        return dataManager.get(COMMAND_GROUP);
    }

    @Override
    public void setPickUpState(int state) {
        dataManager.set(PICKUP_STATE, state);
        //We can't add any goals while the entity is still being loaded/initialised
        if(!(entity instanceof Mob mob)) return;
        if(pickUpGoal == null) {
            pickUpGoal = new PickupGoal(mob, 16);
        }
        mob.setCanPickUpLoot(state % 3 == 2);
        if(state % 3 == 1) {
            mob.goalSelector.addGoal(CommandCapability.MAX_NON_COMMAND_GOALS - 2, pickUpGoal);
        } else mob.goalSelector.removeGoal(pickUpGoal);
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
        access.setOwner(newOwner);
    }

    @Override
    public @NonNull UUID getOwner() {
        return access.getOwner();
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
