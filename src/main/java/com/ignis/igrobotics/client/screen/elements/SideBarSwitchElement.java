package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.server.PacketOpenRobotMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class SideBarSwitchElement extends GuiElement {

    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/sidebar.png");
    private static final RegistryObject<MenuType<?>>[] POSSIBLE_TYPES = RobotBehavior.ALL_ROBOT_MENUS;

    /** Index of current menu inside all possible menus */
    int currentGuiIndex = 0;
    /** Index of current menu inside the currently active menus */
    int subsetGuiIndex = 0;
    /** For each button, saves at which row the designated texture is */
    int[] textureRows;

    public SideBarSwitchElement(MenuType currentMenu, List<MenuType> possibleMenus, int x, int y, int width, int height, int entityId) {
        super(x, y, width, height);


        for(int i = 0; i < POSSIBLE_TYPES.length; i++) {
            if(currentMenu.equals(POSSIBLE_TYPES[i].get())) {
                currentGuiIndex = i;
            }
        }

        for(int i = 0; i < possibleMenus.size(); i++) {
            if(possibleMenus.get(i).equals(currentMenu)) {
                subsetGuiIndex = i;
                continue; //Don't add a button for the currently active gui
            }

            ButtonElement button = new ButtonElement(x, y + i * height, width, height) {
                @Override
                public void onPress() {
                    NetworkHandler.sendToServer(new PacketOpenRobotMenu(possibleMenus.get(Math.floorDiv(getY() - y, height)), entityId));
                }
            };

            addElement(button);
        }

        //Find out on which row the texture is and save it in textureRows
        ArrayList<MenuType> buttonGuiIds = new ArrayList<>(possibleMenus);
        buttonGuiIds.remove(currentMenu);
        this.textureRows = new int[buttonGuiIds.size()];
        int i = 0, j = 0;
        while(i < POSSIBLE_TYPES.length && j < buttonGuiIds.size()) {
            if(POSSIBLE_TYPES[i].get() == buttonGuiIds.get(j)) {
                textureRows[j] = i;
                j++;
            }
            i++;
        }
    }

    @Override
    public void initTextureLocation(ResourceLocation texture, int textureX, int textureY) {
        //Assumes inactive icons are in the 4th column
        super.initTextureLocation(texture, textureX + width * 4, textureY + currentGuiIndex * height);
        int i = 0;
        for(var child : children()) {
            if(!(child instanceof IGuiTexturable texturable)) continue;
            texturable.initTextureLocation(texture, textureX, textureY + textureRows[i] * height);
            i++;
        }
    }
}
