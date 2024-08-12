package com.ignis.norabotics.client.screen.elements;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.client.screen.RobotCommandScreen;
import com.ignis.norabotics.client.screen.selectors.SelectorElement;
import com.ignis.norabotics.common.helpers.types.Selection;
import com.ignis.norabotics.common.helpers.types.SelectionType;
import com.ignis.norabotics.common.robot.RobotCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class CommandElement extends ButtonElement {

    public static final ResourceLocation TEXTURE = RobotCommandScreen.TEXTURE;
    public static final int OFFSET_FROM_TEXT = 2;
    public static final int TYPICAL_SELECTOR_WIDTH = 18;

    private final RobotCommand command;
    private final Font font;

    public CommandElement(RobotCommand command, int x, int y, Button.OnPress onPress) {
        super(x, y, 116, 25, onPress);
        this.command = command;
        font = Minecraft.getInstance().font;
        initSingleTextureLocation(TEXTURE, 9, 182);
        int i = 0;
        for(Selection<?> selection : command.getSelectors()) {
            Optional<SelectorElement<?>> selector = SelectionType.createSelectionGui(selection, getX() + getOffsetToSelector(i++), getY() + 3);
            selector.ifPresent(this::addElement);
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
        int i = 0;
        for(Component comp : command.getDescription()) {
            graphics.drawString(font, comp, getX() + getOffsetToText(i++), getY() + 9, Reference.FONT_COLOR);
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
