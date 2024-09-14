package com.io.norabotics.client.screen;

import com.io.norabotics.Reference;
import com.io.norabotics.Robotics;
import com.io.norabotics.client.screen.elements.EnergyBarElement;
import com.io.norabotics.client.screen.elements.SideBarSwitchElement;
import com.io.norabotics.common.capabilities.IPartBuilt;
import com.io.norabotics.common.capabilities.IRobot;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.content.menu.RobotMenu;
import com.io.norabotics.common.handlers.RobotBehavior;
import com.io.norabotics.common.helpers.util.MathUtil;
import com.io.norabotics.common.helpers.util.RenderUtil;
import com.io.norabotics.common.robot.EnumModuleSlot;
import com.io.norabotics.common.robot.EnumRobotPart;
import com.io.norabotics.definitions.ModMenuTypes;
import com.io.norabotics.integration.config.RoboticsConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;

@ParametersAreNonnullByDefault
public class RobotScreen extends EffectRenderingRobotScreen<RobotMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot.png");

    private final LivingEntity entity;

    private IPartBuilt entityParts;
    private IRobot robot;

    public RobotScreen(RobotMenu menu, Inventory inv, Component comp) {
        super(menu, inv, menu.robot, comp);
        this.entity = menu.robot;
        entity.getCapability(ModCapabilities.PARTS).ifPresent(parts -> this.entityParts = parts);
        entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> this.robot = robot);
        imageHeight = Reference.GUI_ROBOT_DIMENSIONS.height;
    }

    @Override
    protected void init() {
        super.init();
        SideBarSwitchElement sidebar = new SideBarSwitchElement(ModMenuTypes.ROBOT.get(), RobotBehavior.possibleMenus(entity), menu.access, leftPos + imageWidth - 1, topPos + 3, 18, 17, entity.getId());
        sidebar.initTextureLocation(SideBarSwitchElement.DEFAULT_TEXTURE);
        addElement(sidebar);
        entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            addElement(new EnergyBarElement(energy, leftPos + 155, topPos + 7, 71));
        });
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float p_97788_, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        graphics.setColor(1, 1, 1, 1);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if(entity == null) return;

        this.drawHealthBar(graphics, 7, 81, Math.round(entity.getHealth()), Math.round(entity.getMaxHealth()));
        this.drawArmor(graphics, 89, 81, entity.getArmorValue());

        if(entityParts == null || robot == null) return;

        graphics.setColor(1, 1, 1, 1);
        if(entityParts.hasBodyPart(EnumModuleSlot.RIGHT_ARM)) {
            graphics.blit(Reference.MISC, leftPos + 76, topPos + 43, 238, 0, 18, 18);
        }
        if(entityParts.hasBodyPart(EnumModuleSlot.LEFT_ARM)) {
            graphics.blit(Reference.MISC, leftPos + 76, topPos + 61, 238, 0, 18, 18);
        }
        entity.getCapability(ModCapabilities.SHIELDED).ifPresent(shield -> {
            this.drawShieldBar(graphics, 7, 81, Math.round(shield.getHealth()), entityParts.getColor().getTextColor());
        });

        if(robot.isActive()) {
            RenderUtil.drawEntityOnScreen(graphics, leftPos + 25, topPos + 7, mouseX, mouseY, 30, false, entity);
        } else {
            RenderUtil.drawInactiveRobot(graphics, leftPos + 25, topPos + 7, 30, entity, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int p_97809_, int p_97810_) {
        //Don't
    }

    public void drawHealthBar(GuiGraphics graphics, int x, int y, int health, int maxHealth) {
        graphics.setColor(1, 1, 1, 1);

        //Health canisters
        int maxHealthToDraw = Math.min(20, maxHealth);
        for(int i = 0; i < maxHealthToDraw / 2; i++) {
            graphics.blit(Reference.MISC, this.leftPos + x + i * 8, this.topPos + y, 241, 18, 9, 9);
        }
        if(maxHealthToDraw % 2 == 1) {
            graphics.blit(Reference.MISC, this.leftPos + x + (maxHealthToDraw - 1)/2 * 8, this.topPos + y, 250, 18, 6, 9);
        }

        //Health for previous bar
        int healthToDraw = health % 20;
        List<? extends Integer> heartColors = RoboticsConfig.client.heartColors.get();
        int heartColor = heartColors.get(Math.floorDiv(health, 20) % heartColors.size());
        if(health >= 20) {
            int backgroundHeartColor = heartColors.get((Math.floorDiv(health, 20) - 1) % heartColors.size());
            setColor(backgroundHeartColor);
            for(int i = 0; i < 10; i++) {
                graphics.blit(Reference.MISC, this.leftPos + x + i * 8, this.topPos + y, 232, 18, 9, 9);
            }
        }
        //Any Health on top
        setColor(heartColor);
        for(int i = 0; i < healthToDraw / 2; i++) {
            graphics.blit(Reference.MISC, this.leftPos + x + i * 8, this.topPos + y, 232, 18, 9, 9);
        }
        if(healthToDraw % 2 == 1) {
            graphics.blit(Reference.MISC, this.leftPos + x + (healthToDraw - 1)/2 * 8, this.topPos + y, 232, 18, 5, 9);
        }
    }

    public void drawArmor(GuiGraphics graphics, int x, int y, int armor) {
        if(armor == 0) return;
        graphics.setColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, Reference.ICONS);

        //Always draw all empty armor icons
        for(int i = 0; i < 10; i++) {
            graphics.blit(Reference.ICONS, this.leftPos + x + i * 8, this.topPos + y, 16, 9, 9, 9);
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
                graphics.blit(Reference.MISC, this.leftPos + x + i * 8, this.topPos + y, 223, 18, 9, 9);
            }
        }
        //Then, fill in the rest of the armor value
        setColor(armorColor);
        for(int i = 0; i < armorToDraw / 2; i++) {
            graphics.blit(Reference.MISC, this.leftPos + x + i * 8, this.topPos + y, 223, 18, 9, 9);
        }
        if(armorToDraw % 2 == 1) {
            graphics.blit(Reference.MISC, this.leftPos + x + ((armorToDraw - 1)/ 2) * 8, this.topPos + y, 214, 18, 9, 9);
        }
    }

    public void drawShieldBar(GuiGraphics graphics, int x, int y, int shieldHealth, int energyColor) {
        RenderSystem.enableBlend();

        for(int i = 0; i < 10; i++) {
            int timesHeartOccupied = Math.floorDiv(shieldHealth, 20) + (shieldHealth % 20 >= (i + 1) * 2 ? 1 : 0);
            if(timesHeartOccupied <= 0) break;
            setColor(energyColor, MathUtil.asymptote(timesHeartOccupied, 0.3f, 0.8f));
            graphics.blit(Reference.MISC, this.leftPos + x + i * 8, this.topPos + y, 232, 18, 9, 9);
        }
        if(shieldHealth % 2 == 1) {
            float alphaLeft = MathUtil.asymptote(Math.floorDiv(shieldHealth, 20) + 1, 0.3f, 0.8f);
            setColor(energyColor, alphaLeft);
            graphics.blit(Reference.MISC, this.leftPos + x + (shieldHealth % 20 - 1)/2 * 8, this.topPos + y, 232, 18, 5, 9);
        }
    }

    private void setColor(int color) {
        setColor(color, 1);
    }

    private void setColor(int color, float alpha) {
        Color c = new Color(color);
        RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, alpha);
    }

}
