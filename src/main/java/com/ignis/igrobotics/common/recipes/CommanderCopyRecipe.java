package com.ignis.igrobotics.common.recipes;

import com.ignis.igrobotics.definitions.ModItems;
import com.ignis.igrobotics.definitions.ModMachines;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommanderCopyRecipe extends CustomRecipe {

    public static final Predicate<ItemStack> PREDICATE_COPY_FROM = s -> !s.isEmpty() && s.is(ModItems.COMMANDER.get()) && s.hasTag();

    public CommanderCopyRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
        super(pId, pCategory);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level pLevel) {
        //The inventory contains exactly two commanders, of which at least one needs to contain nbt data
        int count = 0;
        boolean nbtPresent = false;
        for(ItemStack stack : inv.getItems()) {
            if(!stack.isEmpty() && stack.is(ModItems.COMMANDER.get())) {
                count++;
            }
            nbtPresent = nbtPresent || stack.hasTag();
        }
        return count == 2 && nbtPresent;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess pRegistryAccess) {
        ItemStack copyFrom = ItemStack.EMPTY;

        //Find the correct commander to copy the id from
        int i = 0;
        while(copyFrom.isEmpty()) {
            ItemStack stack = inv.getItem(i);
            if (PREDICATE_COPY_FROM.test(stack)) {
                copyFrom = stack;
            }
            i++;
            if(i >= inv.getContainerSize()) {
                return ItemStack.EMPTY; //If none is found, return
            }
        }

        //Copy nbt and name
        ItemStack copy = new ItemStack(ModItems.COMMANDER.get());
        copy.setTag(copyFrom.getTag().copy());
        if (copyFrom.hasCustomHoverName()) {
            copy.setHoverName(copyFrom.getDisplayName());
        }

        return copy;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (PREDICATE_COPY_FROM.test(stack)) {
                ItemStack copied = stack.copy();
                copied.setCount(1);
                nonnulllist.set(i, copied);
                break;
            }
        }

        return nonnulllist;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth + pHeight > 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModMachines.COMMANDERCOPY_RECIPE.get();
    }
}
