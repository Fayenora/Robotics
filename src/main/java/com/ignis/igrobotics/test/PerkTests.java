package com.ignis.igrobotics.test;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Robotics.MODID)
public class PerkTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default")
    public static void testPerkInvulnerability(GameTestHelper helper) {
        RobotEntity robot = new RobotEntity(helper.getLevel());
        robot.setPos(helper.absoluteVec(new Vec3(1.5, 2, 1.5)));
        robot.getCapability(ModCapabilities.PARTS).ifPresent(parts -> parts.setBodyPart(RobotPart.get(EnumRobotPart.BODY, EnumRobotMaterial.NETHERITE)));
        helper.getLevel().addFreshEntity(robot);
        robot.hurt(robot.damageSources().generic(), 1);
        helper.runAfterDelay(15, () -> {
            if(!robot.hurt(robot.damageSources().generic(), 1)) {
                helper.succeed();
            } else helper.fail("Invulnerability Perk does not increase invulnerability time");
        });
    }
}
