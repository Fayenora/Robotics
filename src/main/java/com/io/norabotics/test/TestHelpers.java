package com.io.norabotics.test;

import com.google.common.base.Predicates;
import com.io.norabotics.common.capabilities.CommandApplyException;
import com.io.norabotics.common.capabilities.ModCapabilities;
import com.io.norabotics.common.capabilities.impl.perk.Perk;
import com.io.norabotics.common.content.entity.RobotEntity;
import com.io.norabotics.common.helpers.types.EntitySearch;
import com.io.norabotics.common.helpers.types.Selection;
import com.io.norabotics.common.helpers.types.Tuple;
import com.io.norabotics.common.robot.*;
import com.io.norabotics.definitions.robotics.ModCommands;
import com.io.norabotics.definitions.robotics.ModModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class TestHelpers {
    static void succeedOnDamaged(GameTestHelper helper, LivingEntity entity, String failMessage) {
        helper.runAfterDelay(1, () ->
                helper.succeedWhen(() -> {
                    if(entity.getHealth() == entity.getMaxHealth()) throw new GameTestAssertException(failMessage);
                })
        );
    }

    static void succeedOnEffect(GameTestHelper helper, LivingEntity entity, MobEffect effect, String failMessage) {
        helper.succeedWhen(() -> {
            if(!entity.hasEffect(effect)) throw new GameTestAssertException(failMessage);
        });
    }

    static void assertEntityInVicinity(GameTestHelper helper, EntityType<?> type, BlockPos pos) {
        helper.assertEntityPresent(type, Vec3.atCenterOf(pos).add(-1.5, -1, -1.5), Vec3.atCenterOf(pos).add(1.5, 1, 1.5));
    }

    static void assertEntityNotInVicinity(GameTestHelper helper, EntityType<?> type, BlockPos pos) {
        try {
            helper.assertEntityPresent(type, Vec3.atCenterOf(pos).add(-1, -1, -1), Vec3.atCenterOf(pos).add(1, 1, 1));
            throw new CommandApplyException("");
        } catch(GameTestAssertException ignored) {

        } catch (CommandApplyException e) {
            throw new GameTestAssertException("Unexpected " + type.toShortString() + " in vicinity of " + pos);
        }
    }

    static RobotEntity createIronRobot(Level level) {
        RobotEntity robot = new RobotEntity(level);
        robot.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
            for(EnumRobotPart part : EnumRobotPart.values()) {
                parts.setBodyPart(part, EnumRobotMaterial.IRON);
            }
        });
        return robot;
    }

    static void addActionToRobot(RobotEntity robot, String actionName) {
        addToRobot(robot, mod -> mod.getAction().toString().equals(actionName), Predicates.alwaysFalse());
    }

    static void addPerkToRobot(RobotEntity robot, Perk perkToAdd) {
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
                robot.getCapability(ModCapabilities.PARTS).ifPresent(r -> r.setBodyParts(slot, NonNullList.of(ItemStack.EMPTY, stack)));
                return;
            }
        }
    }

    static RobotEntity setupDefaultRobot(GameTestHelper helper) {
        RobotEntity robot = createIronRobot(helper.getLevel());
        robot.setPos(helper.absoluteVec(new Vec3(1.5, 2, 1.5)));
        helper.getLevel().addFreshEntity(robot);
        return robot;
    }

    static Tuple<RobotEntity, RobotEntity> setupDuel(GameTestHelper helper) {
        RobotEntity robot = createIronRobot(helper.getLevel());
        robot.setPos(helper.absoluteVec(new Vec3(1.5, 2, 2.5)));
        RobotEntity robot2 = createIronRobot(helper.getLevel());
        robot2.setPos(helper.absoluteVec(new Vec3(1.5, 2, 6.5)));
        addCommand(robot2, ModCommands.ATTACK.get(), new EntitySearch(robot.getUUID()));

        helper.getLevel().addFreshEntity(robot);
        helper.getLevel().addFreshEntity(robot2);
        return new Tuple<>(robot, robot2);
    }

    static void activateAction(GameTestHelper helper, RobotEntity entity, String action) {
        helper.runAfterDelay(1, () -> {
            entity.getCapability(ModCapabilities.PARTS).ifPresent(r -> {
                for(EnumModuleSlot slot : EnumModuleSlot.values()) {
                    for(ItemStack stack : r.getBodyParts(slot)) {
                        RobotModule module = ModModules.get(stack);
                        if(module != null && module.getAction().toString().equals(action)) {
                            module.activate(entity);
                            return;
                        }
                    }
                }
            });
        });
    }

    static RobotCommand addCommand(RobotEntity entity, CommandType type, Object selection) {
        RobotCommand command = new RobotCommand(type, List.of(Selection.of(selection)));
        entity.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> commands.addCommand(command));
        return command;
    }

    static RobotCommand addCommand(RobotEntity entity, CommandType type, Object selection, Object selection2) {
        RobotCommand command = new RobotCommand(type, List.of(Selection.of(selection), Selection.of(selection2)));
        entity.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> commands.addCommand(command));
        return command;
    }
}
