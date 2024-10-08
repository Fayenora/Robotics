package com.io.norabotics.client;

import com.io.norabotics.Robotics;
import com.io.norabotics.client.screen.FactoryScreen;
import com.io.norabotics.client.tooltips.ModuleTooltip;
import com.io.norabotics.common.capabilities.impl.EnergyStorage;
import com.io.norabotics.common.content.actions.IAction;
import com.io.norabotics.common.content.items.CommanderItem;
import com.io.norabotics.common.helpers.util.Lang;
import com.io.norabotics.common.helpers.util.PosUtil;
import com.io.norabotics.common.helpers.util.StringUtil;
import com.io.norabotics.common.robot.EnumModuleSlot;
import com.io.norabotics.common.robot.RobotModule;
import com.io.norabotics.common.robot.RobotPart;
import com.io.norabotics.definitions.ModSounds;
import com.io.norabotics.definitions.robotics.ModModules;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    public static final String BLOCK_DATA_TAG = "BlockEntityTag";

    public static int soundCooldown = 0;

    private static final Component componentAction = ComponentUtils.formatList(List.of(Lang.localise("module.action").withStyle(Lang.AQUA),
                                                                                        Component.literal(": ").withStyle(Lang.AQUA)), Component.empty());

    @SubscribeEvent
    public static void onToolTip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Level level = Minecraft.getInstance().level;
        if(ModModules.isModule(stack)) {
            RobotModule module = ModModules.get(stack);
            if(!module.getViableSlots().isEmpty()) {
                Component tooltipSlots = ComponentUtils.formatList(List.of(Lang.localise("module.slots"), Component.literal(": "), Lang.localiseAll(module.getViableSlots())), Component.empty());
                event.getToolTip().add(tooltipSlots);
            }
            if(module.getAction() != IAction.NO_ACTION) {
                Component tooltipAction = ComponentUtils.formatList(List.of(componentAction, Lang.localise("action." + module.getAction().toString().toLowerCase()).withStyle(Lang.color(module.getAction().getColor()))), Component.empty());
                event.getToolTip().add(tooltipAction);
            }
            event.getToolTip().addAll(module.getPerks().getDisplayString());
        }
        if(stack.getItem() instanceof CommanderItem) {
            event.getToolTip().add(Component.literal("ID: " + CommanderItem.getID(stack)));
            Entity entity = CommanderItem.getRememberedEntity(level, stack);
            GlobalPos pos = CommanderItem.getRememberedPos(stack);
            if(entity != null) {
                List<Component> tooltip = List.of(Lang.localise("selected_robot"), Component.literal(": "), entity.getName());
                event.getToolTip().add(ComponentUtils.formatList(tooltip, Component.empty()));
            }
            if(pos != null) {
                List<Component> tooltip = List.of(Lang.localise("selected_pos"), PosUtil.prettyPrint(pos));
                event.getToolTip().add(ComponentUtils.formatList(tooltip, Component.literal(": ")));
            }
        }
        if(stack.hasTag() && stack.getTag().contains(BLOCK_DATA_TAG, Tag.TAG_COMPOUND) && stack.getTag().getCompound(BLOCK_DATA_TAG).contains("energy", Tag.TAG_COMPOUND)) {
            CompoundTag energyTag = stack.getTag().getCompound(BLOCK_DATA_TAG).getCompound("energy");
            EnergyStorage storage = new EnergyStorage(0);
            storage.deserializeNBT(energyTag);
            List<Component> tooltip = List.of(Lang.localise("stored_energy"), Component.literal(
                    StringUtil.getEnergyDisplay(storage.getEnergyStored()) + " / " +
                    StringUtil.getEnergyDisplay(storage.getMaxEnergyStored())).withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(ComponentUtils.formatList(tooltip, Component.literal(": ")));
        }
    }

    @SubscribeEvent
    public static void renderFancyTooltip(RenderTooltipEvent.Color event) {
        if(Robotics.proxy.getScreen().isEmpty()) return;
        if(!(Robotics.proxy.getScreen().get() instanceof FactoryScreen)) return;
        event.setBackgroundStart(FastColor.ARGB32.color(50, 0, 150, 150));
        event.setBackgroundEnd(FastColor.ARGB32.color(50, FastColor.ARGB32.red(event.getOriginalBackgroundEnd()), FastColor.ARGB32.green(event.getOriginalBackgroundEnd()), FastColor.ARGB32.blue(event.getOriginalBackgroundEnd())));
        event.setBorderStart(FastColor.ARGB32.color(255, 0, 255, 255));
        event.setBorderEnd(FastColor.ARGB32.color(255, 0, 255, 255));
    }

    @SubscribeEvent
    public static void renderFancyTooltip(RenderTooltipEvent.GatherComponents event) {
        if(Robotics.proxy.getScreen().isEmpty()) return;
        if(!(Robotics.proxy.getScreen().get() instanceof FactoryScreen screen)) return;

        RobotPart part = RobotPart.getFromItem(event.getItemStack().getItem());
        if(part == null) return;
        Map<EnumModuleSlot, NonNullList<ItemStack>> moduleAssignment = screen.getMenu().getModuleAssignmentOfPart(part.getPart());
        for(Map.Entry<EnumModuleSlot, NonNullList<ItemStack>> entry : moduleAssignment.entrySet()) {
            event.getTooltipElements().add(Either.right(new ModuleTooltip(entry.getKey(), entry.getValue())));
        }
    }

    @SubscribeEvent
    public static void onServerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        RegistryAccess registryAccess = event.getPlayer().level().registryAccess();
        ModModules.reloadModules(registryAccess);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onSound(PlaySoundEvent event) {
        --soundCooldown;
        if(event.getSound() == null) return;
        if(event.getSound().getLocation().equals(ModSounds.ROBOT_KILL_COMMAND.get().getLocation())) {
            if(soundCooldown > 0) {
                event.setSound(null);
            } else {
                //For very low values of sound cooldown we can assume some time has passed last hearing a robot
                // => We can set a low cooldown
                //If the soundCooldown is a lower value we want to add some more cooldown, depending on how low it is
                soundCooldown = (int) (15 + Math.exp(Math.min(0, soundCooldown + 10)));
            }
        }
    }
}
