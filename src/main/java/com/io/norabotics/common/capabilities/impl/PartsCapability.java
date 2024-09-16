package com.io.norabotics.common.capabilities.impl;

import com.io.norabotics.Reference;
import com.io.norabotics.common.capabilities.IPartBuilt;
import com.io.norabotics.common.capabilities.IPerkMap;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.content.entity.RobotEntity;
import com.io.norabotics.common.content.events.PerkChangeEvent;
import com.io.norabotics.common.helpers.util.InventoryUtil;
import com.io.norabotics.common.robot.*;
import com.io.norabotics.definitions.ModAttributes;
import com.io.norabotics.definitions.robotics.ModModules;
import com.io.norabotics.integration.config.RoboticsConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;

public class PartsCapability implements IPartBuilt {

	LivingEntity entity;
	SynchedEntityData dataManager;

	private static final EntityDataAccessor<Integer> RENDER_OVERLAYS = RobotEntity.RENDER_OVERLAYS;
	private static final EntityDataAccessor<Integer> COLOR = RobotEntity.COLOR;
	private static final EntityDataAccessor<Integer>[] BODY_PARTS = RobotEntity.BODY_PARTS;

	private final Map<EnumModuleSlot, NonNullList<ItemStack>> modules = new HashMap<>();
	private final Map<EnumModuleSlot, Integer> moduleSlots = new HashMap<>();
	
	public PartsCapability() {}
	
	public PartsCapability(LivingEntity entity) {
		this.entity = entity;
		this.dataManager = entity.getEntityData();
		for(EnumModuleSlot slotType : EnumModuleSlot.values()) {
			modules.put(slotType, NonNullList.withSize(slotType.isPrimary() ? 1 : Reference.MAX_MODULES, ItemStack.EMPTY));
			moduleSlots.put(slotType, slotType.isPrimary() ? 1 : 0);
		}

		dataManager.define(RENDER_OVERLAYS, 0);
		dataManager.define(COLOR, 0);
		for (EntityDataAccessor<Integer> bodyPart : BODY_PARTS) {
			dataManager.define(bodyPart, EnumRobotMaterial.NONE.getID());
		}
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("color", this.dataManager.get(COLOR));
		for(EnumModuleSlot slot : EnumModuleSlot.values()) {
			InventoryUtil.saveAllItems(nbt, modules.get(slot), slot.name());
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		dataManager.set(COLOR, nbt.getInt("color"));
		for(EnumModuleSlot slot : EnumModuleSlot.values()) {
			NonNullList<ItemStack> stacks = InventoryUtil.loadAllItems(nbt, slot.name());
			for(int i = 0; i < stacks.size(); i++) {
				setModule(slot, i, stacks.get(i)); //Load modules in directly to avoid them going under the maximum and get discarded
			}
		}
		entity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> MinecraftForge.EVENT_BUS.post(new PerkChangeEvent(entity, perks)));
	}

	@Override
	public NonNullList<ItemStack> getBodyParts(EnumModuleSlot slotType) {
		if(slotType.isPrimary()) {
			return NonNullList.of(ItemStack.EMPTY, RobotPart.get(EnumRobotPart.valueOf(slotType), materialForSlot(slotType)).getItemStack(1));
		}
		return InventoryUtil.toNonNullList(modules.get(slotType).subList(0, getMaxBodyParts(slotType)));
	}

	@Override
	public void setBodyParts(EnumModuleSlot slotType, NonNullList<ItemStack> components) {
		int min = Math.min(components.size(), modules.get(slotType).size());
		int max = Math.min(getMaxBodyParts(slotType), Math.max(components.size(), modules.get(slotType).size()));
		if(max < min) return;
		for(int i = min; i < max; i++) {
			InventoryUtil.dropItem(entity, modules.get(slotType).get(i));
			setModule(slotType, i, ItemStack.EMPTY);
		}
		for(int i = 0; i < min; i++) {
			setModule(slotType, i, components.get(i));
		}
		entity.getCapability(ModCapabilities.PERKS).ifPresent(perks -> MinecraftForge.EVENT_BUS.post(new PerkChangeEvent(entity, perks)));
	}

