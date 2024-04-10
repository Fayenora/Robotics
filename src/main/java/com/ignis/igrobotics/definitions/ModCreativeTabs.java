package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Robotics.MODID);

    public static final RegistryObject<CreativeModeTab> ITEMS = CREATIVE_TABS.register("items", () -> builder("items", ModItems.CIRCUIT)
            .displayItems((params, output) -> {
                Collection<RegistryObject<Item>> itemsWithoutMaterials = new ArrayList<>(ModItems.ITEMS.getEntries());
                Collection<RegistryObject<Block>> blocks = new ArrayList<>(ModBlocks.BLOCKS.getEntries());
                blocks.remove(ModBlocks.REDSTONE_INTEGRATOR);
                itemsWithoutMaterials.removeIf(entry -> entry.getId().equals(ModBlocks.REDSTONE_INTEGRATOR.getId()));
                for(RegistryObject<Item>[] regs : ModItems.MATERIALS) {
                    itemsWithoutMaterials.removeAll(List.of(regs));
                }
                for(RegistryObject<Item> item : itemsWithoutMaterials) {
                    output.accept(item.get());
                }
                for(RegistryObject<Block> block : blocks) {
                    output.accept(block.get());
                }
            }).build());
    public static final RegistryObject<CreativeModeTab> MATERIALS = CREATIVE_TABS.register("materials", () -> builder("materials", ModItems.MATERIALS[0][0])
            .displayItems((params, output) -> {
                for(RegistryObject<Item>[] regs : ModItems.MATERIALS) {
                    for(RegistryObject<Item> reg : regs) {
                        output.accept(reg.get());
                    }
                }
            }).build());

    public static CreativeModeTab.Builder builder(String name, Supplier<Item> displayItem) {
        return CreativeModeTab.builder()
                .icon(() -> new ItemStack(displayItem.get()))
                .title(Component.translatable("item_group." + Robotics.MODID + "." + name));
    }
}
