package com.ignis.igrobotics.datagen;

import com.ignis.igrobotics.common.misc.CopyContentsFunction;
import com.ignis.igrobotics.definitions.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
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
            dropSelfWithContents(blocks.get());
        }
    }

    @NotNull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().flatMap(RegistryObject::stream)::iterator;
    }

    private void dropSelfWithContents(Block block) {
        LootItem.Builder<?> itemLootPool = LootItem.lootTableItem(block);
        if(block instanceof EntityBlock ) {
            itemLootPool.apply(CopyContentsFunction.builder());
            itemLootPool.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));
        }

        add(block, LootTable.lootTable().withPool(applyExplosionCondition(false, LootPool.lootPool()
                .name("main")
                .setRolls(ConstantValue.exactly(1))
                .add(itemLootPool)
        )));
    }

    private static <T extends ConditionUserBuilder<T>> T applyExplosionCondition(boolean explosionResistant, ConditionUserBuilder<T> condition) {
        return explosionResistant ? condition.unwrap() : condition.when(ExplosionCondition.survivesExplosion());
    }
}