package com.ignis.igrobotics.core.capabilities;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.common.handlers.ChunkLoadingHandler;
import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.capabilities.chunkloading.ChunkLoadingCapability;
import com.ignis.igrobotics.core.capabilities.chunkloading.IChunkLoader;
import com.ignis.igrobotics.core.capabilities.commands.CommandCapability;
import com.ignis.igrobotics.core.capabilities.commands.ICommandable;
import com.ignis.igrobotics.core.capabilities.energy.EnergyStorage;
import com.ignis.igrobotics.core.capabilities.energy.RobotEnergyStorage;
import com.ignis.igrobotics.core.capabilities.inventory.BaseInventory;
import com.ignis.igrobotics.core.capabilities.inventory.RobotInventory;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.core.capabilities.parts.PartsCapability;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMapCap;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.capabilities.perks.PerkMapCapability;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.capabilities.robot.RobotCapability;
import com.ignis.igrobotics.core.capabilities.shield.IShielded;
import com.ignis.igrobotics.core.capabilities.shield.ShieldCapability;
import com.ignis.igrobotics.core.util.Tuple;
import com.ignis.igrobotics.integration.cc.ComputerCapability;
import com.ignis.igrobotics.integration.cc.IComputerized;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

@Mod.EventBusSubscriber(modid = Robotics.MODID)
public class ModCapabilities {

    public static final Capability<IRobot> ROBOT = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IPerkMapCap> PERKS = CapabilityManager.get(new CapabilityToken<>(){});
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
    public static final IPerkMapCap NO_PERKS = new IPerkMapCap() {
        @Override
        public void updateAttributeModifiers() {}
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
    };

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof RobotEntity robot)) return;

        PerkMapCapability perksCapability = new PerkMapCapability(robot);
        event.addCapability(LOC_PERKS, new AlwaysProvide<>(PERKS, perksCapability));

        ChunkLoadingCapability chunkLoadingCapability = new ChunkLoadingCapability(robot);
        event.addCapability(LOC_LOADER, new AlwaysProvide<>(CHUNK_LOADER, chunkLoadingCapability));

        event.addCapability(LOC_ENERGY, new AlwaysProvideAndSave<>(ForgeCapabilities.ENERGY, new RobotEnergyStorage()));

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