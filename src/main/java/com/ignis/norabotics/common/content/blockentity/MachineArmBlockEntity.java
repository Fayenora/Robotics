package com.ignis.norabotics.common.content.blockentity;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.utils.Vec3f;
import com.ignis.norabotics.client.rendering.MachineArmModel;
import com.ignis.norabotics.common.helpers.util.InventoryUtil;
import com.ignis.norabotics.common.helpers.util.MathUtil;
import com.ignis.norabotics.common.helpers.util.NBTUtil;
import com.ignis.norabotics.common.robot.EnumRobotPart;
import com.ignis.norabotics.common.robot.RobotPart;
import com.ignis.norabotics.definitions.ModBlocks;
import com.ignis.norabotics.definitions.ModMachines;
import com.ignis.norabotics.definitions.robotics.ModModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.joml.Vector3f;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;

import static com.ignis.norabotics.client.rendering.MachineArmModel.JOINT_COUNT;
import static com.ignis.norabotics.client.rendering.MachineArmModel.constructChain;

public class MachineArmBlockEntity extends BlockEntity {

    final Vec3 rotationBase;
    final AABB seekRadius;

    FabrikChain3D chain;
    Vec3f target;
    ItemStack grabbedItem = ItemStack.EMPTY;
    MachineArmState state = MachineArmState.IDLE;

    public MachineArmBlockEntity(BlockPos pos, BlockState pBlockState) {
        super(ModMachines.MACHINE_ARM.get(), pos, pBlockState);
        chain = MachineArmModel.constructDefaultChain();
        target = new Vec3f(1, 1, 0);
        rotationBase = Vec3.atLowerCornerOf(pos).add(MachineArmModel.LOWER_LEFT_CORNER_OFFSET);
        seekRadius = new AABB(pos).inflate(4, 0, 4).expandTowards(0, 4, 0).expandTowards(0, -1, 0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineArmBlockEntity machineArm) {
        if(machineArm.getGrabbedItem().isEmpty()) {
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, machineArm.seekRadius, i -> ModModules.isModule(i.getItem()));
            Optional<ItemEntity> closest = items.stream().min((i1, i2) -> (int) (i2.distanceToSqr(machineArm.rotationBase) - i1.distanceToSqr(machineArm.rotationBase)));
            if(closest.isEmpty()) return;
            machineArm.tryPickingUpItem(closest.get());
        } else {
            BlockPos nearestFactory = findNearestFactory(level, pos, 5, 5);
            if(nearestFactory == null) return;
            machineArm.tryDroppingOfItemAt(nearestFactory);
        }
    }

    private void tryPickingUpItem(ItemEntity item) {
        state = MachineArmState.PICKING_ITEM;
        if(moveToTargetVec(item.onGround() ? item.position() : item.position().add(item.getDeltaMovement()))) {
            grabbedItem = item.getItem();
            level.playSound(null, target.x, target.y, target.z, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1, 1);
            item.kill();
        }
    }

    private void tryDroppingOfItemAt(BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(!(tile instanceof FactoryBlockEntity factory)) return;
        RobotPart robotPart = RobotPart.getFromItem(grabbedItem.getItem());
        if(robotPart == null && !ModModules.isModule(grabbedItem)) return;
        Direction dir = level.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING);
        Vector3f offset = switch(robotPart == null ? EnumRobotPart.BODY : robotPart.getPart()) {
            case HEAD -> new Vector3f(0, 1.7f, 0);
            case BODY -> new Vector3f(0, 1, 0);
            case LEFT_ARM -> dir.getCounterClockWise().step().mul(0.3f).add(0, 1.5f, 0);
            case RIGHT_ARM -> dir.getClockWise().step().mul(0.3f).add(0, 1.5f, 0);
            case LEFT_LEG -> dir.getCounterClockWise().step().mul(0.15f).add(0, 0.3f, 0);
            case RIGHT_LEG -> dir.getClockWise().step().mul(0.15f).add(0, 0.3f, 0);
        };
        Vec3 target = Vec3.atBottomCenterOf(pos).add(offset.x, offset.y, offset.z);
        if(moveToTargetVec(target)) {
            tile.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(i -> {
                ItemStack remainder = InventoryUtil.insert(i, grabbedItem, false);
                if(remainder.getCount() != grabbedItem.getCount()) level.playSound(null, target.x, target.y, target.z, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.BLOCKS, 1, 1);
                grabbedItem = remainder;
            });
        }
    }

    private boolean moveToTargetVec(Vec3 target) {
        this.target = MathUtil.of(target.subtract(rotationBase).scale(16));
        chain.solveForTarget(this.target);
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        setChanged();
        return getPose().getEffectorLocation().approximatelyEquals(this.target, 1);
    }

    private CompoundTag saveChain(CompoundTag tag) {
        ListTag list = new ListTag();
        for(FabrikBone3D bone : chain.getChain()) {
            list.add(NBTUtil.serializeVec(bone.getDirectionUV()));
        }
        tag.put("rotations", list);
        tag.put("target", NBTUtil.serializeVec(target));
        tag.put("grabbed", grabbedItem.serializeNBT());
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(saveChain(tag));
    }

    @Override
    public void load(CompoundTag tag) {
        Vec3f[] rotations = new Vec3f[JOINT_COUNT];
        ListTag list = tag.getList("rotations", Tag.TAG_LIST);
        for(int i = 0; i < Math.min(rotations.length, list.size()); i++) {
            rotations[i] = NBTUtil.deserializeVec(list.getList(i));
        }
        chain = constructChain(rotations);
        target = NBTUtil.deserializeVec(tag.getList("target", Tag.TAG_FLOAT));
        grabbedItem = ItemStack.of(tag.getCompound("grabbed"));
        super.load(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveChain(super.getUpdateTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    public FabrikChain3D getPose() {
        return chain;
    }

    public Vec3f getTarget() {
        return target;
    }

    public ItemStack getGrabbedItem() {
        return grabbedItem;
    }

    public void dropGrabbedItem() {
        // TODO Drop from arm end if it is calculated correctly
        Vec3 pos = Vec3.atBottomCenterOf(getBlockPos());
        InventoryUtil.dropItem(getLevel(), pos.x, pos.y, pos.z, getGrabbedItem());
        grabbedItem = ItemStack.EMPTY;
    }

    private static BlockPos findNearestFactory(Level level, BlockPos source, int verticalSearchRange, int searchRange) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Block factory = ModBlocks.ROBOT_FACTORY.get();

        for(int k = 0; k <= verticalSearchRange; k++) {
            for(int l = 0; l < searchRange; ++l) {
                for(int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
                    for(int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
                        pos.setWithOffset(source, i1, k - 1, j1);
                        if (level.getBlockState(pos).getBlock().equals(factory) && level.getBlockState(pos).getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                            return pos;
                        }
                    }
                }
            }
        }

        return null;
    }

    public enum MachineArmState {
        IDLE,
        PICKING_ITEM,
        WELDING
    }
}