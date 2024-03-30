package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.items.CommanderItem;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
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
    public static final RegistryObject<Item> MODULE_SOLAR_PANEL = registerBasicItem("solar_panel");
    public static final RegistryObject<Item> MODULE_FIST = registerBasicItem("fist");
    public static final RegistryObject<Item> MODULE_TELEPORT = registerBasicItem("ender_module");
    public static final RegistryObject<Item> MODULE_DASH = registerBasicItem("dash_module");
    public static final RegistryObject<Item> MODULE_CHARGE_LEGS = registerBasicItem("charge_legs");
    public static final RegistryObject<Item> MODULE_REINFORCE = registerBasicItem("reinforce_module");
    public static final RegistryObject<Item> MODULE_STEALTH = registerBasicItem("stealth_module");
    public static final RegistryObject<Item> MODULE_TNT = registerBasicItem("tnt_module");
    public static final RegistryObject<Item> MODULE_SHIELD = registerBasicItem("shield_module");

    public static final RegistryObject<Item> ROBOT_SPAWN_EGG = ITEMS.register("robot_spawn_egg", () -> new ForgeSpawnEggItem(ModEntityTypes.ROBOT, 0x000000, 0xffa500, new Item.Properties()));

    public static final RegistryObject<Item> COMMANDER = ITEMS.register("commander", CommanderItem::new);

    public static final RegistryObject<Item>[] WIRES = new RegistryObject[Reference.WIRE_METALS.length];
    public static final RegistryObject<Item>[] PLATES = new RegistryObject[EnumRobotMaterial.valuesWithoutEmpty().length];
    public static final RegistryObject<Item>[][] MATERIALS = new RegistryObject[EnumRobotMaterial.valuesWithoutEmpty().length][];

    static {
        int i = 0;
        for(EnumRobotMaterial material : Reference.WIRE_METALS) {
            WIRES[i++] = registerBasicItem(material.getName() + "_wire");
        }
        for(EnumRobotMaterial material : EnumRobotMaterial.valuesWithoutEmpty()) {
            PLATES[material.getID() - 1] = registerBasicItem("plate_" + material.getName());
            MATERIALS[material.getID() - 1] = new RegistryObject[EnumRobotPart.values().length];
            for(EnumRobotPart part : EnumRobotPart.values()) {
                MATERIALS[material.getID() - 1][part.getID()] = ITEMS.register(material.getName() + "_" + part.getName(), () -> new Item(new Item.Properties().stacksTo(1)));
            }
        }
    }

    private static RegistryObject<Item> registerBasicItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }
}
