package com.io.norabotics.common.capabilities;

import com.io.norabotics.Reference;
import com.io.norabotics.Robotics;
import com.io.norabotics.common.capabilities.impl.*;
import com.io.norabotics.common.capabilities.impl.inventory.BaseInventory;
import com.io.norabotics.common.capabilities.impl.inventory.RobotInventory;
import com.io.norabotics.common.capabilities.impl.perk.AdvancedPerkMap;
import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.content.entity.RobotEntity;
import com.io.norabotics.common.handlers.ChunkLoadingHandler;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import com.io.norabotics.common.helpers.types.Tuple;
import com.io.norabotics.definitions.ModAttributes;
import com.io.norabotics.integration.cc.ComputerCapability;
import com.io.norabotics.integration.cc.IComputerized;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = Robotics.MODID)
public class ModCapabilities {

    public static final Capability<IRobot> ROBOT = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IPerkMap> PERKS = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IPartBuilt> PARTS = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<ICommandable> COMMANDS = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IChunkLoader> CHUNK_LOADER = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<ChunkLoadingHandler.ChunkTracker> CHUNK_TRACKER = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IComputerized> COMPUTERIZED = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IShielded> SHIELDED = CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation LOC_ROBOT = new ResourceLocation(Robotics.MODID, "robot");
    public static final ResourceLocation LOC_INVENTORY = new ResourceLocation(Robotics.MODID, "inventory");
    public static final ResourceLocation LOC_ENERGY = new ResourceLocation(Robotics.MODID, "energy");
    public static final ResourceLocation LOC_PERKS = new ResourceLocation(Robotics.MODID, "perks");
    public static final ResourceLocation LOC_PARTS = new ResourceLocation(Robotics.MODID, "parts");
    public static final ResourceLocation LOC_COMMANDS = new ResourceLocation(Robotics.MODID, "commands");
    public static final ResourceLocation LOC_LOADER = new ResourceLocation(Robotics.MODID, "chunk_loader");
    public static final ResourceLocation LOC_TRACKER = new ResourceLocation(Robotics.MODID, "chunk_tracker");
    public static final ResourceLocation LOC_COMPUTERIZED = new ResourceLocation(Robotics.MODID, "computerized");
    public static final ResourceLocation LOC_SHIELDED = new ResourceLocation(Robotics.MODID, "shielded");

    public static final BaseInventory EMPTY_INVENTORY = new BaseInventory(() -> BlockPos.ZERO, 0);
    public static final EnergyStorage NO_ENERGY = new EnergyStorage(0);
    public static final IPartBuilt NO_PARTS = new PartsCapability();
    public static final IPerkMap NO_PERKS = new IPerkMap() {
        @Override
        public SimpleDataManager values() {
            return new SimpleDataManager();
        }
        @Override
        public void add(Perk perk, int level) {}
        @Override
        public void remove(Perk perk, int level) {}
        @Override
        public void merge(IPerkMap other) {}
        @Override
        public void diff(IPerkMap toRemove) {}
        @Override
        public void clear() {}
        @Override
        public boolean contains(Perk perk) {
            return false;
        }

        @Override
        public int getLevel(Perk perk) {
            return 0;
        }

        @NotNull
        @Override
        public Iterator<Tuple<Perk, Integer>> iterator() {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }
                @Override
                public Tuple<Perk, Integer> next() {
                    return null;
                }
            };
        }
        @Override
        public Iterable<Tuple<ResourceLocation, Integer>> baseIterator() {
            return new Iterable<>() {
                @NotNull
                @Override
                public Iterator<Tuple<ResourceLocation, Integer>> iterator() {
                    return new Iterator<Tuple<ResourceLocation, Integer>>() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public Tuple<ResourceLocation, Integer> next() {
                            return null;
                        }
                    };
                }
            };
        }
    };

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof RobotEntity robot)) return;

        AdvancedPerkMap perksCapability = new AdvancedPerkMap();
        event.addCapability(LOC_PERKS, new AlwaysProvide<>(PERKS, perksCapability));

        ChunkLoadingCapability chunkLoadingCapability = new ChunkLoadingCapability(robot);
        event.addCapability(LOC_LOADER, new AlwaysProvide<>(CHUNK_LOADER, chunkLoadingCapability));

        event.addCapability(LOC_ENERGY, new AlwaysProvideAndSave<>(ForgeCapabilities.ENERGY, new EnergyStorage((int) ModAttributes.ENERGY_CAPACITY.getDefaultValue())));

        RobotInventory robotInventory = new RobotInventory(robot);
        event.addCapability(LOC_INVENTORY, new AlwaysProvideAndSave<>(ForgeCapabilities.ITEM_HANDLER, robotInventory));

        PartsCapability partsCapability = new PartsCapability(robot);
        event.addCapability(LOC_PARTS, new AlwaysProvideAndSave<>(PARTS, partsCapability));

        CommandCapability commandCapability = new CommandCapability(robot);
        event.addCapability(LOC_COMMANDS, new AlwaysProvideAndSave<>(COMMANDS, commandCapability));

        ShieldCapability shieldCapability = new ShieldCapability(robot);
        event.addCapability(LOC_SHIELDED, new AlwaysProvideAndSave<>(SHIELDED, shieldCapability));

        RobotCapability robotCapability = new RobotCapability(robot);
        event.addCapability(LOC_ROBOT, new AlwaysProvideAndSave<>(ROBOT, robotCapability));

        if(ModList.get().isLoaded(Reference.CC_MOD_ID)) {
            ComputerCapability computerCapability = new ComputerCapability(robot);
            event.addCapability(LOC_COMPUTERIZED, new AlwaysProvideAndSave<>(COMPUTERIZED, computerCapability));
        }
    }

    @SubscribeEvent
    public static void attachChunkLoadingCapability(AttachCapabilitiesEvent<Level> event) {
        if(!(event.getObject() instanceof ServerLevel serverLevel)) return;

        ChunkLoadingHandler.ChunkTracker chunkTracker = new ChunkLoadingHandler.ChunkTracker(serverLevel);
        event.addCapability(LOC_TRACKER, new AlwaysProvideAndSave<>(CHUNK_TRACKER, chunkTracker));
    }
}

