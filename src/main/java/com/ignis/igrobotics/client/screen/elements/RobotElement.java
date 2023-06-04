package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.network.messages.server.PacketOpenRobotMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RobotElement extends ButtonElement {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/command_module.png");

    LivingEntity robot;

    public RobotElement(LivingEntity robot, int pX, int pY) {
        super(pX, pY, 147, 26);
        this.robot = robot;
        initSingleTextureLocation(TEXTURE, 0, 183);
        setNetworkAction(() -> new PacketOpenRobotMenu(ModMenuTypes.ROBOT.get(), robot.getId()));
    }

    @Override
    public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        InventoryScreen.renderEntityInInventoryFollowsAngle(poseStack, getX() + 10, getY() + 23, 10, 0, 0, robot);
    }
}
