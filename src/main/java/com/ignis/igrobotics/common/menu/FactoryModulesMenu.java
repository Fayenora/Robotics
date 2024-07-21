package com.ignis.igrobotics.common.menu;

import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.EnumModuleSlot;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FactoryModulesMenu extends BaseMenu {

    public final FactoryBlockEntity blockEntity;
    private final Level level;

    private final Map<EnumModuleSlot, Integer> moduleSlots;

    public FactoryModulesMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, extraData.readMap(buf -> buf.readEnum(EnumModuleSlot.class), FriendlyByteBuf::readInt), inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public FactoryModulesMenu(int id, Inventory playerInv, Map<EnumModuleSlot, Integer> moduleSlots, BlockEntity blockEntity) {
        super(ModMenuTypes.FACTORY_MODULES.get(), playerInv, id);
        this.blockEntity = (FactoryBlockEntity) blockEntity;
        this.blockEntity.addTrackingContent(this);
        this.level = playerInv.player.level();
        this.moduleSlots = moduleSlots;

        addPlayerInv(36, 137);
        if(this.blockEntity.getEntity().isEmpty()) return;
        this.blockEntity.getEntity().get().getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                addModuleSlots(handler, EnumModuleSlot.SENSOR, 26, 16, false);
                addModuleSlots(handler, EnumModuleSlot.REACTOR, 26, 62, false);
                addModuleSlots(handler, EnumModuleSlot.FEET, 26, 108, false);

                addModuleSlots(handler, EnumModuleSlot.SKIN, 185, 16, true);
                addModuleSlots(handler, EnumModuleSlot.FIST, 185, 62, true);
                addModuleSlots(handler, EnumModuleSlot.CORE, 185, 108, true);
            });
        });
    }

    private void addModuleSlots(IItemHandler handler, EnumModuleSlot slotType, int x, int y, boolean reverse) {
        for(int i = 0; i < moduleSlots.getOrDefault(slotType, 0); i++) {
            int slotId = slotType.ordinal() * EnumModuleSlot.values().length + i + 6;
            addSlot(new SlotItemHandler(handler, slotId, x + (reverse ? -22 : 22) * (i % 4) + 1, y + (i > 3 ? 22 : 0) + 1));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ROBOT_FACTORY.get());
    }

    public Map<EnumModuleSlot, Integer> getModuleSlots() {
        return moduleSlots;
    }
}