class AlwaysProvide<C> implements ICapabilityProvider {

    private final Capability<C> cap;
    private final LazyOptional<C> optional;

    public AlwaysProvide(Capability<C> cap, @NonNull C impl) {
        this.cap = cap;
        this.optional = LazyOptional.of(() -> impl);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(this.cap.equals(cap)) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
}

class AlwaysProvideAndSave<C, TAG extends Tag, IMP extends INBTSerializable<TAG>> implements ICapabilitySerializable<TAG> {

    private final Capability<C> cap;
    private final LazyOptional<IMP> optional;

    public AlwaysProvideAndSave(Capability<C> cap, @NonNull IMP impl) {
        this.cap = cap;
        this.optional = LazyOptional.of(() -> impl);
    }

    @Override
    public TAG serializeNBT() {
        return optional.map(INBTSerializable::serializeNBT).orElseThrow();
    }

    @Override
    public void deserializeNBT(TAG nbt) {
        optional.ifPresent((impl) -> impl.deserializeNBT(nbt));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(this.cap.equals(cap)) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
}

class ConditionalProvideAndSave<C, IMP extends INBTSerializable<CompoundTag>, IMP2 extends INBTSerializable<CompoundTag>> implements ICapabilitySerializable<CompoundTag> {

    private final Capability<C> cap;
    private final Entity entity;
    private final LazyOptional<IMP> optional1;
    private final LazyOptional<IMP2> optional2;
    private final Predicate<Entity> condition;

    public ConditionalProvideAndSave(Capability<C> capability, Entity entity, Predicate<Entity> condition, @NonNull IMP impl, @NonNull IMP2 impl2) {
        this.cap = capability;
        this.entity = entity;
        this.optional1 = LazyOptional.of(() -> impl);
        this.optional2 = LazyOptional.of(() -> impl2);
        this.condition = condition;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("impl1", optional1.map(INBTSerializable::serializeNBT).orElseThrow());
        nbt.put("impl2", optional2.map(INBTSerializable::serializeNBT).orElseThrow());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        optional1.ifPresent((impl) -> impl.deserializeNBT(nbt.getCompound("impl1")));
        optional2.ifPresent((impl) -> impl.deserializeNBT(nbt.getCompound("impl2")));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(this.cap.equals(cap)) {
            return condition.test(entity) ? optional1.cast() : optional2.cast();
        }
        return LazyOptional.empty();
    }
}