package com.ignis.igrobotics.integration.cc.vanilla;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.integration.cc.IComputerized;
import com.ignis.igrobotics.integration.cc.ProgrammingMenu;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraftforge.fml.ModList;

import static com.ignis.igrobotics.common.RobotBehavior.hasAccess;

public class ScreenInvokator {

    public static MenuConstructor invokeProgrammingMenu(Entity target, IComputerized computer) {
        if(ModList.get().isLoaded(Reference.CC_MOD_ID)) return (id, playerInv, f3) -> new ProgrammingMenu(id, playerInv, target, p -> hasAccess(p, target, EnumPermission.COMMANDS), computer.getComputer());
        return (id, playerInv, f3) -> new VProgrammingMenu(id, playerInv, null);
    }
}
