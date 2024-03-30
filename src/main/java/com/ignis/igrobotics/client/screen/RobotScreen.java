package com.ignis.igrobotics.client.screen;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.RobotMenu;
import com.ignis.igrobotics.client.screen.elements.EnergyBarElement;
import com.ignis.igrobotics.client.screen.elements.SideBarSwitchElement;
import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.util.MathUtil;
import com.ignis.igrobotics.core.util.RenderUtil;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;

@ParametersAreNonnullByDefault
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
        SideBarSwitchElement sidebar = new SideBarSwitchElement(ModMenuTypes.ROBOT.get(), RobotBehavior.possibleMenus(entity), leftPos + imageWidth - 1, topPos + 3, 18, 17, entity.getId());
        sidebar.initTextureLocation(SideBarSwitchElement.DEFAULT_TEXTURE);
        addRenderableWidget(sidebar);
        addRenderableWidget(new EnergyBarElement(leftPos + 155, topPos + 7, 71, () -> menu.data.get(0), () -> menu.data.get(1)));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if(entity == null) return;

        this.drawHealthBar(poseStack, 7, 81, Math.round(entity.getHealth()), Math.round(entity.getMaxHealth()));
        this.drawArmor(poseStack, 89, 81, entity.getArmorValue());

        RenderSystem.setShaderTexture(0, Reference.MISC);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
            if(robot.isActive()) {
                RenderUtil.drawEntityOnScreen(poseStack, leftPos + 25, topPos + 7, mouseX, mouseY, 30, false, entity);
            } else {
                RenderUtil.drawInactiveRobot(poseStack, leftPos + 25, topPos + 7, 30, entity);
            }
        });

        entity.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                if(parts.hasBodyPart(EnumRobotPart.RIGHT_ARM)) {
                    blit(poseStack, leftPos + 76, topPos + 43, 238, 0, 18, 18);
                }
                if(parts.hasBodyPart(EnumRobotPart.LEFT_ARM)) {
                    blit(poseStack, leftPos + 76, topPos + 61, 238, 0, 18, 18);
                }

                entity.getCapability(ModCapabilities.SHIELDED).ifPresent(shield -> {
                    this.drawShieldBar(poseStack, 7, 81, Math.round(shield.getHealth()), parts.getColor().getTextColor());
                });
            });
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
        RenderSystem.setShaderTexture(0, Reference.MISC);

        //Health canisters
        int maxHealthToDraw = Math.min(20, maxHealth);
        for(int i = 0; i < maxHealthToDraw / 2; i++) {
            blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 241, 18, 9, 9);
        }
        if(maxHealthToDraw % 2 == 1) {
            blit(poseStack, this.leftPos + x + (maxHealthToDraw - 1)/2 * 8, this.topPos + y, 250, 18, 6, 9);
        }

        //Health for previous bar
        int healthToDraw = health % 20;
        List<? extends Integer> heartColors = RoboticsConfig.client.heartColors.get();
        int heartColor = heartColors.get(Math.floorDiv(health, 20) % heartColors.size());
        if(health >= 20) {
            int backgroundHeartColor = heartColors.get((Math.floorDiv(health, 20) - 1) % heartColors.size());
            setColor(backgroundHeartColor);
            for(int i = 0; i < 10; i++) {
                blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 232, 18, 9, 9);
            }
        }
        //Any Health on top
        setColor(heartColor);
        for(int i = 0; i < healthToDraw / 2; i++) {
            blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 232, 18, 9, 9);
        }
        if(healthToDraw % 2 == 1) {
            blit(poseStack, this.leftPos + x + (healthToDraw - 1)/2 * 8, this.topPos + y, 232, 18, 5, 9);
        }
    }

    public void drawArmor(PoseStack poseStack, int x, int y, int armor) {
        if(armor == 0) return;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Reference.ICONS);

        //Always draw all empty armor icons
        for(int i = 0; i < 10; i++) {
            blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 16, 9, 9, 9);
        }
        //Armor in the previous bar
        RenderSystem.setShaderTexture(0, Reference.MISC);
        int armorToDraw = armor % 20;
        List<? extends Integer> armorColors = RoboticsConfig.client.armorColors.get();
        int armorColor = armorColors.get(Math.floorDiv(armor, 20) % armorColors.size());
        if(armor >= 20) {
            int backgroundArmorColor = armorColors.get((Math.floorDiv(armor, 20) - 1) % armorColors.size());
            setColor(backgroundArmorColor);
            for(int i = 0; i < 10; i++) {
                blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 223, 18, 9, 9);
            }
        }
        //Then, fill in the rest of the armor value
        setColor(armorColor);
        for(int i = 0; i < armorToDraw / 2; i++) {
            blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 223, 18, 9, 9);
        }
        if(armorToDraw % 2 == 1) {
            blit(poseStack, this.leftPos + x + ((armorToDraw - 1)/ 2) * 8, this.topPos + y, 214, 18, 9, 9);
        }
    }

    public void drawShieldBar(PoseStack poseStack, int x, int y, int shieldHealth, int energyColor) {
        RenderSystem.setShaderTexture(0, Reference.MISC);
        RenderSystem.enableBlend();

        for(int i = 0; i < 10; i++) {
            int timesHeartOccupied = Math.floorDiv(shieldHealth, 20) + (shieldHealth % 20 >= (i + 1) * 2 ? 1 : 0);
            if(timesHeartOccupied <= 0) break;
            setColor(energyColor, MathUtil.asymptote(timesHeartOccupied, 0.3f, 0.8f));
            blit(poseStack, this.leftPos + x + i * 8, this.topPos + y, 232, 18, 9, 9);
        }
        if(shieldHealth % 2 == 1) {
            float alphaLeft = MathUtil.asymptote(Math.floorDiv(shieldHealth, 20) + 1, 0.3f, 0.8f);
            setColor(energyColor, alphaLeft);
            blit(poseStack, this.leftPos + x + (shieldHealth % 20 - 1)/2 * 8, this.topPos + y, 232, 18, 5, 9);
        }
        RenderSystem.disableBlend();
    }

    private void setColor(int color) {
        setColor(color, 1);
    }

    private void setColor(int color, float alpha) {
        Color c = new Color(color);
        RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, alpha);
    }

}
