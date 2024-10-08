package com.io.norabotics.definitions;

import com.io.norabotics.Reference;
import com.io.norabotics.Robotics;
import com.io.norabotics.common.content.menu.*;
import com.io.norabotics.integration.cc.ProgrammingMenu;
import com.io.norabotics.integration.cc.vanilla.VProgrammingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Robotics.MODID);

    public static final RegistryObject<MenuType<AssemblerMenu>> ASSEMBLER = registerMenuType(AssemblerMenu::new, "assembler_menu");
    public static final RegistryObject<MenuType<WireCutterMenu>> WIRE_CUTTER = registerMenuType(WireCutterMenu::new, "wire_cutter_menu");
    public static final RegistryObject<MenuType<FactoryMenu>> FACTORY = registerMenuType(FactoryMenu::new, "factory_menu");
    public static final RegistryObject<MenuType<StorageMenu>> STORAGE = registerMenuType(StorageMenu::new, "storage_menu");
    public static final RegistryObject<MenuType<RobotMenu>> ROBOT = registerMenuType(RobotMenu::new, "robot_menu");
    public static final RegistryObject<MenuType<RobotInfoMenu>> ROBOT_INFO = registerMenuType(RobotInfoMenu::new, "robot_info_menu");
    public static final RegistryObject<MenuType<AbstractContainerMenu>> ROBOT_INVENTORY = registerMenuType((id, inv, buf) -> null, "robot_inventory_menu");
    public static final RegistryObject<MenuType<RobotCommandMenu>> ROBOT_COMMANDS = registerMenuType(RobotCommandMenu::new, "robot_command_menu");
    public static final RegistryObject<MenuType<CommanderMenu>> COMMANDER = registerMenuType(CommanderMenu::new , "commander_menu");
    public static RegistryObject<MenuType<? extends AbstractContainerMenu>> COMPUTER = registerCompatMenuType(ModList.get().isLoaded(Reference.CC_MOD_ID) ? ProgrammingMenu::new : VProgrammingMenu::new, "computer_menu");

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENU_TYPES.register(name, () -> IForgeMenuType.create(factory));
    }

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<? extends AbstractContainerMenu>> registerCompatMenuType(IContainerFactory<T> factory, String name) {
        return MENU_TYPES.register(name, () -> IForgeMenuType.create(factory));
    }

    @Nullable
    public static RegistryObject<MenuType<?>> of(MenuType<?> menuType) {
        return MENU_TYPES.getEntries().stream().filter(m -> m.get().equals(menuType)).findFirst().get();
    }
}
