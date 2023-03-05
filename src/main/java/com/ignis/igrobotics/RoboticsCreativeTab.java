package com.ignis.igrobotics;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

public class RoboticsCreativeTab {

    @SubscribeEvent
    public static void register(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(Robotics.MODID, "items"), builder -> {
            builder.title(Component.translatable("item_group." + Robotics.MODID + ".items"))
                    .icon(() -> new ItemStack(ModItems.CIRCUIT.get()))
                    .displayItems((flags, populator, hasPermissions) -> {
                        for(RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
                            populator.accept(item.get());
                        }
                        for(RegistryObject<Block> block : ModBlocks.BLOCKS.getEntries()) {
                            populator.accept(block.get());
                        }
                    });
        });
    }
}
