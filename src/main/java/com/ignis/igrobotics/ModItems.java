package com.ignis.igrobotics;

import com.ignis.igrobotics.common.entity.ModEntityTypes;
import com.ignis.igrobotics.common.items.ItemCommander;
import com.ignis.igrobotics.core.RobotPart;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
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

    public static final RegistryObject<Item> ROBOT_SPAWN_EGG = ITEMS.register("robot_spawn_egg", () -> new ForgeSpawnEggItem(ModEntityTypes.ROBOT, 0x000000, 0xffa500, new Item.Properties()));

    public static final RegistryObject<Item> COMMANDER = ITEMS.register("commander", () -> new ItemCommander());

    public static final RegistryObject<Item>[] WIRES = new RegistryObject[Reference.WIRE_METALS.length];
    public static final RegistryObject<Item>[] PLATES = new RegistryObject[RobotPart.EnumRobotMaterial.valuesWithoutEmpty().length];
    public static final RegistryObject<Item>[][] MATERIALS = new RegistryObject[RobotPart.EnumRobotMaterial.valuesWithoutEmpty().length][];

    static {
        for(int i = 0; i < Reference.WIRE_METALS.length; i++) {
            WIRES[i] = registerBasicItem(Reference.WIRE_METALS[i] + "_wire");
        }
        for(RobotPart.EnumRobotMaterial material : RobotPart.EnumRobotMaterial.valuesWithoutEmpty()) {
            PLATES[material.getID() - 1] = registerBasicItem("plate_" + material.getName());
            MATERIALS[material.getID() - 1] = new RegistryObject[RobotPart.EnumRobotPart.values().length];
            for(RobotPart.EnumRobotPart part : RobotPart.EnumRobotPart.values()) {
                MATERIALS[material.getID() - 1][part.getID()] = registerBasicItem(material.getName() + "_" + part.getName());
            }
        }
    }

    private static RegistryObject<Item> registerBasicItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }
}
