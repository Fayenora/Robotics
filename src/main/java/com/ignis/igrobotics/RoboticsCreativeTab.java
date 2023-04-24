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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RoboticsCreativeTab {

    @SubscribeEvent
    public static void register(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(Robotics.MODID, "items"), builder -> {
            builder.title(Component.translatable("item_group." + Robotics.MODID + ".items"))
                    .icon(() -> new ItemStack(ModItems.CIRCUIT.get()))
                    .displayItems((params, output) -> {
                        Collection<RegistryObject<Item>> itemsWithoutMaterials = new ArrayList(ModItems.ITEMS.getEntries());
                        for(RegistryObject[] regs : ModItems.MATERIALS) {
                            itemsWithoutMaterials.removeAll(List.of(regs));
                        }
                        for(RegistryObject<Item> item : itemsWithoutMaterials) {
                            output.accept(item.get());
                        }
                        for(RegistryObject<Block> block : ModBlocks.BLOCKS.getEntries()) {
                            output.accept(block.get());
                        }
                    });
        });
        event.registerCreativeModeTab(new ResourceLocation(Robotics.MODID, "materials"), builder -> {
            builder.title(Component.translatable("item_group." + Robotics.MODID + ".materials"))
                    .icon(() -> new ItemStack(ModItems.MATERIALS[0][0].get()))
                    .displayItems((params, output) -> {
                        for(RegistryObject[] regs : ModItems.MATERIALS) {
                            for(RegistryObject<Item> reg : regs) {
                                output.accept(reg.get());
                            }
                        }
                    });
        });
    }
}
