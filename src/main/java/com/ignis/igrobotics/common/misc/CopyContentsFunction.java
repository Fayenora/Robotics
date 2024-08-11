package com.ignis.igrobotics.common.misc;

import com.ignis.igrobotics.definitions.ModLootItemFunctions;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Set;

@MethodsReturnNonnullByDefault
public class CopyContentsFunction implements LootItemFunction {

    public static final CopyContentsFunction INSTANCE = new CopyContentsFunction();

    private CopyContentsFunction() {}

    @Override
    public LootItemFunctionType getType() {
        return ModLootItemFunctions.COPY_CONTENTS.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if(blockEntity == null) return stack;
        blockEntity.saveToItem(stack);
        return stack;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.BLOCK_ENTITY);
    }

    public static LootItemFunction.Builder builder() {
        return () -> INSTANCE;
    }
}
