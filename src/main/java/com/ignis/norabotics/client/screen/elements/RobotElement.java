package com.ignis.norabotics.client.screen.elements;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.IRobot;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.content.items.CommanderItem;
import com.ignis.norabotics.common.helpers.types.EntitySearch;
import com.ignis.norabotics.common.helpers.types.Selection;
import com.ignis.norabotics.common.helpers.util.Lang;
import com.ignis.norabotics.common.helpers.util.PosUtil;
import com.ignis.norabotics.common.helpers.util.RenderUtil;
import com.ignis.norabotics.common.robot.RobotCommand;
import com.ignis.norabotics.common.robot.RobotView;
import com.ignis.norabotics.definitions.ModCommands;
import com.ignis.norabotics.definitions.ModMenuTypes;
import com.ignis.norabotics.network.messages.NetworkInfo;
import com.ignis.norabotics.network.messages.server.PacketAddCommand;
import com.ignis.norabotics.network.messages.server.PacketComponentAction;
import com.ignis.norabotics.network.messages.server.PacketOpenRobotMenu;
import com.ignis.norabotics.network.messages.server.PacketReleaseFromCommandGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class RobotElement extends ButtonElement {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/command_module.png");
    private static final int DEFAULT_FOLLOW_RANGE = 4;

    RobotView view;
    LivingEntity robot;
    IRobot robotics;
    IEnergyStorage energy;

    private ButtonElement moveHere, powerButton;

    public RobotElement(RobotView view, int pX, int pY) {
        super(pX, pY, 147, 26);
        initSingleTextureLocation(TEXTURE, 0, 183);
        this.view = view;

        ButtonElement removeButton = new ButtonElement(getX() + 135, getY() + 13, 8, 7, b -> {
            if(getParentComponent() instanceof ScrollableElement list) {
                list.removeComponent(this);
            }
        });
        removeButton.initSingleTextureLocation(Reference.MISC, 230, 0);
        Player player = Robotics.proxy.getPlayer();
        InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(player, item -> item instanceof CommanderItem);
        int currentGroup = CommanderItem.getID(player.getItemInHand(hand));
        removeButton.setNetworkAction(() -> new PacketReleaseFromCommandGroup(currentGroup, view.getUUID()));
        addElement(removeButton);

        if(!(view.getEntity() instanceof LivingEntity living)) return;
        this.robot = living;
        robot.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> this.energy = energy);
        if(!robot.getCapability(ModCapabilities.ROBOT).isPresent()) return;
        robotics = robot.getCapability(ModCapabilities.ROBOT).resolve().get();
        setNetworkAction(() -> new PacketOpenRobotMenu(ModMenuTypes.ROBOT.get(), robot.getId()));

        if(view.getState() == RobotView.RobotState.IN_STORAGE) {
            ButtonElement exitStorage = new ButtonElement(getX() + 78, getY() + 4, 17, 17);
            exitStorage.initTextureLocation(Reference.MISC, 51, 102);
            exitStorage.setTooltip(Lang.localise("button.exit.storage"));
            // TODO Robot could be in another level / chunk could be not loaded
            exitStorage.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_FACTORY_BUTTON, new NetworkInfo(view.getLastKnownPosition().pos())));
            addElement(exitStorage);
        } else {
            moveHere = new ButtonElement(getX() + 78, getY() + 4, 17, 17);
            moveHere.initTextureLocation(Reference.MISC, 0, 102);
            moveHere.setTooltip(Lang.localise("button.move.here"));
            UUID playerUUID = player.getUUID();
            var selectionPlayer = Selection.of(new EntitySearch(playerUUID));
            var selectionFollowRange = Selection.of(DEFAULT_FOLLOW_RANGE);
            moveHere.setNetworkAction(() -> new PacketAddCommand(robot.getId(), new RobotCommand(ModCommands.FOLLOW.get(), List.of(selectionPlayer, selectionFollowRange))));
            addElement(moveHere);
        }

        powerButton = new ButtonElement(getX() + 98, getY() + 4, 17, 17, robotics.isActive() ? 1 : 0, 2, b -> {
            if(b instanceof ButtonElement button) {
                boolean newState = button.getState() == 1;
                view.setActive(newState);
                this.robot.getCapability(ModCapabilities.ROBOT).ifPresent(robotics2 -> robotics2.setActivation(newState));
            }
        });
        powerButton.initTextureLocation(Reference.MISC, 0, 204);
        powerButton.setTooltip(0, Lang.localise("button.power.up"));
        powerButton.setTooltip(1, Lang.localise("button.power.down"));
        powerButton.setNetworkAction(() -> new PacketComponentAction(PacketComponentAction.ACTION_POWER_STATE, new NetworkInfo(robot)));
        addElement(powerButton);

        if(energy != null && view.getState() != RobotView.RobotState.OFFLINE) {
            EnergyBarElement energyBar = new EnergyBarElement(energy, getX() + 117, getY() + 2, 20);
            addElement(energyBar);
        } else {
            ButtonElement unknownEnergy = new ButtonElement(getX() + 117, getY() + 2, 13, 19);
            unknownEnergy.setTooltip(Lang.localise("unknown"));
            addElement(unknownEnergy);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        moveHere.setEnabled(view.getState() == RobotView.RobotState.IN_WORLD && robotics != null && robotics.isActive());
        powerButton.setEnabled(view.getState() == RobotView.RobotState.IN_WORLD && energy != null && energy.getEnergyStored() > 0);

        graphics.setColor(1, 1, 1, 1);
        InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, getX() + 10, getY() + 23, 10, 0, 0, robot);
        RenderUtil.drawString(graphics, Lang.localise(view.getState().toString()), getX() + 46, getY() + 3, Reference.FONT_COLOR, 0.6f);
        RenderUtil.drawString(graphics, PosUtil.prettyPrint(view.getLastKnownPosition().dimension()), getX() + 46, getY() + 9, Reference.FONT_COLOR, 0.6f);
        RenderUtil.drawString(graphics, PosUtil.prettyPrint(view.getLastKnownPosition().pos()), getX() + 46, getY() + 15, Reference.FONT_COLOR, 0.6f);
        if(robot == null) return;
        RenderUtil.drawString(graphics, String.valueOf(robot.getHealth()), getX() + 19, getY() + 4, Reference.FONT_COLOR, 0.8f);
        graphics.blit(Reference.ICONS, getX() + 35, getY() + 2, 16, 0, 9, 9);
        graphics.blit(Reference.ICONS, getX() + 35, getY() + 2, 52, 0, 9, 9);
        RenderUtil.drawString(graphics, String.valueOf(robot.getArmorValue()), getX() + 19, getY() + 15, Reference.FONT_COLOR, 0.8f);
        graphics.blit(Reference.ICONS,  getX() + 35, getY() + 13, 34, 9, 9, 9);
    }
}
