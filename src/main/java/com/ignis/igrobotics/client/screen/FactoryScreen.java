package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.FactoryMenu;
import com.ignis.igrobotics.client.screen.base.BaseContainerScreen;
import com.ignis.igrobotics.client.screen.elements.ButtonElement;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.common.blockentity.MachineBlockEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.ignis.igrobotics.core.util.StringUtil;
import com.ignis.igrobotics.network.messages.NetworkInfo;
import com.ignis.igrobotics.network.messages.server.PacketComponentAction;
import com.ignis.igrobotics.network.messages.server.PacketConstructRobot;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

public class FactoryScreen extends BaseContainerScreen<FactoryMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot_factory.png");

    public ButtonElement startButton;
    public ButtonElement switchColorLeft, switchColorRight;
    public EditBox nameBar;
    FactoryBlockEntity factory;

    public FactoryScreen(FactoryMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        imageWidth = Reference.GUI_ROBOTFACTORY_DIMENSIONS.width;
        imageHeight = Reference.GUI_ROBOTFACTORY_DIMENSIONS.height;
        this.factory = menu.blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new EnergyBarElement(leftPos + 8, topPos + 8, 203, () -> menu.data.get(3), () -> menu.data.get(4)));

        startButton = new ButtonElement(leftPos + 89, topPos + 112, 54, 19, Lang.localise("start"), button -> {});
        startButton.initTextureLocation(Reference.MISC, 94, 34);
        startButton.setNetworkAction(() -> new PacketConstructRobot(factory.getBlockPos(), nameBar.getValue()));
        switchColorLeft = new ButtonElement(leftPos + 75, topPos + 62, 13, 17, button -> {
            factory.getEntity().ifPresent(ent -> ent.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                DyeColor colorLeft = DyeColor.byId(Math.floorMod(parts.getColor().getId() + 1, 16));
                parts.setColor(colorLeft);
            }));
        });
        switchColorLeft.initTextureLocation(Reference.MISC, 51, 170);
        switchColorLeft.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_COLOR_LEFT, new NetworkInfo(factory.getBlockPos())));
        switchColorRight = new ButtonElement(leftPos + 143, topPos + 62, 13, 17, button -> {
            factory.getEntity().ifPresent(ent -> ent.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                DyeColor colorRight = DyeColor.byId(Math.floorMod(parts.getColor().getId() - 1, 16));
                parts.setColor(colorRight);
            }));
        });
        switchColorRight.initTextureLocation(Reference.MISC, 51, 187);
        switchColorRight.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_COLOR_RIGHT, new NetworkInfo(factory.getBlockPos())));
        nameBar = new EditBox(Minecraft.getInstance().font, leftPos + 80, topPos + 17, 70, 16, Component.empty());
        nameBar.setMaxLength(Reference.MAX_ROBOT_NAME_LENGTH);
        if(factory.getEntity().isPresent() && factory.getEntity().get().hasCustomName()) {
            nameBar.setValue(factory.getEntity().get().getCustomName().getString());
        }

        addElement(startButton);
        addElement(switchColorLeft);
        addElement(switchColorRight);
        addRenderableWidget(nameBar);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        startButton.setEnabled(!MachineBlockEntity.isRunning(menu.data) && (factory.canStart() || (factory.hasCraftedRobotReady() && !nameBar.getValue().isEmpty())));
        switchColorLeft.setVisible(factory.getEntity().isPresent());
        switchColorRight.setVisible(factory.getEntity().isPresent());
        switchColorLeft.setEnabled(factory.getEntity().isPresent());
        switchColorRight.setEnabled(factory.getEntity().isPresent());

        if(factory.hasCraftedRobotReady()) {
            startButton.setMessage(Component.translatable("create"));
        } else startButton.setMessage(Component.translatable("start"));

        if(factory.getEntity().isEmpty() || !(factory.getEntity().get() instanceof LivingEntity living)) return;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderUtil.drawEntityOnScreen(poseStack, leftPos + 116, topPos + 105, 30, 0, 0, living);

        //Draw Module Slots
        RenderSystem.setShaderTexture(0, Reference.MISC);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for(int i = 6; i < menu.data.get(0); i++) {
            blit(poseStack, leftPos + 206, topPos + 16 + 23 * (i - 6), 238, 0, 18, 18);
        }

		/* Draw Color Icon
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(this.guiLeft + 108, this.guiTop + 18, 0, 238, 17, 17);
		Color robotColor = new Color(robot.data.getColor().getColorValue(), false);
		GlStateManager.color(robotColor.getRed() / 255f, robotColor.getGreen() / 255f, robotColor.getBlue() / 255f);
		drawTexturedModalRect(this.guiLeft + 108, this.guiTop + 18, 17, 238, 17, 17);
		*/
    }

    @Override
    public void onClose() {
        super.onClose();
        factory.getEntity().ifPresent(ent -> ent.setCustomName(Component.literal(nameBar.getValue())));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderTooltip(poseStack, mouseX, mouseY);
        if(startButton.isHoveredOrFocused()) {
            List<Component> tooltip = new ArrayList<>();
            if(MachineBlockEntity.isRunning(menu.data)) {
                Component timeDisplay = Component.literal(": " + StringUtil.getTimeDisplay(menu.data.get(1) - menu.data.get(2)));
                tooltip.add(ComponentUtils.formatList(List.of(Lang.localise("remaining_time"), timeDisplay), Component.empty()));
            } else if(!startButton.isEnabled()) {
                tooltip.add(Lang.localise("requires"));
                tooltip.add(requirement("name"));
                tooltip.add(requirement("energy", StringUtil.getEnergyDisplay(FactoryBlockEntity.ENERGY_COST)));
                tooltip.add(requirement("leg"));
                tooltip.add(requirement("body"));
                tooltip.add(requirement("head"));
            }
            renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
        }
    }

    private Component requirement(String localizationString, Object... args) {
        return ComponentUtils.formatList(List.of(Component.literal("- "), Lang.localise("requirement." + localizationString, args)), Component.empty());
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        //Don't
    }
}
