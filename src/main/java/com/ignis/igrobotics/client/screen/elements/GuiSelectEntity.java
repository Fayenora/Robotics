package com.ignis.igrobotics.client.screen.elements;

import com.ignis.igrobotics.client.screen.base.GuiElement;
import com.ignis.igrobotics.client.screen.selectors.SelectorElement;
import com.ignis.igrobotics.common.helpers.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class GuiSelectEntity extends GuiElement {

    EditBox searchBar;
    ScrollableElement entityGrid;

    String currentSearch = "";
    Collection<LivingEntity> allOptions;
    Collection<LivingEntity> currentOptions;
    Consumer<LivingEntity> onSelect;

    private float angle;

    public GuiSelectEntity(Collection<LivingEntity> selectableOptions, Consumer<LivingEntity> onSelect) {
        super(0, 0, 162, 164);
        initTextureLocation(SelectorElement.TEXTURE, 0, 0);
        this.allOptions = selectableOptions;
        this.currentOptions = selectableOptions;
        this.onSelect = onSelect;

        searchBar = new EditBox(Minecraft.getInstance().font, getX() + 8, getY() + 8, 146, 10, Component.empty());
        entityGrid = new ScrollableElement(getX() + 9, getY() + 25, 145, 131);
        setOptions(currentOptions);

        addElement(searchBar);
        addElement(entityGrid);
        setFocused(searchBar);
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        angle += pPartialTick * 3;
        angle %= 360;
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        //Update the search
        String searchTerm = searchBar.getValue().toLowerCase();
        if(searchTerm.equals(currentSearch)) return;
        if(currentSearch.length() < searchTerm.length()) {
            //Squeeze some efficiency here by only narrowing the search if it got longer
            currentOptions = MathUtil.subset(currentOptions, ent -> ent.getName().getString().toLowerCase().contains(searchTerm));
        } else {
            //If the search got shorter, we have no choice but to search everything again
            currentOptions = MathUtil.subset(allOptions, ent -> ent.getName().getString().toLowerCase().contains(searchTerm));
        }
        setOptions(currentOptions);
        currentSearch = searchTerm;
    }

    private void setOptions(Collection<LivingEntity> options) {
        entityGrid.clear();
        for(LivingEntity living : options) {
            entityGrid.addElement(new EntityElement(living, 0, 0, () -> angle, pButton -> {
                onSelect.accept(living);
                getBaseGui().removeSubGui();
            }));
        }
    }
}
