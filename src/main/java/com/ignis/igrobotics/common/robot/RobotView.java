package com.ignis.igrobotics.common.robot;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.WorldData;
import com.ignis.igrobotics.common.capabilities.ModCapabilities;
import com.ignis.igrobotics.common.content.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.content.entity.RobotEntity;
import com.ignis.igrobotics.common.helpers.util.NBTUtil;
import com.ignis.igrobotics.common.helpers.util.PosUtil;
import com.ignis.igrobotics.network.messages.EntityByteBufUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class RobotView implements INBTSerializable<CompoundTag> {

    private UUID uuid;
    private String name;
    private DyeColor color;
    private RobotPart[] parts;
    private boolean active;
    private GlobalPos lastKnownPosition;
    private RobotState state = RobotState.IN_WORLD;

    private Entity cache;

    private RobotView() {}

    public RobotView(Entity entity) {
        this.uuid = entity.getUUID();
        this.name = entity.getDisplayName().getString();
        entity.getCapability(ModCapabilities.PARTS).ifPresent(partCap -> {
            this.color = partCap.getColor();
            this.parts = partCap.getBodyParts();
        });
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> active = robot.isActive());
        this.lastKnownPosition = GlobalPos.of(entity.level().dimension(), entity.blockPosition());
    }

    public Entity getEntity() {
        if(cache != null) return cache;
        if(ServerLifecycleHooks.getCurrentServer() != null) { //Only do searches on server side
            ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(lastKnownPosition.dimension());
            //CASE 1: In Storage
            if(state == RobotState.IN_STORAGE && level != null) {
                BlockEntity be = level.getBlockEntity(lastKnownPosition.pos());
                if(be instanceof StorageBlockEntity storage && storage.getEntity().isPresent()) {
                    return storage.getEntity().get();
                }
            }
            //CASE 2: In World
            if(level != null) {
                Entity ent = level.getEntity(uuid);
                if(ent != null) {
                    updatePosition(RobotState.IN_WORLD, ent.level(), ent.blockPosition());
                    return ent;
                }
            }
            //Search Everywhere
            for(ServerLevel dimension : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
                Entity ent = dimension.getEntity(uuid);
                if(ent != null) {
                    updatePosition(RobotState.IN_WORLD, ent.level(), ent.blockPosition());
                    return ent;
                }
            }
        }
        if(state == RobotState.IN_WORLD) {
            state = RobotState.OFFLINE;
        }
        //Case 3: Nowhere to be found
        RobotEntity robot = new RobotEntity(Robotics.proxy.getLevel());
        robot.setUUID(uuid);
        robot.setCustomName(Component.literal(name));
        robot.getCapability(ModCapabilities.PARTS).ifPresent(partCaps -> {
            partCaps.setColor(color);
            for (RobotPart part : parts) {
                partCaps.setBodyPart(part);
            }
        });
        robot.getCapability(ModCapabilities.ROBOT).ifPresent(robotics -> robotics.setActivation(active));
        return robot;
    }

    public void updatePosition(RobotState state, Level level, BlockPos storagePos) {
        lastKnownPosition = GlobalPos.of(level.dimension(), storagePos);
        this.state = state;
        WorldData.get().setDirty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", uuid);
        tag.putString("name", name);
        tag.putInt("color", color.getId());
        tag.put("parts", NBTUtil.serializeParts(parts));
        tag.putBoolean("active", active);
        tag.putByte("state", (byte) state.ordinal());
        tag.put("pos", PosUtil.writePos(lastKnownPosition));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        uuid = nbt.getUUID("uuid");
        name = nbt.getString("name");
        color = DyeColor.byId(nbt.getInt("color"));
        parts = NBTUtil.deserializeParts(nbt.get("parts"));
        active = nbt.getBoolean("active");
        state = RobotState.values()[nbt.getByte("state")];
        lastKnownPosition = PosUtil.readPos(nbt.getCompound("pos"));
    }

    public void write(FriendlyByteBuf buf) {
        Entity ent = getEntity();
        buf.writeEnum(state);
        buf.writeGlobalPos(lastKnownPosition);
        buf.writeBoolean(active);
        EntityByteBufUtil.writeEntity(ent, buf);
    }

    public void read(FriendlyByteBuf buf) {
        state = buf.readEnum(RobotState.class);
        lastKnownPosition = buf.readGlobalPos();
        active = buf.readBoolean();
        cache = EntityByteBufUtil.readEntity(buf);
    }

    public static void writeViews(FriendlyByteBuf buf, Collection<RobotView> views) {
        buf.writeInt(views.size());
        for(RobotView view : views) {
            view.write(buf);
        }
    }

    public static Collection<RobotView> readViews(FriendlyByteBuf buf) {
        int size = buf.readInt();
        ArrayList<RobotView> views = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            RobotView view = new RobotView();
            view.read(buf);
            views.add(view);
        }
        return views;
    }

    public static RobotView deserialize(CompoundTag nbt) {
        RobotView info = new RobotView();
        info.deserializeNBT(nbt);
        return info;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof RobotView info)) return false;
        return this.uuid.equals(info.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public UUID getUUID() {
        return uuid != null ? uuid : cache.getUUID();
    }

    public GlobalPos getLastKnownPosition() {
        return lastKnownPosition;
    }

    public RobotState getState() {
        return state;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean activation) {
        this.active = activation;
    }

    public void setState(RobotState state) {
        this.state = state;
    }

    public enum RobotState {
        IN_WORLD,
        IN_STORAGE,
        OFFLINE
    }
}
