package com.io.norabotics.common.content.menu;

import com.io.norabotics.Reference;
import com.io.norabotics.common.access.AccessConfig;
import com.io.norabotics.common.access.EnumPermission;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.capabilities.ModifiableEnergyStorage;
import com.io.norabotics.common.content.menu.slots.CustomSlot;
import com.io.norabotics.common.robot.EnumModuleSlot;
import com.io.norabotics.common.robot.EnumRobotPart;
import com.io.norabotics.definitions.ModMenuTypes;
import com.io.norabotics.network.container.SyncableInt;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RobotMenu extends BaseMenu {

    public final LivingEntity robot;
    public final AccessConfig access = new AccessConfig();

    public RobotMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getEntity(extraData.readInt()));
        access.read(extraData);
    }

    public RobotMenu(int id, Inventory playerInv, Entity entity) {
        super(ModMenuTypes.ROBOT.get(), playerInv, id);
        this.robot = (LivingEntity) entity;

        addPlayerInv(Reference.GUI_ROBOT_DIMENSIONS);
        robot.getCapability(ForgeCapabilities.ENERGY).ifPresent(e -> {
            if(e instanceof ModifiableEnergyStorage energy) {
                track(SyncableInt.create(energy::getEnergyStored, energy::setEnergy));
                track(SyncableInt.create(energy::getMaxEnergyStored, energy::setMaxEnergyStored));
            }
        });

        robot.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for(int i = 0; i < 4; i++) {
                this.addSlot(new CustomSlot(handler, 5 - i, 8, 8 + 18 * i));
            }
            for(int x = 0; x < 3; x++) {
                for(int y = 0; y < 4; y++) {
                    this.addSlot(new CustomSlot(handler, x * 4 + y + 6, 98 + 18 * x, 8 + 18 * y));
                }
            }

            robot.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                if(parts.hasBodyPart(EnumModuleSlot.RIGHT_ARM)) {
                    this.addSlot(new CustomSlot(handler, 0, 77, 44)); //Mainhand
                }
                if(parts.hasBodyPart(EnumModuleSlot.LEFT_ARM)) {
                    this.addSlot(new CustomSlot(handler, 1, 77, 62)); //Offhand
                }
            });
        });
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int quickMovedSlotIndex) {
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);
        if(!quickMovedSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack rawStack = quickMovedSlot.getItem(); //Stack inside the slot
        if(player.distanceToSqr(robot) > 64) return ItemStack.EMPTY;

        if(quickMovedSlotIndex < 36) {
            if(!rawStack.getAttributeModifiers(EquipmentSlot.OFFHAND).isEmpty() || rawStack.getItem() instanceof ShieldItem) {
                if(!this.moveItemStackTo(rawStack, 53, 54, false)) {
                    return super.quickMoveStack(player, quickMovedSlotIndex);
                }
            }
            if(!rawStack.getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty()) {
                if(!this.moveItemStackTo(rawStack, 52, 54, false)) {
                    return super.quickMoveStack(player, quickMovedSlotIndex);
                }
            }
        }
        return super.quickMoveStack(player, quickMovedSlotIndex);
    }

    @Override
    public void clicked(int p_150400_, int p_150401_, ClickType p_150402_, Player player) {
        for(Slot slot : slots) {
            if(slot instanceof CustomSlot customSlot) {
                customSlot.setInteractable(isInteractable(player));
            }
        }
        super.clicked(p_150400_, p_150401_, p_150402_, player);
    }

    public boolean isInteractable(Player player) {
        return access.hasPermission(player, EnumPermission.INVENTORY) && player.distanceToSqr(robot) < 64;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !robot.isDeadOrDying();
    }
}
