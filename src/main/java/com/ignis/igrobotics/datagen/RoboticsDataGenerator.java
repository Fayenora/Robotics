package com.ignis.igrobotics.datagen;

import com.ignis.igrobotics.Robotics;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RoboticsDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // Client Generators
        //event.getGenerator().addProvider(event.includeClient(), (Factory<TextureGenerator>) TextureGenerator::new);
        // Server Generators
        event.getGenerator().addProvider(event.includeServer(), (DataProvider.Factory<RecipeGenerator>) RecipeGenerator::new);
        event.getGenerator().addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) output ->
                new LootTableProvider(output,
                        Collections.emptySet(),
                        List.of(new LootTableProvider.SubProviderEntry(BlockSelfDropGenerator::new, LootContextParamSets.BLOCK))));
        event.getGenerator().addProvider(event.includeServer(), (DataProvider.Factory<TagsProvider<Item>>) output ->
                new TagGenerator(output, event.getLookupProvider(), event.getExistingFileHelper()));
    }
}
