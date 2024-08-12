package com.ignis.norabotics.definitions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.misc.CopyContentsFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModLootItemFunctions {

    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Robotics.MODID);

    public static final RegistryObject<LootItemFunctionType> COPY_CONTENTS = register("copy_contents", CopyContentsFunction.INSTANCE);

    private static RegistryObject<LootItemFunctionType> register(String name, LootItemFunction lootItemFunction) {
        Codec<LootItemFunction> codec = Codec.unit(lootItemFunction);
        return LOOT_FUNCTIONS.register(name, () -> new LootItemFunctionType(new Serializer<>() {
            @Override
            public void serialize(JsonObject pJson, LootItemFunction pValue, JsonSerializationContext pSerializationContext) {
                codec.encode(pValue, JsonOps.INSTANCE, pJson);
            }

            @Override
            public LootItemFunction deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
                return codec.parse(JsonOps.INSTANCE, pJson).getOrThrow(false, s -> {});
            }
        }));
    }
}
