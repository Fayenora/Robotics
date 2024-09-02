package com.ignis.norabotics.common.content.blockentity;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.utils.Vec3f;
import com.ignis.norabotics.client.rendering.MachineArmModel;
import com.ignis.norabotics.common.helpers.util.MathUtil;
import com.ignis.norabotics.common.helpers.util.NBTUtil;
import com.ignis.norabotics.definitions.ModMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import static com.ignis.norabotics.client.rendering.MachineArmModel.JOINT_COUNT;
import static com.ignis.norabotics.client.rendering.MachineArmModel.constructChain;

public class MachineArmBlockEntity extends BlockEntity {
    FabrikChain3D chain;
    Vec3f target;

    public MachineArmBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModMachines.MACHINE_ARM.get(), pPos, pBlockState);
        chain = MachineArmModel.constructDefaultChain();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineArmBlockEntity machineArm) {
        if(level.getServer().getPlayerList().getPlayerCount() == 0) return;
        Vec3 playerPos = level.getServer().getPlayerList().getPlayers().get(0).getEyePosition();
        Vec3 base = Vec3.atLowerCornerOf(pos).add(MachineArmModel.LOWER_LEFT_CORNER_OFFSET);
        machineArm.target = MathUtil.of(playerPos.subtract(base).scale(16));
        machineArm.getPose().solveForTarget(machineArm.target);
        level.sendBlockUpdated(pos, state, state, 3);
        machineArm.setChanged();
    }

    private CompoundTag saveChain(CompoundTag tag) {
        ListTag list = new ListTag();
        for(FabrikBone3D bone : chain.getChain()) {
            list.add(NBTUtil.serializeVec(bone.getDirectionUV()));
        }
        tag.put("rotations", list);
        tag.put("target", NBTUtil.serializeVec(target));
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
}
