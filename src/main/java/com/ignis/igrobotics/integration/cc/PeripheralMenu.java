package com.ignis.igrobotics.integration.cc;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.menu.BaseMenu;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class PeripheralMenu extends BaseMenu {

    public final LivingEntity entity;
    private ServerComputer computer;

    public PeripheralMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getEntity(extraData.readInt()), null);
    }

    public PeripheralMenu(int id, Inventory playerInv, Entity entity, @Nullable ServerComputer computer) {
        super(ModMenuTypes.PERIPHERALS.get(), id);
        this.entity = (LivingEntity) entity;

        addPlayerInv(playerInv, Reference.GUI_ROBOT_PERIPHERALS_DIMENSION);
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return !entity.isDeadOrDying();
    }
}
