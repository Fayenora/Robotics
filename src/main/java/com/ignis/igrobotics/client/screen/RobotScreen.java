package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.RobotMenu;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

public class RobotScreen extends EffectRenderingRobotScreen<RobotMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot.png");

	private final LivingEntity entity;

    public RobotScreen(RobotMenu menu, Inventory inv, Component comp) {
        super(menu, inv, menu.robot, comp);
        this.entity = menu.robot;
        imageHeight = Reference.GUI_ROBOT_DIMENSIONS.height;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new EnergyBarElement(leftPos + 155, topPos + 7, 71, () -> menu.data.get(0), () -> menu.data.get(1)));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int mouseX, int mouseY) {
		if(entity == null) return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        RenderSystem.setShaderTexture(0, Reference.MISC);
        entity.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                if(parts.hasBodyPart(EnumRobotPart.RIGHT_ARM)) {
                    blit(poseStack, leftPos + 76, topPos + 43, 238, 0, 18, 18);
                }
                if(parts.hasBodyPart(EnumRobotPart.LEFT_ARM)) {
                    blit(poseStack, leftPos + 76, topPos + 61, 238, 0, 18, 18);
                }
            });

        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            if(robot.isActive()) {
                RenderUtil.drawEntityOnScreen(leftPos + 25, topPos + 7, mouseX, mouseY, 30, false, entity);
            } else {
                //TODO
                //RenderUtil.drawInactiveRobotOnScreen(this.guiLeft + 25, this.guiTop + 7, 30, robot);
            }
        });

		this.drawHealthBar(poseStack, 7, 81, Math.round(entity.getHealth()), Math.round(entity.getMaxHealth()));
		this.drawArmor(poseStack, 89, 81, entity.getArmorValue());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        //Don't
    }

	public void drawHealthBar(PoseStack poseStack, int x, int y, int health, int maxHealth) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Reference.ICONS);

		for(int i = 0; i < maxHealth / 2; i++) {
			blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 16, 0, 9, 9);
		}
		for(int i = 0; i < health / 2; i++) {
			blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 52, 0, 9, 9);
		}

		if(maxHealth % 2 == 1) {
            RenderSystem.setShaderTexture(0, Reference.MISC);
			blit(poseStack, this.leftPos + x + (maxHealth - 1)/2 * 8, this.topPos + y, 250, 18, 6, 9);
            RenderSystem.setShaderTexture(0, Reference.ICONS);
		}
		if(health % 2 == 1) {
			blit(poseStack, this.leftPos + x + (health - 1)/2 * 8, this.topPos + y, 52, 0, 5, 9);
		}
	}

	public void drawArmor(PoseStack poseStack, int x, int y, int armor) {
		if(armor == 0) return;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Reference.ICONS);

		for(int i = 0; i < 10; i++) {
			blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 16, 9, 9, 9);
		}
		for(int i = 0; i < armor / 2; i++) {
			blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 34, 9, 9, 9);
		}
		if(armor % 2 == 1) {
			blit(poseStack, this.leftPos + x + ((armor - 1)/ 2) * 8, this.topPos + y, 25, 9, 9, 9);
		}
	}

}
