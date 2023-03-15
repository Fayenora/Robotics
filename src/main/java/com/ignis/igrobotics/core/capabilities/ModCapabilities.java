package com.ignis.igrobotics.core.capabilities;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.INBTSerializer;
import com.ignis.igrobotics.core.capabilities.parts.IPartBuilt;
import com.ignis.igrobotics.core.capabilities.parts.PartsCapability;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMapCap;
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

    public static final Capability<IPartBuilt> PART_BUILT_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IPerkMapCap> PERK_MAP_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static final ResourceLocation LOC_LOADER = new ResourceLocation(Robotics.MODID, "chunk_loader");
    public static final ResourceLocation LOC_PARTS = new ResourceLocation(Robotics.MODID, "parts");
    public static final ResourceLocation LOC_PERKS = new ResourceLocation(Robotics.MODID, "perks");

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof RobotEntity)) return;
        RobotEntity robot = (RobotEntity) event.getObject();

        PartsCapability partsCapability = new PartsCapability(robot);
        event.addCapability(LOC_PARTS, new AlwaysProvideAndSave<>(PART_BUILT_CAPABILITY, partsCapability));
    }
}

class AlwaysProvide<C> implements ICapabilityProvider {

    private Capability<C> cap;
    private LazyOptional<C> optional;

    public AlwaysProvide(Capability<C> cap, @NonNull C impl) {
        this.cap = cap;
        this.optional = LazyOptional.of(() -> impl);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(this.cap.equals(cap)) {
            return optional.cast();
        }
        return null;
    }
}

class AlwaysProvideAndSave<C extends INBTSerializer> implements ICapabilitySerializable<CompoundTag> {

    private Capability<C> cap;
    private LazyOptional<C> optional;

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
        return null;
    }
}