package com.ignis.igrobotics.core.capabilities.parts;

import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMapCap;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.core.util.InventoryUtil;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class PartsCapability implements IPartBuilt {

	LivingEntity entity;
	SynchedEntityData dataManager;
	
	private static final EntityDataAccessor<Integer> COLOR = RobotEntity.COLOR;
	private static final EntityDataAccessor<Integer>[] BODY_PARTS = RobotEntity.BODY_PARTS;
	
	public static final EnumRobotPart[] PARTS = EnumRobotPart.values();
	
	public PartsCapability() {}
	
	public PartsCapability(LivingEntity entity) {
		this.entity = entity;
		this.dataManager = entity.getEntityData();

		dataManager.define(COLOR, 0);
		for (EntityDataAccessor<Integer> bodyPart : BODY_PARTS) {
			dataManager.define(bodyPart, 0);
		}
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("color", this.dataManager.get(COLOR));
		nbt.putIntArray("parts", getMaterials());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		setMaterials(nbt.getIntArray("parts"));
		dataManager.set(COLOR, nbt.getInt("color"));
	}

	@Override
	public RobotPart[] getBodyParts() {
		RobotPart[] parts = new RobotPart[PARTS.length];
		for(int i = 0; i < parts.length; i++) {
			parts[i] = getBodyPart(PARTS[i]);
		}
		return parts;
	}

	@Override
	public RobotPart getBodyPart(EnumRobotPart part) {
		return RobotPart.get(part, EnumRobotMaterial.byId(this.dataManager.get(BODY_PARTS[part.getID()])));
	}

	@Override
	public void setBodyPart(RobotPart part) {
		if(!entity.getCapability(ModCapabilities.PERKS, null).isPresent()) {
			this.dataManager.set(BODY_PARTS[part.getPart().getID()], part.getMaterial().getID());
			return;
		}
		
		IPerkMapCap perkMap = entity.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS);
		
		//Remove perks from previous part
		RobotPart current = getBodyPart(part.getPart());
		perkMap.diff(current.getPerks());
		
		this.dataManager.set(BODY_PARTS[part.getPart().getID()], part.getMaterial().getID());
		
		//Apply perks from new part
		RobotPart newPart = getBodyPart(part.getPart());
		perkMap.merge(newPart.getPerks()); 
		perkMap.updateAttributeModifiers();
	}

	@Override
	public void destroyBodyPart(EnumRobotPart part) {
		if(!RoboticsConfig.general.limbDestruction.get()) return;
		RobotPart robotPart = getBodyPart(part);
		InventoryUtil.dropItem(entity.level(), entity.position().x, entity.position().y, entity.position().z, robotPart.getItemStack(1));
		entity.playSound(SoundEvents.ANVIL_FALL, 1, 1);
		setBodyPart(part, EnumRobotMaterial.NONE);

		//Drop any held items, if an arm got destroyed
		if(part == EnumRobotPart.RIGHT_ARM || part == EnumRobotPart.LEFT_ARM) {
			EquipmentSlot slot = Boolean.logicalXor(part == EnumRobotPart.RIGHT_ARM, entity.getMainArm() == HumanoidArm.RIGHT) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
			InventoryUtil.dropItem(entity.level(), entity.position().x, entity.position().y, entity.position().z, entity.getItemBySlot(slot));
			entity.setItemSlot(slot, ItemStack.EMPTY);
		}

		if(part == EnumRobotPart.BODY || !(hasBodyPart(EnumRobotPart.LEFT_LEG) || hasBodyPart(EnumRobotPart.RIGHT_LEG))) {
			entity.kill();
		}
	}
	
	private void setMaterials(int[] materials) {
		for(int i = 0; i < materials.length; i++) {
			setBodyPart(PARTS[i], EnumRobotMaterial.byId(materials[i]));
		}
	}
	
	private int[] getMaterials() {
		RobotPart[] parts = getBodyParts();
		int[] materials = new int[parts.length];
		for(int i = 0; i < parts.length; i++) {
			materials[i] = parts[i].getMaterial().getID();
		}
		return materials;
	}

	@Override
	public void setColor(DyeColor color) {
		this.dataManager.set(COLOR, color.getId() * DyeColor.values().length + color.getId());
	}
	
	@Override
	public DyeColor getColor() {
		return DyeColor.byId(this.dataManager.get(COLOR) & 15);
	}
	
	@Override
	public void setTemporaryColor(DyeColor color) {
		this.dataManager.set(COLOR, color.getId() * DyeColor.values().length + getColor().getId());
	}
	
	@Override
	public DyeColor getTemporaryColor() {
		return DyeColor.byId(Math.floorDiv(dataManager.get(COLOR), DyeColor.values().length) & 15);
	}
}
