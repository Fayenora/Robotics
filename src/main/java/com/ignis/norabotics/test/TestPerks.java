package com.ignis.norabotics.test;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.content.entity.RobotEntity;
import com.ignis.norabotics.common.helpers.types.EntitySearch;
import com.ignis.norabotics.common.helpers.types.Selection;
import com.ignis.norabotics.common.helpers.types.Tuple;
import com.ignis.norabotics.common.helpers.util.InventoryUtil;
import com.ignis.norabotics.common.robot.*;
import com.ignis.norabotics.definitions.*;
import com.ignis.norabotics.definitions.robotics.ModCommands;
import com.ignis.norabotics.definitions.robotics.ModPerks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

@GameTestHolder(Robotics.MODID)
public class TestPerks {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "perks")
    public static void testInvulnerability(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        TestHelpers.addPerkToRobot(robot, ModPerks.PERK_INVULNERABILITY.get());
        robot.hurt(robot.damageSources().generic(), 1);
        helper.runAfterDelay(15, () -> {
            if(!robot.hurt(robot.damageSources().generic(), 1)) {
                helper.succeed();
            } else helper.fail("Invulnerability Perk does not increase invulnerability time");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "perks")
    public static void testThermalConductivity(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        TestHelpers.addPerkToRobot(robot, ModPerks.PERK_THERMAL_CONDUCTIVITY.get());

        helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(1, 2, 1)), Blocks.LAVA.defaultBlockState());
        helper.runAfterDelay(25, () -> {
            if(!robot.hurt(robot.damageSources().generic(), 1)) {
                helper.succeed();
            } else helper.fail("Thermal Conductivity Perk does not increase invulnerability time");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "perks")
    public static void testRobust(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        TestHelpers.addPerkToRobot(robot, ModPerks.PERK_ROBUST.get());
        robot.hurt(robot.damageSources().generic(), 1);
        helper.runAfterDelay(1, () -> {
            if (robot.getHealth() < robot.getMaxHealth()) helper.fail("Robust perk did not protect robot from damage");
            else helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testMagnetic(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.createIronRobot(helper.getLevel());
        TestHelpers.addPerkToRobot(robot, ModPerks.PERK_MAGNETIC.get());
        robot.setPos(helper.absoluteVec(new Vec3(1.5, 2, 2.5)));
        Vec3 itemPos = helper.absoluteVec(new Vec3(1.5, 2, 5.5));
        ItemEntity item = new ItemEntity(helper.getLevel(), itemPos.x, itemPos.y, itemPos.z, Items.IRON_INGOT.getDefaultInstance());
        helper.getLevel().addFreshEntity(robot);
        helper.getLevel().addFreshEntity(item);
        IItemHandler inventory = robot.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(() -> new RuntimeException("Robot has no inventory"));
        helper.succeedWhen(() -> InventoryUtil.contains(inventory, Items.IRON_INGOT));
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testReflective(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        TestHelpers.addPerkToRobot(robots.first, ModPerks.PERK_REFLECTIVE.get());

        helper.onEachTick(() -> robots.first.setHealth(robots.first.getMaxHealth()));
        TestHelpers.succeedOnDamaged(helper, robots.second, "Reflective perk did not reflect any damage");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testFist(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        TestHelpers.addPerkToRobot(robots.second, ModPerks.PERK_FIST.get());

        int y = helper.absolutePos(new BlockPos(0, 2, 0)).getY();
        helper.succeedWhen(() -> {
            if(robots.first.getY() < y + 2) throw new GameTestAssertException("Fist perk did not knockup");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testInspire(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        RobotEntity ally = TestHelpers.setupDefaultRobot(helper);
        Player owner = helper.makeMockPlayer();
        robots.first.getCapability(ModCapabilities.ROBOT).orElseThrow(() -> new RuntimeException("Robot has no robot capability")).setOwner(owner.getUUID());
        ally.getCapability(ModCapabilities.ROBOT).orElseThrow(() -> new RuntimeException("Robot has no robot capability")).setOwner(owner.getUUID());
        TestHelpers.addPerkToRobot(robots.first, ModPerks.PERK_INSPIRE.get());
        helper.succeedWhen(() -> {
            if(!ally.hasEffect(MobEffects.MOVEMENT_SPEED) || !ally.hasEffect(MobEffects.DAMAGE_BOOST)) throw new GameTestAssertException("Inspire perk did not grant speed and strength");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testImpact(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        TestHelpers.addPerkToRobot(robots.second, ModPerks.PERK_IMPACT.get());
        robots.second.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100000, 4));

        TestHelpers.succeedOnDamaged(helper, robots.first, "Impact perk did not increase damage");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks", timeoutTicks = 250)
    public static void testArmorShred(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        RobotEntity robot3 = TestHelpers.setupDefaultRobot(helper);
        robot3.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> commands.addCommand(new RobotCommand(ModCommands.ATTACK.get(), List.of(Selection.of(new EntitySearch(robots.first.getUUID()))))));
        TestHelpers.addPerkToRobot(robot3, ModPerks.PERK_ARMOR_SHRED.get());

        helper.succeedWhen(() -> {
            robots.first.setHealth(robots.first.getMaxHealth());
            MobEffectInstance instance = robots.first.getEffect(ModMobEffects.ARMOR_SHRED.get());
            if(instance == null || instance.getAmplifier() < 2) throw new GameTestAssertException("Armor Shred perk does not stack");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "perks")
    public static void testMassProduced(GameTestHelper helper) {
        RobotEntity robot1 = TestHelpers.setupDefaultRobot(helper);
        RobotEntity robot2 = TestHelpers.setupDefaultRobot(helper);
        RobotEntity robot3 = TestHelpers.setupDefaultRobot(helper);
        TestHelpers.addPerkToRobot(robot1, ModPerks.PERK_MASS_PRODUCED.get());
        TestHelpers.addPerkToRobot(robot2, ModPerks.PERK_MASS_PRODUCED.get());
        TestHelpers.addPerkToRobot(robot3, ModPerks.PERK_MASS_PRODUCED.get());

        TestHelpers.succeedOnEffect(helper, robot1, MobEffects.DAMAGE_BOOST, "Mass Produced perk gives no buffs");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "perks")
    public static void testModuleBuff(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        TestHelpers.addPerkToRobot(robot, ModPerks.PERK_MODULE_BUFF.get());
        TestHelpers.addActionToRobot(robot, "shield");
        TestHelpers.activateAction(helper, robot, "shield");
        TestHelpers.succeedOnEffect(helper, robot, MobEffects.MOVEMENT_SPEED, "Module Buff perk did not apply speed");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testPrecious(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        RobotEntity entityToAid = TestHelpers.setupDefaultRobot(helper);
        TestHelpers.addPerkToRobot(robots.first, ModPerks.PERK_PRECIOUS.get());
        Player player = helper.makeMockPlayer();
        robots.first.getCapability(ModCapabilities.ROBOT).ifPresent(r -> r.setOwner(player.getUUID()));
        entityToAid.getCapability(ModCapabilities.ROBOT).ifPresent(r -> r.setOwner(player.getUUID()));

        helper.succeedWhen(() -> {
            robots.first.setHealth(robots.first.getMaxHealth());
            if(robots.second.getHealth() == robots.second.getMaxHealth() && !entityToAid.equals(robots.second.getLastAttacker())) throw new GameTestAssertException("Precious perk did not make allies aid");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testVoidant(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        TestHelpers.addPerkToRobot(robots.first, ModPerks.PERK_VOIDANT.get());
        robots.second.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 20));

        helper.succeedWhen(() -> {
            if(robots.second.hasEffect(MobEffects.LUCK)) {
                robots.second.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 20));
                throw new GameTestAssertException("Voidant perk did not reduce effects");
            }
        });
    }

    public static void testAttractant() {
        //TODO
    }

}
