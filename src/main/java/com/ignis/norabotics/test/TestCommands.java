package com.ignis.norabotics.test;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.content.entity.RobotEntity;
import com.ignis.norabotics.common.helpers.types.EntitySearch;
import com.ignis.norabotics.common.helpers.types.Tuple;
import com.ignis.norabotics.common.helpers.util.InventoryUtil;
import com.ignis.norabotics.common.robot.RobotCommand;
import com.ignis.norabotics.definitions.ModEntityTypes;
import com.ignis.norabotics.definitions.robotics.ModCommands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import java.util.Optional;

@GameTestHolder(Robotics.MODID)
public class TestCommands {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testAttackUUID(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        TestHelpers.succeedOnDamaged(helper, robots.first, "Attack command by UUID does not work");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testAttackName(GameTestHelper helper) {
        RobotEntity robot1 = TestHelpers.setupDefaultRobot(helper);
        RobotEntity robot2 = TestHelpers.setupDefaultRobot(helper);
        robot2.setPos(robot2.position().add(0, 0, 4));
        robot1.setCustomName(Component.literal("Jared"));
        TestHelpers.addCommand(robot2, ModCommands.ATTACK.get(), new EntitySearch("Jared"));
        TestHelpers.succeedOnDamaged(helper, robot1, "Attack command by Name does not work");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testAttackType(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        LivingEntity sheep = helper.spawn(EntityType.SHEEP, robot.position());
        robot.setPos(robot.position().add(0, 0, 4));
        TestHelpers.addCommand(robot, ModCommands.ATTACK.get(), new EntitySearch(EntityType.SHEEP));
        TestHelpers.succeedOnDamaged(helper, sheep, "Attack command by Type does not work");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands", timeoutTicks = 500)
    public static void testAttackManyTargets(GameTestHelper helper) {
        RobotEntity robot1 = TestHelpers.setupDefaultRobot(helper);
        LivingEntity pig = helper.spawn(EntityType.PIG, robot1.position());
        RobotEntity robot3 = TestHelpers.setupDefaultRobot(helper);
        pig.setPos(pig.position().add(0, 0, 1));
        robot3.setPos(robot3.position().add(0, 0, 4));
        robot1.setCustomName(Component.literal("Justin"));
        pig.setCustomName(Component.literal("Justin"));
        TestHelpers.addCommand(robot3, ModCommands.ATTACK.get(), new EntitySearch("Justin"));
        robot3.setItemSlot(EquipmentSlot.MAINHAND, Items.NETHERITE_SWORD.getDefaultInstance());
        TestHelpers.succeedOnDamaged(helper, robot1, "Attack command did not re-target entities with the same name");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testRangedCrossbow(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        helper.setBlock(robots.first.blockPosition().offset(0, 0, 2), Blocks.OAK_FENCE.defaultBlockState());
        robots.second.setItemSlot(EquipmentSlot.MAINHAND, Items.CROSSBOW.getDefaultInstance());
        Optional<IItemHandler> inv = robots.second.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if(inv.isEmpty()) {
            helper.fail("Robot does not have an inventory");
            return;
        }
        InventoryUtil.insert(inv.get(), new ItemStack(Items.ARROW, 64), false);
        TestHelpers.succeedOnDamaged(helper, robots.first, "");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testRangedBow(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        helper.getLevel().setBlockAndUpdate(robots.first.blockPosition().offset(0, 0, 2), Blocks.OAK_FENCE.defaultBlockState());
        robots.second.setItemSlot(EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
        Optional<IItemHandler> inv = robots.second.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if(inv.isEmpty()) {
            helper.fail("Robot does not have an inventory");
            return;
        }
        InventoryUtil.insert(inv.get(), new ItemStack(Items.ARROW, 64), false);
        TestHelpers.succeedOnDamaged(helper, robots.first, "");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testThrowPotion(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        helper.setBlock(robots.first.blockPosition().offset(0, 0, 2), Blocks.OAK_FENCE.defaultBlockState());
        ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.SLOWNESS);
        robots.second.setItemSlot(EquipmentSlot.MAINHAND, potion);
        TestHelpers.succeedOnEffect(helper, robots.first, MobEffects.MOVEMENT_SLOWDOWN, "Robot did not throw potion (or missed a stationary target?)");
    }

    /* Whether the robot switches to melee when arrows dwindled */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testRangedSwitchToMeleeCombat(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        TestHelpers.addActionToRobot(robots.first, "shield");
        TestHelpers.activateAction(helper, robots.first, "shield");
        robots.second.setPos(robots.second.position().add(0, 0, 2));
        robots.second.setItemSlot(EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
        Optional<IItemHandler> inv = robots.second.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if(inv.isEmpty()) {
            helper.fail("Robot does not have an inventory");
            return;
        }
        InventoryUtil.insert(inv.get(), new ItemStack(Items.ARROW, 1), false);
        helper.runAfterDelay(40, () -> TestHelpers.activateAction(helper, robots.first, "shield"));
        TestHelpers.succeedOnDamaged(helper, robots.first, "Robot did not switch to melee fight");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands", timeoutTicks = 200)
    public static void testDefend(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        RobotEntity protector = TestHelpers.createIronRobot(helper.getLevel());
        protector.setPos(protector.position().add(0, 0, 6));
        TestHelpers.addCommand(protector, ModCommands.DEFEND.get(), new EntitySearch(robots.first.getUUID()));
        TestHelpers.succeedOnDamaged(helper, robots.second, "Defend command did not make robot damage the attacker");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testSelfDefense(GameTestHelper helper) {
        Tuple<RobotEntity, RobotEntity> robots = TestHelpers.setupDuel(helper);
        TestHelpers.addCommand(robots.first, ModCommands.DEFEND.get(), new EntitySearch(robots.first.getUUID()));
        TestHelpers.succeedOnDamaged(helper, robots.second, "Self-Defense did not work");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testFollow(GameTestHelper helper) {
        RobotEntity follower = TestHelpers.setupDefaultRobot(helper);
        RobotEntity leader = TestHelpers.setupDefaultRobot(helper);
        TestHelpers.addCommand(leader, ModCommands.STAY.get(), GlobalPos.of(helper.getLevel().dimension(), BlockPos.containing(helper.absoluteVec(new Vec3(1.5, 2, 6.5)))));
        TestHelpers.addCommand(follower, ModCommands.FOLLOW.get(), new EntitySearch(leader.getUUID()), 1);
        helper.succeedWhen(() -> {
            if(follower.getBlockZ() < helper.absolutePos(new BlockPos(0, 0, 3)).getZ()) throw new GameTestAssertException("Robot did not follow other robot");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "portal", batch = "commands", timeoutTicks = 300)
    public static void testFollowThroughDimension(GameTestHelper helper) {
        RobotEntity follower = TestHelpers.setupDefaultRobot(helper);
        RobotEntity leader = TestHelpers.setupDefaultRobot(helper);
        double scale = 1d / 8;
        GlobalPos pos = GlobalPos.of(Level.NETHER, new BlockPos((int) (follower.getBlockX() * scale), follower.getBlockY(), (int) (follower.getBlockZ() * scale)));
        TestHelpers.addCommand(leader, ModCommands.STAY.get(), pos);
        TestHelpers.addCommand(follower, ModCommands.FOLLOW.get(), new EntitySearch(leader.getUUID()), 6);
        helper.succeedWhen(() -> {
            if(follower.level().dimension().equals(Level.OVERWORLD)) throw new GameTestAssertException("Robot did not switch dimension");
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands", timeoutTicks = 200)
    public static void testTakeItems(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        BlockPos chestLocation = new BlockPos(1, 2, 6);
        helper.setBlock(chestLocation, Blocks.CHEST);
        BlockEntity chest = helper.getBlockEntity(chestLocation);
        if(chest == null) {
            helper.fail("Could not place chest");
            return;
        }
        chest.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
            inv.insertItem(1, new ItemStack(Items.BLUE_CONCRETE, 4), false);
            inv.insertItem(4, new ItemStack(Items.BLUE_CONCRETE, 3), false);
        });
        TestHelpers.addCommand(robot, ModCommands.RETRIEVE.get(), Items.BLUE_CONCRETE.getDefaultInstance(), GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(chestLocation)));
        helper.succeedWhen(() -> {
            chest.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
                if(InventoryUtil.contains(inv, Items.BLUE_CONCRETE)) throw new GameTestAssertException("Robot did not take items from chest");
            });
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands", timeoutTicks = 200)
    public static void testDontTakeItems(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        BlockPos chestLocation = new BlockPos(1, 2, 6);
        helper.setBlock(chestLocation, Blocks.CHEST);
        BlockEntity chest = helper.getBlockEntity(chestLocation);
        if(chest == null) {
            helper.fail("Could not place chest");
            return;
        }
        chest.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
            inv.insertItem(1, new ItemStack(Items.BLUE_CONCRETE, 4), false);
            inv.insertItem(2, new ItemStack(Items.IRON_SWORD), false);
            inv.insertItem(3, new ItemStack(Items.BLUE_CONCRETE, 2), false);
        });
        TestHelpers.addCommand(robot, ModCommands.RETRIEVE.get(), Items.BLUE_CONCRETE.getDefaultInstance(), GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(chestLocation)));
        helper.runAfterDelay(100, () -> {
            helper.succeedWhen(() -> {
                chest.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
                    if(!InventoryUtil.contains(inv, Items.IRON_SWORD)) throw new GameTestAssertException("Robot did not take item from chest it was not supposed to");
                });
            });
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands", timeoutTicks = 200)
    public static void testPutItems(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        BlockPos chestLocation = new BlockPos(1, 2, 6);
        helper.setBlock(chestLocation, Blocks.CHEST);
        BlockEntity chest = helper.getBlockEntity(chestLocation);
        if(chest == null) {
            helper.fail("Could not place chest");
            return;
        }
        robot.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
            inv.insertItem(7, new ItemStack(Items.BLUE_CONCRETE, 4), false);
            inv.insertItem(8, new ItemStack(Items.IRON_SWORD), false);
            inv.insertItem(9, new ItemStack(Items.BLUE_CONCRETE, 2), false);
        });
        TestHelpers.addCommand(robot, ModCommands.STORE.get(), Items.BLUE_CONCRETE.getDefaultInstance(), GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(chestLocation)));
        helper.succeedWhen(() -> {
            chest.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
                if(InventoryUtil.count(inv, Items.BLUE_CONCRETE) < 6) throw new GameTestAssertException("Robot did put all items in chest");
            });
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands", timeoutTicks = 400)
    public static void testTransportItems(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        robot.setPos(robot.position().add(0, 0, 1));
        BlockPos chestLoc1 = new BlockPos(1, 2, 1);
        BlockPos chestLoc2 = new BlockPos(1, 2, 9);
        helper.setBlock(chestLoc1, Blocks.CHEST);
        helper.setBlock(chestLoc2, Blocks.CHEST);
        BlockEntity chest1 = helper.getBlockEntity(chestLoc1);
        BlockEntity chest2 = helper.getBlockEntity(chestLoc2);
        if(chest1 == null || chest2 == null) {
            helper.fail("Could not place chests");
            return;
        }
        chest1.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
            inv.insertItem(5, new ItemStack(Items.BLUE_CONCRETE, 4), false);
            inv.insertItem(2, new ItemStack(Items.IRON_SWORD), false);
            inv.insertItem(7, new ItemStack(Items.BLUE_CONCRETE, 2), false);
        });
        TestHelpers.addCommand(robot, ModCommands.RETRIEVE.get(), Items.BLUE_CONCRETE.getDefaultInstance(), GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(chestLoc1)));
        TestHelpers.addCommand(robot, ModCommands.STORE.get(), Items.BLUE_CONCRETE.getDefaultInstance(), GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(chestLoc2)));
        helper.succeedWhen(() -> {
            chest2.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
                if(InventoryUtil.count(inv, Items.BLUE_CONCRETE) < 6) throw new GameTestAssertException("Robot did not put all items in chest");
            });
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "portal", batch = "commands", timeoutTicks = 800)
    public static void testTransportThroughDimension(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        if(nether == null) return;
        double coordinateScale = 1 / nether.dimensionType().coordinateScale();
        BlockPos chestLoc1 = new BlockPos(1, 2, 1);
        BlockPos chestLoc2 = BlockPos.containing(helper.absolutePos(new BlockPos(1, 2, 6)).getCenter().scale(coordinateScale)).atY(robot.getBlockY());
        helper.setBlock(chestLoc1, Blocks.CHEST);
        nether.setBlockAndUpdate(chestLoc2, Blocks.CHEST.defaultBlockState());
        BlockEntity chest1 = helper.getBlockEntity(chestLoc1);
        BlockEntity chest2 = nether.getBlockEntity(chestLoc2);
        if(chest1 == null || chest2 == null) {
            helper.fail("Could not place chests");
            return;
        }
        chest1.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
            inv.insertItem(5, new ItemStack(Items.BLUE_CONCRETE, 4), false);
            inv.insertItem(2, new ItemStack(Items.IRON_SWORD), false);
            inv.insertItem(7, new ItemStack(Items.BLUE_CONCRETE, 2), false);
        });
        TestHelpers.addCommand(robot, ModCommands.RETRIEVE.get(), Items.BLUE_CONCRETE.getDefaultInstance(), GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(chestLoc1)));
        TestHelpers.addCommand(robot, ModCommands.STORE.get(), Items.BLUE_CONCRETE.getDefaultInstance(), GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(chestLoc2)));
        helper.succeedWhen(() -> {
            chest2.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inv -> {
                if(InventoryUtil.count(inv, Items.BLUE_CONCRETE) < 6) throw new GameTestAssertException("Robot did put all items in chest");
            });
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "commands", timeoutTicks = 800)
    public static void testBreak(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        BlockPos pos1 = new BlockPos(0, 2, 0);
        BlockPos pos2 = new BlockPos(2, 2, 2);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos1));
        GlobalPos globalPos2 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos2));
        TestHelpers.addCommand(robot, ModCommands.BREAK.get(), globalPos1, globalPos2);
        helper.succeedWhen(() -> {
            for(int i = 0; i < 2; i++) {
                for(int j = 0; j  < 2; j++) {
                    helper.assertBlockPresent(Blocks.AIR, pos1.offset(i, 0, j));
                }
            }
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "commands", timeoutTicks = 400)
    public static void testBreakSingleBlock(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        BlockPos pos1 = new BlockPos(0, 2, 0);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos1));
        TestHelpers.addCommand(robot, ModCommands.BREAK.get(), globalPos1, globalPos1);
        helper.succeedWhenBlockPresent(Blocks.AIR, pos1);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands", timeoutTicks = 400)
    public static void testBreakSpeed(GameTestHelper helper) {
        RobotEntity robot1 = TestHelpers.setupDefaultRobot(helper);
        RobotEntity robot2 = TestHelpers.setupDefaultRobot(helper);
        robot1.setItemSlot(EquipmentSlot.MAINHAND, Items.IRON_PICKAXE.getDefaultInstance());
        robot2.setItemSlot(EquipmentSlot.MAINHAND, Items.DIAMOND_PICKAXE.getDefaultInstance());
        robot1.setPos(robot1.position().add(0, 0, 1));
        robot2.setPos(robot2.position().add(0, 0, 7));
        BlockPos pos1 = new BlockPos(1, 2, 1);
        BlockPos pos2 = new BlockPos(1, 2, 9);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos1));
        GlobalPos globalPos2 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos2));
        helper.setBlock(pos1, Blocks.DIAMOND_ORE);
        helper.setBlock(pos2, Blocks.DIAMOND_ORE);
        TestHelpers.addCommand(robot1, ModCommands.BREAK.get(), globalPos1, globalPos1);
        TestHelpers.addCommand(robot2, ModCommands.BREAK.get(), globalPos2, globalPos2);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.DIAMOND_ORE, pos1);
            helper.assertBlockNotPresent(Blocks.DIAMOND_ORE, pos2);
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "commands", timeoutTicks = 400)
    public static void testReBreakBlock(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        robot.setItemSlot(EquipmentSlot.MAINHAND, Items.DIAMOND_PICKAXE.getDefaultInstance());
        BlockPos pos1 = new BlockPos(0, 2, 0);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos1));
        helper.setBlock(pos1, Blocks.AIR);
        TestHelpers.addCommand(robot, ModCommands.BREAK.get(), globalPos1, globalPos1);
        helper.runAfterDelay(20, () -> {
            helper.setBlock(pos1, Blocks.DIAMOND_ORE);
        });
        helper.runAfterDelay(30, () -> {
            helper.succeedWhenBlockPresent(Blocks.AIR, pos1);
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testPlace(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        robot.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Blocks.DIAMOND_BLOCK, 3));
        BlockPos pos1 = new BlockPos(1, 3, 3);
        BlockPos pos2 = new BlockPos(1, 4, 4);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), pos1);
        GlobalPos globalPos2 = GlobalPos.of(helper.getLevel().dimension(), pos2);
        TestHelpers.addCommand(robot, ModCommands.PLACE.get(), globalPos1, globalPos2);
        helper.succeedWhen(() -> {
            for(int i = 0; i < 2; i++) {
                for(int j = 0; j < 2; j++) {
                    helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, pos1.offset(0, i, j));
                }
            }
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testPlaceSingleBlock(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        robot.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Blocks.DIAMOND_BLOCK));
        BlockPos pos1 = new BlockPos(1, 2, 3);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos1));
        TestHelpers.addCommand(robot, ModCommands.PLACE.get(), globalPos1, globalPos1);
        helper.succeedWhenBlockPresent(Blocks.DIAMOND_BLOCK, pos1);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commands")
    public static void testPlaceBlockOnSelf(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        robot.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Blocks.DIAMOND_BLOCK));
        BlockPos pos1 = new BlockPos(1, 2, 1);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos1));
        TestHelpers.addCommand(robot, ModCommands.PLACE.get(), globalPos1, globalPos1);
        helper.succeedWhenBlockPresent(Blocks.DIAMOND_BLOCK, pos1);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "commands")
    public static void testRePlaceBlock(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        robot.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Blocks.DIAMOND_BLOCK));
        BlockPos pos1 = new BlockPos(1, 2, 0);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos1));
        TestHelpers.addCommand(robot, ModCommands.PLACE.get(), globalPos1, globalPos1);
        helper.runAfterDelay(20, () -> {
            helper.setBlock(helper.relativePos(globalPos1.pos()), Blocks.AIR);
        });
        helper.runAfterDelay(30, () -> {
            helper.succeedWhenBlockPresent(Blocks.DIAMOND_BLOCK, pos1);
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "racetrack", batch = "commands")
    public static void testCommandPriorities(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        BlockPos pos1 = new BlockPos(1, 2, 6);
        BlockPos pos2 = new BlockPos(1, 2, 16);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos1));
        GlobalPos globalPos2 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos2));
        TestHelpers.addCommand(robot, ModCommands.STAY.get(), globalPos1);
        TestHelpers.addCommand(robot, ModCommands.STAY.get(), globalPos2);
        helper.runAfterDelay(100, () -> helper.succeedIf(() -> {
            TestHelpers.assertEntityInVicinity(helper, ModEntityTypes.ROBOT.get(), globalPos1.pos());
        }));
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "racetrack", batch = "commands", timeoutTicks = 200)
    public static void testRemoveCommand(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        BlockPos pos1 = new BlockPos(1, 2, 6);
        BlockPos pos2 = new BlockPos(1, 2, 16);
        GlobalPos globalPos1 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos1));
        GlobalPos globalPos2 = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos2));
        RobotCommand command = TestHelpers.addCommand(robot, ModCommands.STAY.get(), globalPos1);
        TestHelpers.addCommand(robot, ModCommands.STAY.get(), globalPos2);
        helper.runAfterDelay(100, () -> robot.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> commands.removeCommand(command)));
        helper.runAfterDelay(190, () -> helper.succeedIf(() -> {
            TestHelpers.assertEntityInVicinity(helper, ModEntityTypes.ROBOT.get(), globalPos2.pos());
        }));
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "racetrack", batch = "commands", timeoutTicks = 200)
    public static void testCommandsOnShutdown(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        BlockPos pos = new BlockPos(1, 2, 32);
        GlobalPos globalPos = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos));
        TestHelpers.addCommand(robot, ModCommands.STAY.get(), globalPos);
        helper.onEachTick(() -> {
            if(robot.getZ() > helper.absolutePos(new BlockPos(0, 0, 16)).getZ()) {
                robot.getCapability(ModCapabilities.ROBOT).ifPresent(r -> r.setActivation(false));
            }
        });
        helper.runAfterDelay(190, () -> helper.succeedIf(() -> {
            TestHelpers.assertEntityNotInVicinity(helper, ModEntityTypes.ROBOT.get(), globalPos.pos());
        }));
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "racetrack", batch = "commands", timeoutTicks = 400)
    public static void testCommandsOnReboot(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        BlockPos pos = new BlockPos(2, 2, 32);
        GlobalPos globalPos = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(pos));
        TestHelpers.addCommand(robot, ModCommands.STAY.get(), globalPos);
        helper.onEachTick(() -> {
            boolean shouldBeActive = robot.getZ() < helper.absolutePos(new BlockPos(0, 0, 16)).getZ();
            robot.getCapability(ModCapabilities.ROBOT).ifPresent(r -> r.setActivation(shouldBeActive || helper.getTick() > 200));
        });
        helper.runAfterDelay(390, () -> helper.succeedIf(() -> {
            TestHelpers.assertEntityInVicinity(helper, ModEntityTypes.ROBOT.get(), globalPos.pos());
        }));
    }
}
