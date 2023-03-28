package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.menu.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Robotics.MODID);

    public static final RegistryObject<MenuType<AssemblerMenu>> ASSEMBLER = registerMenuType(AssemblerMenu::new, "assembler_menu");
    public static final RegistryObject<MenuType<WireCutterMenu>> WIRE_CUTTER = registerMenuType(WireCutterMenu::new, "wire_cutter_menu");
    public static final RegistryObject<MenuType<FactoryMenu>> FACTORY = registerMenuType(FactoryMenu::new, "factory_menu");
    public static final RegistryObject<MenuType<RobotMenu>> ROBOT = registerMenuType(RobotMenu::new, "robot_menu");
    public static final RegistryObject<MenuType<RobotInfoMenu>> ROBOT_INFO = registerMenuType(RobotInfoMenu::new, "robot_info_menu");

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENU_TYPES.register(name, () -> IForgeMenuType.create(factory));
    }
}
