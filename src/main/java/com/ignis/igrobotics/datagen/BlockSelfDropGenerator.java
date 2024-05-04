package com.ignis.igrobotics.datagen;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.definitions.ModBlocks;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockSelfDropGenerator extends BlockLootSubProvider {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) output ->
                new LootTableProvider(output,
                        Collections.emptySet(),
                        List.of(new LootTableProvider.SubProviderEntry(BlockSelfDropGenerator::new, LootContextParamSets.BLOCK))));
    }

    protected BlockSelfDropGenerator() {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        for(RegistryObject<Block> blocks : ModBlocks.BLOCKS.getEntries()) {
            dropSelf(blocks.get());
        }
    }

    @NotNull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().flatMap(RegistryObject::stream)::iterator;
    }
}