package com.ignis.norabotics.test;

import com.google.common.base.Predicates;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.capabilities.impl.perk.Perk;
import com.ignis.norabotics.common.content.entity.RobotEntity;
import com.ignis.norabotics.common.helpers.types.EntitySearch;
import com.ignis.norabotics.common.helpers.types.Selection;
import com.ignis.norabotics.common.helpers.types.Tuple;
import com.ignis.norabotics.common.helpers.util.InventoryUtil;
import com.ignis.norabotics.common.robot.*;
import com.ignis.norabotics.definitions.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

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
        helper.runAfterDelay(1, () -> {
            if (robot.getHealth() < robot.getMaxHealth()) helper.fail("Robust perk did not protect robot from damage");
            else helper.succeed();
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

        helper.onEachTick(() -> robots.first.setHealth(robots.first.getMaxHealth()));
        succeedOnDamaged(helper, robots.second, "Reflective perk did not reflect any damage");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testFist(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = setupDuel(helper);
        addPerkToRobot(robots.second, ModPerks.PERK_FIST.get());

        int y = helper.absolutePos(new BlockPos(0, 2, 0)).getY();
        helper.succeedWhen(() -> {
            if(robots.first.getY() < y + 2) throw new GameTestAssertException("Fist perk did not knockup");
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
        addPerkToRobot(robots.second, ModPerks.PERK_IMPACT.get());
        robots.second.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100000, 4));

        succeedOnDamaged(helper, robots.first, "Impact perk did not increase damage");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testArmorShred(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = setupDuel(helper);
        RobotEntity robot3 = setupDefaultRobot(helper);
        robot3.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> commands.addCommand(new RobotCommand(ModCommands.ATTACK.get(), List.of(Selection.of(new EntitySearch(robots.first.getUUID()))))));
        addPerkToRobot(robot3, ModPerks.PERK_ARMOR_SHRED.get());

        helper.succeedWhen(() -> {
            MobEffectInstance instance = robots.first.getEffect(ModMobEffects.ARMOR_SHRED.get());
            if(instance == null || instance.getAmplifier() < 2) throw new GameTestAssertException("Armor Shred perk does not stack");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "perks")
    public static void testMassProduced(GameTestHelper helper) {
        RobotEntity robot1 = setupDefaultRobot(helper);
        RobotEntity robot2 = setupDefaultRobot(helper);
        RobotEntity robot3 = setupDefaultRobot(helper);
        addPerkToRobot(robot1, ModPerks.PERK_MASS_PRODUCED.get());
        addPerkToRobot(robot2, ModPerks.PERK_MASS_PRODUCED.get());
        addPerkToRobot(robot3, ModPerks.PERK_MASS_PRODUCED.get());

        helper.succeedWhen(() -> {
            if(!robot1.hasEffect(MobEffects.DAMAGE_BOOST)) throw new GameTestAssertException("Mass Produced perk gives no buffs");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "perks")
    public static void testModuleBuff(GameTestHelper helper) {
        RobotEntity robot = setupDefaultRobot(helper);
        addPerkToRobot(robot, ModPerks.PERK_MODULE_BUFF.get());
        addModuleToRobot(robot, "shield");
        helper.runAfterDelay(1, () -> {
            robot.getCapability(ModCapabilities.ROBOT).ifPresent(r -> {
                ModModules.get(r.getModules(EnumModuleSlot.CORE).get(0)).activate(robot);
            });
            helper.succeedIf(() -> {
                if(!robot.hasEffect(MobEffects.MOVEMENT_SPEED)) throw new GameTestAssertException("Module Buff perk gives no buffs");
            });
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "perks")
    public static void testPrecious(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = setupDuel(helper);
        RobotEntity entityToAid = setupDefaultRobot(helper);
        addPerkToRobot(robots.first, ModPerks.PERK_PRECIOUS.get());
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
        Tuple<RobotEntity, RobotEntity> robots = setupDuel(helper);
        addPerkToRobot(robots.first, ModPerks.PERK_VOIDANT.get());
        robots.second.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 20));

        helper.succeedWhen(() -> {
            if(robots.second.hasEffect(MobEffects.LUCK)) {
                robots.second.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 20));
                throw new GameTestAssertException("Voidant perk did not reduce effects");
            }
        });
    }

    private static void succeedOnDamaged(GameTestHelper helper, LivingEntity entity, String failMessage) {
        helper.runAfterDelay(1, () ->
                helper.succeedWhen(() -> {
                    if(entity.getHealth() == entity.getMaxHealth()) throw new GameTestAssertException(failMessage);
                })
        );
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

    private static void addModuleToRobot(RobotEntity robot, String actionName) {
        addToRobot(robot, mod -> mod.getAction().toString().equals(actionName), Predicates.alwaysFalse());
    }

    private static void addPerkToRobot(RobotEntity robot, Perk perkToAdd) {
        addToRobot(robot, mod -> mod.getPerks().contains(perkToAdd), r -> r.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(perkToAdd));
        if(!robot.getCapability(ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(perkToAdd)) {
            throw new RuntimeException("Perk " + perkToAdd + " is not present in any robot part");
        }
    }

    private static void addToRobot(RobotEntity robot, Predicate<RobotModule> modulePredicate, Predicate<RobotEntity> fulfilledWhen) {
        Iterator<RobotModule> modules = ModModules.getModules(robot.level().registryAccess()).iterator();
        while(!fulfilledWhen.test(robot)) {
            RobotModule module = modules.next();
            if(!modulePredicate.test(module)) continue;
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
                return;
            }
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
        robot2.setPos(helper.absoluteVec(new Vec3(1.5, 2, 6.5)));
        robot2.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> commands.addCommand(new RobotCommand(ModCommands.ATTACK.get(), List.of(Selection.of(new EntitySearch(robot.getUUID()))))));

        helper.getLevel().addFreshEntity(robot);
        helper.getLevel().addFreshEntity(robot2);
        return new Tuple<>(robot, robot2);
    }
}
