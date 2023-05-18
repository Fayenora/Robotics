package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.common.blockentity.MachineBlockEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.definitions.ModAttributes;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactoryMenu extends BaseMenu {

    public final FactoryBlockEntity blockEntity;
    private final Level level;
    public final ContainerData data;

    private static final int SLOTS_MACHINE = 6;
    private static final int SLOTS_PLAYER = 36;

    private int currentModuleSlots = Reference.MAX_MODULES;

    public FactoryMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public FactoryMenu(int id, Inventory playerInv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.FACTORY.get(), id);
        this.blockEntity = (FactoryBlockEntity) blockEntity;
        this.level = playerInv.player.level;
        this.data = data;

        addDataSlots(data);
        addPlayerInv(playerInv, 36, 137);
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            addSlot(new SlotItemHandler(handler, 0, 27, 17)); //Head
            addSlot(new SlotItemHandler(handler, 1, 186, 17)); //Body
            addSlot(new SlotItemHandler(handler, 2, 186, 63)); //Left arm
            addSlot(new SlotItemHandler(handler, 3, 27, 63)); //Right arm
            addSlot(new SlotItemHandler(handler, 4, 186, 109)); //Left leg
            addSlot(new SlotItemHandler(handler, 5, 27, 109)); //Right leg

            //Module Slots
            for(int i = 0; i < Reference.MAX_MODULES; i++) {
                addSlot(new SlotModule(handler, 6 + i, 207, 17 + 23 * i));
            }
        });
        addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu menu, int p_39316_, ItemStack stack) {}
            @Override
            public void dataChanged(AbstractContainerMenu menu, int key, int value) {
                //Listen to data changes on the server
                if(key == MachineBlockEntity.DATA_INVENTORY) {
                    changeModuleSlots(value - SLOTS_MACHINE);
                }
            }
        });

        updateSlotAmount();
    }

    @Override
    public void setData(int key, int value) {
        super.setData(key, value);
        //Listen to data changes on the client
        if(key == MachineBlockEntity.DATA_INVENTORY) {
            changeModuleSlots(value - SLOTS_MACHINE);
        }
    }

    private void updateSlotAmount() {
        if(blockEntity.getEntity().isPresent() && blockEntity.getEntity().get() instanceof LivingEntity living) {
            int size = living.getAttributes().hasAttribute(ModAttributes.MODIFIER_SLOTS) ? (int) living.getAttributeValue(ModAttributes.MODIFIER_SLOTS) : 0;
            changeModuleSlots(size);
        }
    }

    private void changeModuleSlots(int newSize) {
        for(int i = currentModuleSlots; i < newSize; i++) {
            setModuleSlotEnabled(i, true);
        }
        for(int i = currentModuleSlots - 1; i >= newSize; i--) {
            setModuleSlotEnabled(i, false);
        }
        currentModuleSlots = newSize;
    }

    private void setModuleSlotEnabled(int index, boolean enabled) {
        if(index < 0 || index > Reference.MAX_MODULES) return;
        if(!(slots.get(SLOTS_PLAYER + SLOTS_MACHINE + index) instanceof SlotModule modSlot)) return;
        modSlot.setActive(enabled);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ROBOT_FACTORY.get());
    }
}
