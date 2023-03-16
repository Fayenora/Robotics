package com.ignis.igrobotics.core.capabilities.parts;

import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.EnumRobotMaterial;
import com.ignis.igrobotics.core.EnumRobotPart;
import com.ignis.igrobotics.core.RobotPart;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.perks.IPerkMapCap;
import com.ignis.igrobotics.core.util.ItemStackUtils;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

import java.util.Random;

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
		for(int i = 0; i < BODY_PARTS.length; i++) {
			dataManager.define(BODY_PARTS[i], 1);
		}
	}
	
	@Override
	public void writeToNBT(CompoundTag compound) {
		compound.putInt("color", this.dataManager.get(COLOR));
		compound.putIntArray("parts", getMaterials());
	}
	
	@Override
	public void readFromNBT(CompoundTag compound) {
		setMaterials(compound.getIntArray("parts"));
		dataManager.set(COLOR, compound.getInt("color"));
	}
	
	@Override
	public void onDeath(DamageSource cause) {
		if(entity.level.isClientSide()) return;
		Random r = new Random();
		if(entity.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			for(RobotPart part : getBodyParts()) {
				if(hasBodyPart(part.getPart()) && r.nextDouble() < RoboticsConfig.current().general.limbDropChance.get()) {
					ItemStackUtils.dropItem(entity.level, entity.position().x, entity.position().y, entity.position().z, part.getItemStack(1));
				}
			}
		}
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
		if(!entity.getCapability(ModCapabilities.PERK_MAP_CAPABILITY, null).isPresent()) {
			this.dataManager.set(BODY_PARTS[part.getPart().getID()], part.getMaterial().getID());
			return;
		}
		
		IPerkMapCap perkMap = entity.getCapability(ModCapabilities.PERK_MAP_CAPABILITY, null).orElse(null);
		
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
		if(!RoboticsConfig.current().general.limbDestruction.get()) return;
		RobotPart robotPart = getBodyPart(part);
		ItemStackUtils.dropItem(entity.level, entity.position().x, entity.position().y, entity.position().z, robotPart.getItemStack(1));
		entity.playSound(SoundEvents.ANVIL_FALL, 1, 1);
		setBodyPart(part, EnumRobotMaterial.NONE);

		//Drop any held items, if the arm got destroyed
		if(part == EnumRobotPart.RIGHT_ARM || part == EnumRobotPart.LEFT_ARM) {
			EquipmentSlot slot = (part == EnumRobotPart.RIGHT_ARM) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
			ItemStackUtils.dropItem(entity.level, entity.position().x, entity.position().y, entity.position().z, entity.getItemBySlot(slot));
			entity.setItemSlot(slot, ItemStack.EMPTY);
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
		return DyeColor.byId(this.dataManager.get(COLOR).intValue() & 15);
	}
	
	@Override
	public void setTemporaryColor(DyeColor color) {
		this.dataManager.set(COLOR, color.getId() * DyeColor.values().length + getColor().getId());
	}
	
	@Override
	public DyeColor getTemporaryColor() {
		return DyeColor.byId(Math.floorDiv(dataManager.get(COLOR).intValue(), DyeColor.values().length) & 15);
	}

}
