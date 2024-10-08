package com.io.norabotics.network.messages.server;

import com.io.norabotics.common.capabilities.IRobot;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.content.blockentity.FactoryBlockEntity;
import com.io.norabotics.common.content.blockentity.IMuffleable;
import com.io.norabotics.common.content.blockentity.StorageBlockEntity;
import com.io.norabotics.common.content.blocks.MachineBlock;
import com.io.norabotics.common.helpers.util.InventoryUtil;
import com.io.norabotics.common.robot.EnumModuleSlot;
import com.io.norabotics.common.robot.RobotPart;
import com.io.norabotics.network.messages.IMessage;
import com.io.norabotics.network.messages.NetworkInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkEvent;

public class PacketComponentAction implements IMessage {

    public static final byte ACTION_PICKUP_STATE = 1;
    public static final byte ACTION_POWER_STATE = 2;
    public static final byte ACTION_MUTE_STATE = 3;
    public static final byte ACTION_CHUNK_LOADING_STATE = 4;
    public static final byte ACTION_FACTORY_BUTTON = 5;
    public static final byte ACTION_COLOR_LEFT = 6;
    public static final byte ACTION_COLOR_RIGHT = 7;
    public static final byte ACTION_DISMANTLE_ROBOT = 8;

    @Override
    public void handle(NetworkEvent.Context cxt) {
        Player player = cxt.getSender();
        Level level = player.level();
        Entity entity = data.getAsEntity(level);
        BlockEntity blockEntity = data.getAsBlockEntity(level);
        switch (action) {
            case ACTION_PICKUP_STATE -> {
                if (entity == null) return;
                entity.getCapability(ModCapabilities.ROBOT).ifPresent(IRobot::nextPickUpState);
            }
            case ACTION_POWER_STATE -> {
                if (entity == null) return;
                entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> robot.setActivation(!robot.isActive()));
            }
            case ACTION_MUTE_STATE -> {
                if (entity != null) {
                    entity.getCapability(ModCapabilities.ROBOT).ifPresent(IRobot::nextMuteState);
                }
                if(blockEntity instanceof IMuffleable muffleable) {
                    muffleable.nextMuffleState();
                }
            }
            case ACTION_CHUNK_LOADING_STATE -> {
                if (entity == null) return;
                entity.getCapability(ModCapabilities.ROBOT).ifPresent(IRobot::nextChunkLoadingState);
            }
            case ACTION_COLOR_LEFT -> {
                if (!(blockEntity instanceof FactoryBlockEntity factory)) return;
                factory.getEntity().ifPresent(ent -> ent.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                    DyeColor colorLeft = DyeColor.byId(Math.floorMod(parts.getColor().getId() + 1, 16));
                    parts.setColor(colorLeft);
                }));
            }
            case ACTION_COLOR_RIGHT -> {
                if (!(blockEntity instanceof FactoryBlockEntity factory)) return;
                factory.getEntity().ifPresent(ent -> ent.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                    DyeColor colorRight = DyeColor.byId(Math.floorMod(parts.getColor().getId() - 1, 16));
                    parts.setColor(colorRight);
                }));
            }
            case ACTION_FACTORY_BUTTON -> {
                if (blockEntity instanceof FactoryBlockEntity factory) {
                    if (factory.hasCraftedRobotReady()) {
                        factory.createNewRobot(player.getUUID());
                    } else factory.startMachine(2);
                }
                if (blockEntity instanceof StorageBlockEntity storage) {
                    if (storage.getEntity().isPresent()) {
                        storage.exitStorage();
                    }
                }
            }
            case ACTION_DISMANTLE_ROBOT -> {
                if (blockEntity instanceof StorageBlockEntity storage) {
                    Vec3 pos = Vec3.atCenterOf(data.getAsPos()).relative(blockEntity.getBlockState().getValue(MachineBlock.FACING), 0.6);
                    storage.getEntity().ifPresent(ent -> {
                        ent.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                            for (EnumModuleSlot slotType : EnumModuleSlot.values()) {
                                for (ItemStack stack : parts.getBodyParts(slotType)) {
                                    InventoryUtil.dropItem(level, pos.x, pos.y, pos.z, stack);
                                }
                            }
                        });
                        ent.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(items -> {
                            for(int i = 0; i < items.getSlots(); i++) {
                                InventoryUtil.dropItem(level, pos.x, pos.y, pos.z, items.getStackInSlot(i));
                            }
                        });
                    });
                    storage.clearEntity();
                }
            }
        }
    }

    private byte action;
    private NetworkInfo data;

    public PacketComponentAction() {}

    public PacketComponentAction(byte action, NetworkInfo info) {
        this.action = action;
        this.data = info;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(action);
        data.write(buf);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        action = buf.readByte();
        data = new NetworkInfo();
        data.read(buf);
    }

}
