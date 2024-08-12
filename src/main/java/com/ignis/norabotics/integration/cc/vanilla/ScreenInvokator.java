package com.ignis.norabotics.integration.cc.vanilla;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.common.access.EnumPermission;
import com.ignis.norabotics.integration.cc.IComputerized;
import com.ignis.norabotics.integration.cc.ProgrammingMenu;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraftforge.fml.ModList;

import static com.ignis.norabotics.common.handlers.RobotBehavior.hasAccess;

public class ScreenInvokator {

    public static MenuConstructor invokeProgrammingMenu(Entity target, IComputerized computer) {
        if(ModList.get().isLoaded(Reference.CC_MOD_ID)) return (id, playerInv, f3) -> new ProgrammingMenu(id, playerInv, target, p -> hasAccess(p, target, EnumPermission.COMMANDS), computer.getComputer());
        return (id, playerInv, f3) -> new VProgrammingMenu(id, playerInv, null);
    }
}
