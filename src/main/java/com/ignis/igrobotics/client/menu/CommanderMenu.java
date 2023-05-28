package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.network.messages.EntityByteBufUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommanderMenu extends AbstractContainerMenu {

    Collection<LivingEntity> robots;

    public CommanderMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, EntityByteBufUtil.readEntities(extraData));
    }

    public CommanderMenu(int id, Collection<LivingEntity> robots) {
        super(ModMenuTypes.COMMANDER.get(), id);
        this.robots = robots;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return true;
    }

    public Collection<LivingEntity> getRobots() {
        return robots;
    }
}
