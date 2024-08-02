package com.ignis.igrobotics.test;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.perks.Perk;
import com.ignis.igrobotics.core.robot.*;
import com.ignis.igrobotics.core.util.InventoryUtil;
import com.ignis.igrobotics.core.util.Tuple;
import com.ignis.igrobotics.definitions.ModCommands;
import com.ignis.igrobotics.definitions.ModModules;
import com.ignis.igrobotics.definitions.ModPerks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

@GameTestHolder(Robotics.MODID)
public class PerkTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "perks")
    public static void testInvulnerability(GameTestHelper helper) {
        RobotEntity robot = setupDefaultRobot(helper);
        addPerkToRobot(robot, ModPerks.PERK_INVULNERABILITY.get());
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
        RobotEntity robot = setupDefaultRobot(helper);
        addPerkToRobot(robot, ModPerks.PERK_THERMAL_CONDUCTIVITY.get());

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
        RobotEntity robot = setupDefaultRobot(helper);
        addPerkToRobot(robot, ModPerks.PERK_ROBUST.get());
        robot.hurt(robot.damageSources().generic(), 1);
        helper.succeedIf(() -> {
            if(robot.getHealth() < robot.getMaxHealth()) throw new GameTestAssertException("Robust perk did not protect robot from damage");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testMagnetic(GameTestHelper helper) {
        RobotEntity robot = createIronRobot(helper.getLevel());
        addPerkToRobot(robot, ModPerks.PERK_MAGNETIC.get());
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
        Tuple<RobotEntity, RobotEntity> robots = setupDuel(helper);
        addPerkToRobot(robots.first, ModPerks.PERK_REFLECTIVE.get());

        helper.succeedWhen(() -> {
            robots.first.setHealth(robots.first.getMaxHealth());
            if(robots.second.getHealth() == robots.second.getMaxHealth()) throw new GameTestAssertException("Reflective perk did not reflect any damage");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testInspire(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = setupDuel(helper);
        RobotEntity ally = setupDefaultRobot(helper);
        Player owner = helper.makeMockPlayer();
        robots.first.getCapability(ModCapabilities.ROBOT).orElseThrow(() -> new RuntimeException("Robot has no robot capability")).setOwner(owner.getUUID());
        ally.getCapability(ModCapabilities.ROBOT).orElseThrow(() -> new RuntimeException("Robot has no robot capability")).setOwner(owner.getUUID());
        addPerkToRobot(robots.first, ModPerks.PERK_INSPIRE.get());
        helper.succeedWhen(() -> {
            if(!ally.hasEffect(MobEffects.MOVEMENT_SPEED) || !ally.hasEffect(MobEffects.DAMAGE_BOOST)) throw new GameTestAssertException("Inspire perk did not grant speed and strength");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testImpact(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = setupDuel(helper);
        addPerkToRobot(robots.first, ModPerks.PERK_IMPACT.get());
        robots.second.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100000, 4));

        helper.succeedWhen(() -> {
            if(robots.second.getHealth() == robots.second.getMaxHealth()) throw new GameTestAssertException("Impact perk did not increase damage");
        });
    }

    private static RobotEntity createIronRobot(Level level) {
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

    private static RobotEntity setupDefaultRobot(GameTestHelper helper) {
        RobotEntity robot = createIronRobot(helper.getLevel());
        robot.setPos(helper.absoluteVec(new Vec3(1.5, 2, 1.5)));
        helper.getLevel().addFreshEntity(robot);
        return robot;
    }

    private static Tuple<RobotEntity, RobotEntity> setupDuel(GameTestHelper helper) {
        RobotEntity robot = createIronRobot(helper.getLevel());
        robot.setPos(helper.absoluteVec(new Vec3(1.5, 2, 2.5)));
        RobotEntity robot2 = createIronRobot(helper.getLevel());
        robot.setPos(helper.absoluteVec(new Vec3(1.5, 2, 6.5)));
        robot2.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> commands.addCommand(new RobotCommand(ModCommands.ATTACK.get(), List.of(Selection.of(new EntitySearch(robot.getUUID()))))));

        helper.getLevel().addFreshEntity(robot);
        helper.getLevel().addFreshEntity(robot2);
        return new Tuple<>(robot, robot2);
    }

    @AfterBatch(batch = "perks")
    public static void cleanup(GameTestHelper helper) {
        helper.killAllEntities();
    }
}
