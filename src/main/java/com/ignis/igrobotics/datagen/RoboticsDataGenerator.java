package com.ignis.igrobotics.datagen;

import com.ignis.igrobotics.Robotics;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RoboticsDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        // Client Generators
        gen.addProvider(event.includeClient(), new ItemModelGenerator(output, event.getExistingFileHelper()));
        //event.getGenerator().addProvider(event.includeClient(), (Factory<TextureGenerator>) TextureGenerator::new);
        // Server Generators
        gen.addProvider(event.includeServer(), new RecipeGenerator(output));
        gen.addProvider(event.includeServer(), new LootTableProvider(output, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(BlockSelfDropGenerator::new, LootContextParamSets.BLOCK))));
        gen.addProvider(event.includeServer(), new TagGenerator(output, lookupProvider, existingFileHelper));
    }
}
