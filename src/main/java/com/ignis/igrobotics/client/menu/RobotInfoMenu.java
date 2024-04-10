package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.core.access.AccessConfig;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class RobotInfoMenu extends BaseMenu {
    public final LivingEntity robot;
    public final ContainerData data;
    public final AccessConfig access = new AccessConfig();
    public final HashMap<Attribute, Float> attributes = new HashMap<>();

    public RobotInfoMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv.player.level().getEntity(extraData.readInt()), new SimpleContainerData(2));
        access.read(extraData);
        while(extraData.isReadable()) {
            ResourceKey<Attribute> key = extraData.readResourceKey(ForgeRegistries.ATTRIBUTES.getRegistryKey());
            float value = extraData.readFloat();
            attributes.put(ForgeRegistries.ATTRIBUTES.getValue(key.location()), value);
        }
    }

    public RobotInfoMenu(int id, Entity entity, ContainerData data) {
        super(ModMenuTypes.ROBOT_INFO.get(), id);
        this.robot = (LivingEntity) entity;
        this.data = data;

        addDataSlots(data);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int quickMovedSlotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return !robot.isDeadOrDying();
    }
}
