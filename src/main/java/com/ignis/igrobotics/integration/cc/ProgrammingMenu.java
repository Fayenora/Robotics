package com.ignis.igrobotics.integration.cc;

import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.integration.cc.vanilla.IProgrammingMenu;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.inventory.ComputerMenuWithoutInventory;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Predicate;

public class ProgrammingMenu extends ComputerMenuWithoutInventory implements IProgrammingMenu {

    public final LivingEntity robot;

    public ProgrammingMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super((MenuType<ProgrammingMenu>) ModMenuTypes.COMPUTER.get(), id, inv, new ComputerContainerData(extraData));
        this.robot = (LivingEntity) inv.player.level().getEntity(extraData.readInt());
    }

    public ProgrammingMenu(int id, Inventory player, Entity robot, Predicate<Player> canUse, ServerComputer computer) {
        super((MenuType<ProgrammingMenu>) ModMenuTypes.COMPUTER.get(), id, player, canUse, computer, ComputerFamily.ADVANCED);
        this.robot = (LivingEntity) robot;
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return !robot.isDeadOrDying();
    }
}
