package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.RobotCommandMenu;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.client.screen.elements.CommandElement;
import com.ignis.igrobotics.client.screen.elements.ScrollableElement;
import com.ignis.igrobotics.client.screen.elements.SideBarSwitchElement;
import com.ignis.igrobotics.common.handlers.RobotBehavior;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.CommandType;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.definitions.ModCommands;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.server.PacketSetCommands;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class RobotCommandScreen extends BaseContainerScreen<RobotCommandMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_commands.png");

    private final LivingEntity entity;

    ScrollableElement selectedCommands;
    ScrollableElement availableCommands;

    public RobotCommandScreen(RobotCommandMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        entity = menu.robot;
        imageWidth = Reference.GUI_ROBOT_COMMANDS_DIMENSION.width;
        imageHeight = Reference.GUI_ROBOT_COMMANDS_DIMENSION.height;
    }

    @Override
    protected void init() {
        super.init();
        if(entity == null) return;
        SideBarSwitchElement sidebar = new SideBarSwitchElement(ModMenuTypes.ROBOT_COMMANDS.get(), RobotBehavior.possibleMenus(entity), leftPos + imageWidth - 1, topPos + 3, 18, 17, entity.getId());
        sidebar.initTextureLocation(SideBarSwitchElement.DEFAULT_TEXTURE);
        addRenderableWidget(sidebar);
        selectedCommands = new ScrollableElement(leftPos + 9, topPos + 9, 116, 165);
        availableCommands = new ScrollableElement(leftPos + 132, topPos + 9, 116, 165);

        //Available Commands
        List<CommandType> availableTypes = new ArrayList<>();
        for(String typeString : RoboticsConfig.general.availableCommands.get()) {
            availableTypes.add(ModCommands.byName(typeString));
        }
        for(CommandType commandType : ModCommands.COMMAND_TYPES) {
            RobotCommand command = new RobotCommand(commandType);
            if(!availableTypes.contains(commandType)) continue;
            availableCommands.addElement(new CommandElement(command, 0, 0, button -> {
                selectedCommands.addElement(new CommandElement(command.clone(), 0, 0, button1 -> {
                    selectedCommands.removeComponent((IElement) button1);
                }));
            }));
        }
        //Selected Commands
        entity.getCapability(ModCapabilities.COMMANDS).ifPresent(robot -> {
            for(RobotCommand command : robot.getCommands()) {
                selectedCommands.addElement(new CommandElement(command, 0, 0, button -> {
                    selectedCommands.removeComponent((IElement) button);
                }));
            }
        });
        addElement(selectedCommands);
        addElement(availableCommands);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        //NO-OP
    }

    @Override
    public void removed() {
        if(entity == null) {
            super.removed();
            return;
        }

        List<RobotCommand> commands = new ArrayList<>();
        for(var child : selectedCommands.children()) {
            if(child instanceof CommandElement commandElement) {
                commands.add(commandElement.getCommand());
            }
        }

        entity.getCapability(ModCapabilities.COMMANDS).ifPresent(robot -> {
            //In the case the client has not been informed about current commands (client side robot does not contain commands)
            //prevent it from deleting them immediatly by closing the gui
            //NOTE: If a player manages to add a command in the time it takes to inform him, server-side commands will be overwritten
            if(robot.getCommands().isEmpty() && commands.isEmpty()) return;
            NetworkHandler.sendToServer(new PacketSetCommands(entity.getId(), commands));
        });
        super.removed();
    }
}
