package com.ignis.igrobotics.test;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.robot.*;
import com.ignis.igrobotics.definitions.ModModules;
import com.ignis.igrobotics.definitions.ModPerks;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

@GameTestHolder(Robotics.MODID)
public class PerkTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "perks")
    public static void testPerkInvulnerability(GameTestHelper helper) {
        RobotEntity robot = createDefaultRobot(helper.getLevel());
        addPerkToRobot(robot, ModPerks.PERK_INVULNERABILITY.get());
        robot.setPos(helper.absoluteVec(new Vec3(1.5, 2, 1.5)));
        helper.getLevel().addFreshEntity(robot);
        robot.hurt(robot.damageSources().generic(), 1);
        helper.runAfterDelay(15, () -> {
            if(!robot.hurt(robot.damageSources().generic(), 1)) {
                helper.succeed();
            } else helper.fail("Invulnerability Perk does not increase invulnerability time");
        });
    }

    private static RobotEntity createDefaultRobot(Level level) {
        RobotEntity robot = new RobotEntity(level);
        robot.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
            for(EnumRobotPart part : EnumRobotPart.values()) {
                parts.setBodyPart(part, EnumRobotMaterial.IRON);
            }
        });
        return robot;
    }

    private static void addPerkToRobot(RobotEntity robot, Perk perkToAdd) {
        Iterator<RobotModule> modules = ModModules.getModules(robot.level().registryAccess()).iterator();
        while(!robot.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(perkToAdd)) {
            RobotModule module = modules.next();
            if(!module.getPerks().contains(perkToAdd)) continue;
            if(module.getItems().getItems().length == 0) continue;
            ItemStack stack = module.getItems().getItems()[0];
            RobotPart part = RobotPart.getFromItem(stack.getItem());
            if(part != null) {
                robot.getCapability(ModCapabilities.PARTS).ifPresent(parts -> parts.setBodyPart(part));
            } else {
                EnumSet<EnumModuleSlot> slots = module.getViableSlots();
                if(slots.isEmpty()) continue;
                EnumModuleSlot slot = slots.iterator().next();
                robot.getCapability(ModCapabilities.ROBOT).ifPresent(r -> r.setModules(slot, List.of(stack)));
            }
        }
        if(!robot.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(perkToAdd)) {
            throw new RuntimeException("Perk " + perkToAdd + " is not present in any robot part");
        }
    }

    @AfterBatch(batch = "perks")
    public static void cleanup(GameTestHelper helper) {
        helper.killAllEntities();
    }
}
