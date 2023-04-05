package com.ignis.igrobotics.client;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.items.CommanderItem;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.RobotModule;
import com.ignis.igrobotics.core.robot.RobotPart;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class Tooltips {

    @SubscribeEvent
    public static void onToolTip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Level level = Minecraft.getInstance().level;
        if(RoboticsConfig.current().modules.isModule(stack.getItem())) {
            RobotModule module = RoboticsConfig.current().modules.get(stack);
            event.getToolTip().addAll(module.getPerks().getDisplayString());
        }
        RobotPart part = RobotPart.getFromItem(stack.getItem());
        if(part.getMaterial() != EnumRobotMaterial.NONE) {
            event.getToolTip().addAll(part.getPerks().getDisplayString());
        }
        if(stack.getItem() instanceof CommanderItem) {
            event.getToolTip().add(Component.literal("ID: " + CommanderItem.getID(stack)));
            Entity entity = CommanderItem.getRememberedEntity(level, stack);
            if(entity != null) {
                List<Component> tooltip = List.of(Lang.localise("selected_robot"), Component.literal(": "), entity.getName());
                event.getToolTip().add(ComponentUtils.formatList(tooltip, Component.empty()));
            }
            if(CommanderItem.getRememberedPos(stack) != null) {
                List<Component> tooltip = List.of(Lang.localise("selected_pos"), Component.literal(": " + CommanderItem.getRememberedPos(stack)));
                event.getToolTip().add(ComponentUtils.formatList(tooltip, Component.empty()));
            }
        }
    }
}
