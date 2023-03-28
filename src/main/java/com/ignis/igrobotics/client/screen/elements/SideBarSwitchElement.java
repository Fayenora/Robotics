package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.definitions.ModMenuTypes;
import com.ignis.igrobotics.network.messages.NetworkHandler;
import com.ignis.igrobotics.network.messages.server.PacketOpenRobotMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class SideBarSwitchElement extends GuiElement {

    public static final RegistryObject<MenuType<?>>[] POSSIBLE_TYPES = new RegistryObject[]{
            ModMenuTypes.ROBOT
    };

    /** Index of current menu inside all possible menus */
    int currentGuiIndex = 0;
    /** Index of current menu inside the currently active menus */
    int subsetGuiIndex = 0;
    int buttonHeight;
    /** For each button, saves at which row the designated texture is */
    int[] textureRows;

    public SideBarSwitchElement(MenuType currentMenu, List<MenuType> possibleMenus, int x, int y, int width, int height) {
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
                    NetworkHandler.sendToServer(new PacketOpenRobotMenu(possibleMenus.get(Math.floorDiv(getY() - y, height)), 0));
                }
            };

            addElement(button);
        }

        buttonHeight = height;
        this.height *= possibleMenus.size();

        //Find out on which row the texture is and save it in textureRows
        ArrayList<MenuType> buttonGuiIds = new ArrayList<>();
        buttonGuiIds.addAll(possibleMenus);
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
        super.initTextureLocation(texture, textureX + width * 4, textureY + currentGuiIndex * buttonHeight);
        int i = 0;
        for(var child : children()) {
            if(!(child instanceof IGuiTexturable texturable)) continue;
            texturable.initTextureLocation(texture, textureX, textureY + textureRows[i] * buttonHeight);
            i++;
        }
    }
}
