package com.ignis.igrobotics;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Robotics.MODID);

    public static final RegistryObject<Item> CIRCUIT = registerBasicItem("circuit");
    public static final RegistryObject<Item> ADVANCED_CIRCUIT = registerBasicItem("advanced_circuit");
    public static final RegistryObject<Item> COMPLEX_CIRCUIT = registerBasicItem("complex_circuit");

    public static final RegistryObject<Item> WIRING = registerBasicItem("wiring");
    public static final RegistryObject<Item> ADVANCED_WIRING = registerBasicItem("advanced_wiring");
    public static final RegistryObject<Item> COMPLEX_WIRING = registerBasicItem("complex_wiring");

    public static final RegistryObject<Item> IRON_ROD = registerBasicItem("iron_rod");
    public static final RegistryObject<Item> ELECTRO_MAGNET = registerBasicItem("electro_magnet");
    public static final RegistryObject<Item> SERVO_MOTOR = registerBasicItem("servo_motor");

    public static final RegistryObject<Item> CAMERA_UNIT = registerBasicItem("camera_unit");
    public static final RegistryObject<Item> PERCEPTRON_LAYER = registerBasicItem("perceptron_layer");
    public static final RegistryObject<Item> NEURAL_PROCESSING_UNIT = registerBasicItem("neural_processing_unit");

    public static final RegistryObject<Item> MODULE_BATTERY = registerBasicItem("battery");

    /*
    public static final RegistryObject<Item> commander = new ItemCommander("commander");

    public static final RegistryObject<Item>[] WIRES = new Item[Reference.WIRE_METALS.length];
    public static final RegistryObject<Item>[] PLATES = new Item[EnumRobotMaterial.values().length];
    public static final RegistryObject<Item>[] MATERIALS = new Item[EnumRobotMaterial.values().length];
     */

    private static RegistryObject<Item> registerBasicItem(String name) {
        return registerBasicItem(name, Robotics.TAB_ROBOTICS);
    }

    private static RegistryObject<Item> registerBasicItem(String name, CreativeModeTab tab) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().tab(tab)));
    }
}
