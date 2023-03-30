package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.RobotCommandScreen;
import com.ignis.igrobotics.client.screen.selectors.SelectorElement;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.robot.SelectionType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class CommandElement extends ButtonElement {

    public static final ResourceLocation TEXTURE = RobotCommandScreen.TEXTURE;
    public static final int OFFSET_FROM_TEXT = 2;
    public static final int TYPICAL_SELECTOR_WIDTH = 18;

    private RobotCommand command;
    private Font font;

    public CommandElement(RobotCommand command, int x, int y, Button.OnPress onPress) {
        super(x, y, 116, 25, onPress);
        this.command = command;
        font = Minecraft.getInstance().font;
        initSingleTextureLocation(TEXTURE, 9, 182);
        int i = 0;
        for(Selection selection : command.getSelectors()) {
            Optional<SelectorElement<?>> selector = SelectionType.createSelectionGui(selection, getX() + getOffsetToSelector(i++), getY() + 3);
            selector.ifPresent(this::addElement);
        }
    }

    @Override
    public void renderButton(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderButton(poseStack, pMouseX, pMouseY, pPartialTick);
        int i = 0;
        for(Component comp : command.getDescription()) {
            drawString(poseStack, font, comp, getX() + getOffsetToText(i++), getY() + 9, Reference.FONT_COLOR);
        }
    }

    public RobotCommand getCommand() {
        return command;
    }

    private int getOffsetToSelector(int index) {
        return getOffsetToText(index + 1) - OFFSET_FROM_TEXT - TYPICAL_SELECTOR_WIDTH;
    }

    private int getOffsetToText(int index) {
        int offset = 5;
        for(int i = 0; i < index; i++) {
            offset += 2 * OFFSET_FROM_TEXT + TYPICAL_SELECTOR_WIDTH + font.width(command.getDescription().get(i).getString());
        }
        return offset;
    }
}
