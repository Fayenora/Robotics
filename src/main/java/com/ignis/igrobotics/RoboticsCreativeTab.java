package com.ignis.igrobotics;

import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoboticsCreativeTab {

    @SubscribeEvent
    public static void register(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(Robotics.MODID, "items"), builder -> {
            builder.title(Component.translatable("item_group." + Robotics.MODID + ".items"))
                    .icon(() -> new ItemStack(ModItems.CIRCUIT.get()))
                    .displayItems((flags, populator, hasPermissions) -> {
                        Collection<RegistryObject<Item>> itemsWithoutMaterials = new ArrayList(ModItems.ITEMS.getEntries());
                        for(RegistryObject[] regs : ModItems.MATERIALS) {
                            itemsWithoutMaterials.removeAll(List.of(regs));
                        }
                        for(RegistryObject<Item> item : itemsWithoutMaterials) {
                            populator.accept(item.get());
                        }
                        for(RegistryObject<Block> block : ModBlocks.BLOCKS.getEntries()) {
                            populator.accept(block.get());
                        }
                    });
        });
        event.registerCreativeModeTab(new ResourceLocation(Robotics.MODID, "materials"), builder -> {
            builder.title(Component.translatable("item_group." + Robotics.MODID + ".materials"))
                    .icon(() -> new ItemStack(ModItems.MATERIALS[0][0].get()))
                    .displayItems((flags, populator, hasPermissions) -> {
                        for(RegistryObject[] regs : ModItems.MATERIALS) {
                            for(RegistryObject<Item> reg : regs) {
                                populator.accept(reg.get());
                            }
                        }
                    });
        });
    }
}
