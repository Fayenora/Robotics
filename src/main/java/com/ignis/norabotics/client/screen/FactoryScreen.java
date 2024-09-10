package com.ignis.norabotics.client.screen;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.client.screen.base.BaseContainerScreen;
import com.ignis.norabotics.client.screen.elements.ButtonElement;
import com.ignis.norabotics.client.screen.elements.EnergyBarElement;
import com.ignis.norabotics.client.tooltips.ItemTooltip;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.content.blockentity.FactoryBlockEntity;
import com.ignis.norabotics.common.content.menu.FactoryMenu;
import com.ignis.norabotics.common.helpers.util.Lang;
import com.ignis.norabotics.common.helpers.util.RenderUtil;
import com.ignis.norabotics.common.helpers.util.StringUtil;
import com.ignis.norabotics.common.robot.EnumRobotPart;
import com.ignis.norabotics.integration.config.RoboticsConfig;
import com.ignis.norabotics.network.messages.NetworkInfo;
import com.ignis.norabotics.network.messages.server.PacketComponentAction;
import com.ignis.norabotics.network.messages.server.PacketConstructRobot;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;
import java.util.*;

@ParametersAreNonnullByDefault
public class FactoryScreen extends BaseContainerScreen<FactoryMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_factory.png");

    public ButtonElement startButton;
    public ButtonElement switchColorLeft, switchColorRight;
    public EditBox nameBar;
    FactoryBlockEntity factory;

    private final Map<Rectangle, EnumRobotPart> hoverableParts = new HashMap<>();

    public FactoryScreen(FactoryMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        imageWidth = Reference.GUI_ROBOT_FACTORY_DIMENSIONS.width;
        imageHeight = Reference.GUI_ROBOT_FACTORY_DIMENSIONS.height;
        this.factory = menu.blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        factory.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            addElement(new EnergyBarElement(energy, leftPos + 8, topPos + 8, 203));
        });

        startButton = new ButtonElement(leftPos + 100, topPos + 180, 54, 19, Lang.localise("start"), button -> {
            if(factory.hasCraftedRobotReady()) {
                nameBar.setValue("");
            }
        });
        startButton.initTextureLocation(Reference.MISC, 94, 34);
        startButton.setNetworkAction(() -> new PacketConstructRobot(factory.getBlockPos(), nameBar.getValue()));
        switchColorLeft = new ButtonElement(leftPos + 83, topPos + 181, 13, 17, button -> {
            factory.getEntity().ifPresent(ent -> ent.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                DyeColor colorLeft = DyeColor.byId(Math.floorMod(parts.getColor().getId() + 1, 16));
                parts.setColor(colorLeft);
            }));
        });
        switchColorLeft.initTextureLocation(Reference.MISC, 51, 170);
        switchColorLeft.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_COLOR_LEFT, new NetworkInfo(factory.getBlockPos())));
        switchColorRight = new ButtonElement(leftPos + 158, topPos + 181, 13, 17, button -> {
            factory.getEntity().ifPresent(ent -> ent.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                DyeColor colorRight = DyeColor.byId(Math.floorMod(parts.getColor().getId() - 1, 16));
                parts.setColor(colorRight);
            }));
        });
        switchColorRight.initTextureLocation(Reference.MISC, 51, 187);
        switchColorRight.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_COLOR_RIGHT, new NetworkInfo(factory.getBlockPos())));
        nameBar = new EditBox(Minecraft.getInstance().font, leftPos + 91, topPos + 17, 70, 16, Component.empty());
        nameBar.setMaxLength(Reference.MAX_ROBOT_NAME_LENGTH);
        if(factory.getEntity().isPresent() && factory.getEntity().get().hasCustomName()) {
            nameBar.setValue(factory.getEntity().get().getCustomName().getString());
        }

        addElement(startButton);
        addElement(switchColorLeft);
        addElement(switchColorRight);
        addElement(nameBar);

        hoverableParts.put(new Rectangle(leftPos + 110, topPos + 50, 30, 30), EnumRobotPart.HEAD);
        hoverableParts.put(new Rectangle(leftPos + 110, topPos + 80, 30, 45), EnumRobotPart.BODY);
        hoverableParts.put(new Rectangle(leftPos + 140, topPos + 80, 15, 45), EnumRobotPart.LEFT_ARM);
        hoverableParts.put(new Rectangle(leftPos + 95, topPos + 80, 15, 45), EnumRobotPart.RIGHT_ARM);
        hoverableParts.put(new Rectangle(leftPos + 125, topPos + 125, 15, 45), EnumRobotPart.LEFT_LEG);
        hoverableParts.put(new Rectangle(leftPos + 110, topPos + 125, 15, 45), EnumRobotPart.RIGHT_LEG);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        startButton.setEnabled(!menu.blockEntity.isRunning() && (factory.canStart() || (factory.hasCraftedRobotReady() && !nameBar.getValue().isEmpty())));
        switchColorLeft.setVisible(factory.getEntity().isPresent());
        switchColorRight.setVisible(factory.getEntity().isPresent());
        switchColorLeft.setEnabled(factory.getEntity().isPresent());
        switchColorRight.setEnabled(factory.getEntity().isPresent());

        if(factory.hasCraftedRobotReady()) {
            startButton.setMessage(Component.translatable("create"));
        } else startButton.setMessage(Component.translatable("start"));

        if(factory.getEntity().isEmpty() || !(factory.getEntity().get() instanceof LivingEntity living)) return;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderUtil.drawEntityOnScreen(graphics, leftPos + 127, topPos + 170, 60, 0, 0, living);
    }

    @Override
    public void onClose() {
        super.onClose();
        factory.getEntity().ifPresent(ent -> ent.setCustomName(Component.literal(nameBar.getValue())));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if(startButton.isHovered()) {
            List<Component> tooltip = new ArrayList<>();
            if(menu.blockEntity.isRunning()) {
                int remainingTime = menu.blockEntity.getRemainingTime();
                Component timeDisplay = Component.literal(": " + StringUtil.getTimeDisplay(remainingTime));
                tooltip.add(ComponentUtils.formatList(List.of(Lang.localise("remaining_time"), timeDisplay), Component.empty()));
            } else if(!startButton.isEnabled()) {
                String energyCostDisplay = StringUtil.getEnergyDisplay(RoboticsConfig.general.constructionEnergyCost.get());
                tooltip.add(Lang.localise("requires"));
                tooltip.add(requirement("name"));
                tooltip.add(requirement("energy", energyCostDisplay));
                tooltip.add(requirement("leg"));
                tooltip.add(requirement("body"));
                tooltip.add(requirement("head"));
            }
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }

        for(Rectangle r : hoverableParts.keySet()) {
            if(r.contains(mouseX, mouseY)) {
                ItemStack stack = menu.getSlot(hoverableParts.get(r).getID()).getItem();
                if(stack.isEmpty()) continue;
                graphics.renderTooltip(this.font, this.getTooltipFromContainerItem(stack), Optional.of(new ItemTooltip(stack)), stack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
        if(keyCode == InputConstants.KEY_ESCAPE) {
            return super.keyPressed(keyCode, pScanCode, pModifiers);
        }
        if(nameBar.isFocused()) {
            return nameBar.keyPressed(keyCode, pScanCode, pModifiers);
        }
        return super.keyPressed(keyCode, pScanCode, pModifiers);
    }

    private Component requirement(String localizationString, Object... args) {
        return ComponentUtils.formatList(List.of(Component.literal("- "), Lang.localise("requirement." + localizationString, args)), Component.empty());
    }

    @Override
    protected void renderLabels(GuiGraphics poseStack, int p_97809_, int p_97810_) {
        //Don't
    }
}