	@Override
	public int getMaxBodyParts(EnumModuleSlot slotType) {
		return (int) entity.getAttributeValue(ModAttributes.MODIFIER_SLOTS.get(slotType));
	}

	@Override
	public void setMaxBodyParts(EnumModuleSlot slotType, int size) {
		moduleSlots.put(slotType, size);
	}

	@Override
	public EnumRobotMaterial materialForSlot(EnumModuleSlot slotType) {
		if(!slotType.isPrimary()) return EnumRobotMaterial.NONE;
		return EnumRobotMaterial.byId(dataManager.get(BODY_PARTS[EnumRobotPart.valueOf(slotType).getID()]));
	}

	private void setModule(EnumModuleSlot slotType, int slot, ItemStack item) {
		if(modules.get(slotType).size() < slot) return;
		IPerkMap perkMap = entity.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS);
		NonNullList<ItemStack> moduleList = modules.get(slotType);

		// Remove perks & render layers from previous part
		if(ModModules.isModule(moduleList.get(slot))) {
			RobotModule oldModule = ModModules.get(moduleList.get(slot));
			perkMap.diff(oldModule.getPerks());
			if(oldModule.hasOverlay()) {
				int overlayId = ModModules.getOverlayID(oldModule);
				removeRenderLayer(overlayId);
			}
		}

		// Set the module
		modules.get(slotType).set(slot, item);
		RobotPart robotPart = RobotPart.getFromItem(item.getItem());
		EnumRobotMaterial material = robotPart != null ? robotPart.getMaterial() : EnumRobotMaterial.NONE;
		this.dataManager.set(BODY_PARTS[EnumRobotPart.valueOf(slotType).getID()], material.getID()); // Tell the data manager & with it the client

		// Apply perks & render layers of the new part
		if(ModModules.isModule(item)) {
			RobotModule newModule = ModModules.get(item);
			perkMap.merge(newModule.getPerks());
			if(newModule.hasOverlay()) {
				int overlayId = ModModules.getOverlayID(newModule);
				addRenderLayer(overlayId);
			}
		}
	}

	@Override
	public void destroyBodyPart(EnumModuleSlot part) {
		if(!RoboticsConfig.general.limbDestruction.get()) return;
		modules.get(part).forEach(i -> InventoryUtil.dropItem(entity, i));
		entity.playSound(SoundEvents.ANVIL_FALL, 1, 1);
		setBodyParts(part, NonNullList.withSize(getMaxBodyParts(part), ItemStack.EMPTY));

		//Drop any held items, if an arm got destroyed
		if(part == EnumModuleSlot.RIGHT_ARM || part == EnumModuleSlot.LEFT_ARM) {
			EquipmentSlot slot = Boolean.logicalXor(part == EnumModuleSlot.RIGHT_ARM, entity.getMainArm() == HumanoidArm.RIGHT) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
			InventoryUtil.dropItem(entity.level(), entity.position().x, entity.position().y, entity.position().z, entity.getItemBySlot(slot));
			entity.setItemSlot(slot, ItemStack.EMPTY);
		}

		if(!isValid()) {
			entity.kill();
		}
	}

	/////////////////////
	// Colors
	/////////////////////

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

	/////////////////////
	// Render Layers
	/////////////////////

	public void addRenderLayer(int id) {
		if(id >= Reference.MAX_RENDER_LAYERS || id < 0) return;
		int currentOverlays = dataManager.get(RENDER_OVERLAYS);
		this.dataManager.set(RENDER_OVERLAYS, currentOverlays | (1 << id));
	}

	public void removeRenderLayer(int id) {
		if(id >= Reference.MAX_RENDER_LAYERS || id < 0) return;
		int currentOverlays = dataManager.get(RENDER_OVERLAYS);
		this.dataManager.set(RENDER_OVERLAYS, currentOverlays & ~(1 << id));
	}

	@Override
	public boolean hasRenderLayer(int id) {
		if(id >= Reference.MAX_RENDER_LAYERS || id < 0) return false;
		return ((dataManager.get(RENDER_OVERLAYS) >> id) & 1) == 1;
	}
}
