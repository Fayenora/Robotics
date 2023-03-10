package com.ignis.igrobotics;

import com.ignis.igrobotics.common.blocks.BlockAssembler;
import com.ignis.igrobotics.common.blocks.BlockFactory;
import com.ignis.igrobotics.common.blocks.BlockStorage;
import com.ignis.igrobotics.common.blocks.BlockWireCutter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Robotics.MODID);

    public static final RegistryObject<Block> ASSEMBLER = registerBlock("assembler", BlockAssembler::new);
    public static final RegistryObject<Block> WIRE_CUTTER = registerBlock("wire_cutter", BlockWireCutter::new);
    public static final RegistryObject<Block> ROBOT_STORAGE = registerBlock("robot_storage", BlockStorage::new);
    public static final RegistryObject<Block> ROBOT_FACTORY = registerBlock("robot_factory", BlockFactory::new);

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }
}
