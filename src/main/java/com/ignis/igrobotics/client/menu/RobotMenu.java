package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class RobotMenu extends BaseMenu {
    public final LivingEntity robot;
    public final ContainerData data;

    public RobotMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getEntity(extraData.readInt()), new SimpleContainerData(2));
    }

    public RobotMenu(int id, Inventory playerInv, Entity entity, ContainerData data) {
        super(ModMenuTypes.ROBOT.get(), id);
        this.robot = (LivingEntity) entity;
        this.data = data;

        addPlayerInv(playerInv, Reference.GUI_ROBOT_DIMENSIONS);
        addDataSlots(data);

        //TODO: Armor slots should only accept valid items
        robot.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for(int i = 0; i < 4; i++) {
                this.addSlot(new SlotItemHandler(handler, 5 - i, 8, 8 + 18 * i));
            }
            for(int x = 0; x < 3; x++) {
                for(int y = 0; y < 4; y++) {
                    this.addSlot(new SlotItemHandler(handler, x * 4 + y + 6, 98 + 18 * x, 8 + 18 * y));
                }
            }

            robot.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                if(parts.hasBodyPart(EnumRobotPart.RIGHT_ARM)) {
                    this.addSlot(new SlotItemHandler(handler, 0, 77, 44)); //Mainhand
                }
                if(parts.hasBodyPart(EnumRobotPart.LEFT_ARM)) {
                    this.addSlot(new SlotItemHandler(handler, 1, 77, 62)); //Offhand
                }
            });
        });
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !robot.isDeadOrDying();
    }
}
