package com.ignis.igrobotics.datagen;

import com.ignis.igrobotics.definitions.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class BlockSelfDropGenerator extends BlockLootSubProvider {

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