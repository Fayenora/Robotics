package com.io.norabotics.definitions;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.robot.EnumRobotMaterial;
import com.io.norabotics.common.robot.EnumRobotPart;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Robotics.MODID);
    public static final RegistryObject<Item> MATERIAL_DISPLAY_ITEM = ModItems.MATERIALS.get(EnumRobotMaterial.IRON).get(EnumRobotPart.HEAD);

    public static final RegistryObject<CreativeModeTab> ITEMS = CREATIVE_TABS.register("items", () -> builder("items", ModItems.CIRCUIT)
            .displayItems((params, output) -> {
                Collection<RegistryObject<Item>> itemsWithoutMaterials = new ArrayList<>(ModItems.ITEMS.getEntries());
                Collection<RegistryObject<Block>> blocks = new ArrayList<>(ModBlocks.BLOCKS.getEntries());
                blocks.remove(ModBlocks.REDSTONE_INTEGRATOR);
                itemsWithoutMaterials.removeIf(entry -> entry.getId().equals(ModBlocks.REDSTONE_INTEGRATOR.getId()));
                for(Map<EnumRobotPart, RegistryObject<Item>> regs : ModItems.MATERIALS.values()) {
                    itemsWithoutMaterials.removeAll(regs.values());
                }
                for(Map.Entry<EnumRobotMaterial, RegistryObject<Item>> entry : ModItems.PLATES.entrySet()) {
                    if(isTagEmpty(params.holders(), entry.getKey().getMetal())) {
                        itemsWithoutMaterials.remove(entry.getValue());
                    }
                }
                for(RegistryObject<Item> item : itemsWithoutMaterials) {
                    output.accept(item.get());
                }
                for(RegistryObject<Block> block : blocks) {
                    output.accept(block.get());
                }
            }).build());
    public static final RegistryObject<CreativeModeTab> MATERIALS = CREATIVE_TABS.register("materials", () -> builder("materials", MATERIAL_DISPLAY_ITEM)
            .displayItems((params, output) -> {
                for(EnumRobotMaterial material : ModItems.MATERIALS.keySet()) {
                    for(RegistryObject<Item> reg : ModItems.MATERIALS.get(material).values()) {
                        if(isTagEmpty(params.holders(), material.getMetal())) continue;
                        output.accept(reg.get());
                    }
                }
            }).build());

    private static boolean isTagEmpty(HolderLookup.Provider provider, TagKey<Item> tag) {
        Optional<HolderLookup.RegistryLookup<Item>> reg = provider.lookup(ForgeRegistries.ITEMS.getRegistryKey());
        if(reg.isEmpty()) return true;
        Optional<HolderSet.Named<Item>> itemsOfTag = reg.get().get(tag);
        return itemsOfTag.map(holders -> holders.size() == 0).orElse(true);
    }

    public static CreativeModeTab.Builder builder(String name, Supplier<Item> displayItem) {
        return CreativeModeTab.builder()
                .icon(() -> new ItemStack(displayItem.get()))
                .title(Component.translatable("item_group." + Robotics.MODID + "." + name));
    }
}
