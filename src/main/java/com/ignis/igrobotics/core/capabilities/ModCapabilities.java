package com.ignis.igrobotics.core.capabilities;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.INBTSerializer;
import com.ignis.igrobotics.core.capabilities.chunkloading.IChunkLoader;
import com.ignis.igrobotics.core.capabilities.commands.ICommandable;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.core.capabilities.parts.PartsCapability;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMapCap;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.capabilities.robot.RobotCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = Robotics.MODID)
public class ModCapabilities {

    public static final Capability<IRobot> ROBOT = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IPerkMapCap> PERKS = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IPartBuilt> PARTS = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<ICommandable> COMMANDS = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IChunkLoader> CHUNK_LOADER = CapabilityManager.get(new CapabilityToken<>(){});

    public static final ResourceLocation LOC_ROBOT = new ResourceLocation(Robotics.MODID, "robot");
    public static final ResourceLocation LOC_PERKS = new ResourceLocation(Robotics.MODID, "perks");
    public static final ResourceLocation LOC_PARTS = new ResourceLocation(Robotics.MODID, "parts");
    public static final ResourceLocation LOC_COMMANDS = new ResourceLocation(Robotics.MODID, "commands");
    public static final ResourceLocation LOC_LOADER = new ResourceLocation(Robotics.MODID, "chunk_loader");

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof RobotEntity robot)) return;

        PartsCapability partsCapability = new PartsCapability(robot);
        event.addCapability(LOC_PARTS, new AlwaysProvideAndSave<>(PARTS, partsCapability));

        RobotCapability robotCapability = new RobotCapability(robot);
        event.addCapability(LOC_ROBOT, new AlwaysProvideAndSave<>(ROBOT, robotCapability));
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

class AlwaysProvideAndSave<C extends INBTSerializer> implements ICapabilitySerializable<CompoundTag> {

    private final Capability<C> cap;
    private final LazyOptional<C> optional;

    public AlwaysProvideAndSave(Capability<C> cap, @NonNull C impl) {
        this.cap = cap;
        this.optional = LazyOptional.of(() -> impl);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        optional.ifPresent((impl) -> impl.writeToNBT(nbt));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        optional.ifPresent((impl) -> impl.readFromNBT(nbt));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(this.cap.equals(cap)) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
}