package com.ignis.norabotics.definitions;

import com.ignis.norabotics.client.screen.selectors.*;
import com.ignis.norabotics.common.helpers.types.EntitySearch;
import com.ignis.norabotics.common.helpers.types.SelectionType;
import com.ignis.norabotics.common.helpers.util.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ModSelectionTypes {

    public static final List<SelectionType<?>> TYPES = new ArrayList<>();

    public static final SelectionType<ItemStack> ITEM = register(ItemStack.class, Items.IRON_SWORD::getDefaultInstance, ItemStack::serializeNBT, ItemStack::of, string -> new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(string))), stack -> ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
    public static final SelectionType<Block> BLOCK = register(Block.class, () -> Blocks.COBBLESTONE, null, null, string -> ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(string)), block -> ForgeRegistries.BLOCKS.getKey(block).toString());
    public static final SelectionType<GlobalPos> POS = register(GlobalPos.class, () -> GlobalPos.of(ServerLifecycleHooks.getCurrentServer().overworld().dimension(), BlockPos.ZERO), PosUtil::writePos, PosUtil::readPos, PosUtil::parseBlockPos, GlobalPos::toString);
    public static final SelectionType<EntityType> ENTITY_TYPE = register(EntityType.class, () -> EntityType.CREEPER, type -> {
                CompoundTag tag = new CompoundTag();
                tag.putString("value", ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
                return tag;
            }, tag -> ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(tag.getString("value"))),
            string -> ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(string)), type -> ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
    public static final SelectionType<Integer> INTEGER = register(Integer.class, () -> 0, number -> {
        CompoundTag tag = new CompoundTag();
        tag.putInt("value", number);
        return tag;
    }, tag -> tag.getInt("value"), string -> Integer.valueOf(Arrays.stream(string.split("\\D")).filter(s -> !s.isEmpty()).findFirst().get()), Object::toString);
    public static final SelectionType<EntitySearch> ENTITY_PREDICATE = register(EntitySearch.class, EntitySearch::searchForNone, EntitySearch::serializeNBT, EntitySearch::of, EntitySearch::new, EntitySearch::toString);

    public static <T> SelectionType<T> register(Class<T> type, Supplier<T> defaultsTo, Function<T, CompoundTag> writer, Function<CompoundTag, T> reader, Function<String, T> parser, Function<T, String> toString) {
        SelectionType<T> selectionType = new SelectionType<>(type, defaultsTo, writer, reader, parser, toString);
        TYPES.add(selectionType);
        return selectionType;
    }

    public static void loadGuis() {
        ITEM.setGui(ItemSelector.class);
        POS.setGui(PosSelector.class);
        ENTITY_TYPE.setGui(EntityTypeSelector.class);
        INTEGER.setGui(IntSelector.class);
        ENTITY_PREDICATE.setGui(EntitySearchSelector.class);
    }

}
