package com.ignis.norabotics.definitions;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.content.blocks.*;
import com.ignis.norabotics.integration.cc.RedstoneInterface;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Robotics.MODID);

    public static final RegistryObject<Block> ASSEMBLER = registerBlock("assembler", AssemblerBlock::new);
    public static final RegistryObject<Block> WIRE_CUTTER = registerBlock("wire_cutter", WireCutterBlock::new);
    public static final RegistryObject<Block> ROBOT_STORAGE = registerBlock("robot_storage", StorageBlock::new);
    public static final RegistryObject<Block> ROBOT_FACTORY = registerBlock("robot_factory", FactoryBlock::new);
    public static final RegistryObject<Block> REDSTONE_INTEGRATOR = registerBlock("redstone_integrator", RedstoneInterface::new);
    public static final RegistryObject<Block> CHARGER = registerBlock("charger", ChargerBlock::new);
    public static final RegistryObject<Block> MACHINE_ARM = registerBlock("machine_arm", MachineArmBlock::new);

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }
}
