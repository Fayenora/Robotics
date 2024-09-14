package com.ignis.norabotics.common.content.menu;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.common.capabilities.IPerkMap;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.capabilities.impl.inventory.FactoryInventory;
import com.ignis.norabotics.common.capabilities.impl.perk.Perk;
import com.ignis.norabotics.common.content.blockentity.FactoryBlockEntity;
import com.ignis.norabotics.common.content.menu.slots.CustomSlot;
import com.ignis.norabotics.common.helpers.types.Tuple;
import com.ignis.norabotics.common.helpers.util.InventoryUtil;
import com.ignis.norabotics.common.robot.EnumModuleSlot;
import com.ignis.norabotics.common.robot.EnumRobotPart;
import com.ignis.norabotics.common.robot.RobotPart;
import com.ignis.norabotics.definitions.ModAttributes;
import com.ignis.norabotics.definitions.ModBlocks;
import com.ignis.norabotics.definitions.ModMenuTypes;
import com.ignis.norabotics.definitions.robotics.ModModules;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactoryMenu extends BaseMenu {

    public final FactoryBlockEntity blockEntity;
    private final Level level;
    private Map<EnumRobotPart, Map<EnumModuleSlot, NonNullList<ItemStack>>> moduleAssignment = new HashMap<>();

    public FactoryMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public FactoryMenu(int id, Inventory playerInv, BlockEntity blockEntity) {
        super(ModMenuTypes.FACTORY.get(), playerInv, id);
        this.blockEntity = (FactoryBlockEntity) blockEntity;
        this.blockEntity.addTrackingContent(this);
        this.level = playerInv.player.level();


        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            List<CustomSlot> slots = List.of(
                    new CustomSlot(handler, 0, 27, 17), //Head
                    new CustomSlot(handler, 1, 186, 17), //Body
                    new CustomSlot(handler, 2, 186, 63), //Left arm
                    new CustomSlot(handler, 3, 27, 63), //Right arm
                    new CustomSlot(handler, 4, 186, 109), //Left leg
                    new CustomSlot(handler, 5, 27, 109) //Right leg
            );
            for(CustomSlot slot : slots) {
                slot.setActive(false);
                addSlot(slot);
            }
            this.blockEntity.getEntity().get().getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                for(EnumModuleSlot slotType : EnumModuleSlot.nonPrimaries()) {
                    for(int i = 0; i < Reference.MAX_MODULES; i++) {
                        int slotId = FactoryInventory.typeToSlotId(slotType, i);
                        CustomSlot slot = new CustomSlot(handler, slotId, 0, 0);
                        slot.setActive(false);
                        addSlot(slot);
                    }
                }
            });
        });

        updateModuleAssignment();
    }

    private void updateModuleAssignment() {
        moduleAssignment.clear();
        Map<EnumModuleSlot, Integer> alreadyDistributed = new HashMap<>();
        for (EnumModuleSlot slotType : EnumModuleSlot.values()) {
            alreadyDistributed.put(slotType, 0);
        }
        for (int i = 0; i < 6; i++) {
            RobotPart part = RobotPart.getFromItem(getSlot(i).getItem().getItem());
            if (part == null) continue;
            moduleAssignment.put(part.getPart(), new HashMap<>());
            Map<EnumModuleSlot, Integer> toAdd = moduleSlotsAsAddedBy(getSlot(i).getItem());
            for (Map.Entry<EnumModuleSlot, Integer> entry : toAdd.entrySet()) {
                if(entry.getValue() <= 0) continue;
                EnumModuleSlot slotType = entry.getKey();
                moduleAssignment.get(part.getPart()).put(slotType, getNMoulesOfType(slotType, entry.getValue(), alreadyDistributed.get(slotType)));
                alreadyDistributed.put(slotType, alreadyDistributed.get(slotType) + entry.getValue());
            }
        }
    }

    private Map<EnumModuleSlot, Integer> moduleSlotsAsAddedBy(ItemStack stack) {
        IPerkMap perks = ModModules.get(stack).getPerks();
        Map<EnumModuleSlot, Integer> moduleSlotsAsAddedBy = new HashMap<>();
        Map<Attribute, AttributeInstance> map = new HashMap<>();
        for(Attribute attr : ModAttributes.MODIFIER_SLOTS.values()) {
            AttributeInstance inst = new AttributeInstance(attr, i -> {});
            inst.setBaseValue(0);
            map.put(attr, inst);
        }
        AttributeMap tempAttrMap = new AttributeMap(new AttributeSupplier(map));
        for(Tuple<Perk, Integer> tup : perks) {
            tempAttrMap.addTransientAttributeModifiers(tup.getFirst().getAttributeModifiers(tup.getSecond()));
        }
        for(EnumModuleSlot slotType : ModAttributes.MODIFIER_SLOTS.keySet()) {
            moduleSlotsAsAddedBy.put(slotType, (int) tempAttrMap.getValue(ModAttributes.MODIFIER_SLOTS.get(slotType)));
        }
        return moduleSlotsAsAddedBy;
    }

    private NonNullList<ItemStack> getNMoulesOfType(EnumModuleSlot slotType, int n, int lowerIndex) {
        List<ItemStack> stacks = new ArrayList<>();
        int startInd = FactoryInventory.typeToSlotId(slotType, lowerIndex);
        int endInd = FactoryInventory.typeToSlotId(slotType,lowerIndex + n);
        for(Slot slot : slots.subList(startInd, endInd)) {
            stacks.add(slot.getItem());
        }
        return InventoryUtil.toNonNullList(stacks);
    }

    @Override
    public void slotsChanged(Container pContainer) {
        super.slotsChanged(pContainer);
        updateModuleAssignment();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ROBOT_FACTORY.get());
    }

    public Map<EnumModuleSlot, NonNullList<ItemStack>> getModuleAssignmentOfPart(EnumRobotPart part) {
        return moduleAssignment.get(part);
    }
}
