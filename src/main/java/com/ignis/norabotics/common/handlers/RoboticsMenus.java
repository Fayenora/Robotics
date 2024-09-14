package com.ignis.norabotics.common.handlers;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.common.access.AccessConfig;
import com.ignis.norabotics.common.access.EnumPermission;
import com.ignis.norabotics.common.capabilities.IRobot;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.content.blockentity.FactoryBlockEntity;
import com.ignis.norabotics.common.content.menu.FactoryMenu;
import com.ignis.norabotics.common.content.menu.RobotCommandMenu;
import com.ignis.norabotics.common.content.menu.RobotInfoMenu;
import com.ignis.norabotics.common.content.menu.RobotMenu;
import com.ignis.norabotics.common.helpers.util.Lang;
import com.ignis.norabotics.common.robot.EnumModuleSlot;
import com.ignis.norabotics.common.robot.RobotCommand;
import com.ignis.norabotics.definitions.ModAttributes;
import com.ignis.norabotics.definitions.ModMenuTypes;
import com.ignis.norabotics.integration.cc.vanilla.ScreenInvokator;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoboticsMenus {

    public static final List<Attribute> UNNECESSARY_INFO = List.of(
            ModAttributes.MODIFIER_SLOTS.get(EnumModuleSlot.HEAD),
            ModAttributes.MODIFIER_SLOTS.get(EnumModuleSlot.BODY),
            ModAttributes.MODIFIER_SLOTS.get(EnumModuleSlot.LEFT_ARM),
            ModAttributes.MODIFIER_SLOTS.get(EnumModuleSlot.LEFT_LEG),
            ModAttributes.MODIFIER_SLOTS.get(EnumModuleSlot.RIGHT_ARM),
            ModAttributes.MODIFIER_SLOTS.get(EnumModuleSlot.RIGHT_LEG)
    );
    public static final Map<RegistryObject<? extends MenuType<?>>, EnumSet<EnumPermission>> REQUIRED_PERMISSIONS = new HashMap<>();
    static {
        REQUIRED_PERMISSIONS.put(ModMenuTypes.ROBOT, EnumSet.of(EnumPermission.VIEW));
        REQUIRED_PERMISSIONS.put(ModMenuTypes.ROBOT_INFO, EnumSet.of(EnumPermission.VIEW));
        REQUIRED_PERMISSIONS.put(ModMenuTypes.ROBOT_COMMANDS, EnumSet.of(EnumPermission.VIEW, EnumPermission.COMMANDS));
        REQUIRED_PERMISSIONS.put(ModMenuTypes.COMPUTER, EnumSet.of(EnumPermission.VIEW, EnumPermission.COMMANDS));
    }

    public static void openMenu(Player player, MenuType<?> type, Object extraData) {
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        if(extraData instanceof Integer entityId) {
            openRobotMenu(player, type, player.level().getEntity(entityId));
        }
        if(extraData instanceof BlockPos pos) {
            BlockEntity be = player.level().getBlockEntity(pos);
            if(!(be instanceof FactoryBlockEntity factory)) return;
            if(type == ModMenuTypes.FACTORY.get()) {
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider((id, playerInv, f3) -> new FactoryMenu(id, playerInv, factory), Lang.localise("container.robot_factory")),
                        buf -> buf.writeBlockPos(pos));
            }
        }
    }

    public static boolean hasRequiredPermissions(Player player, MenuType<?> menuType, Entity target) {
        if(target.getCapability(ModCapabilities.ROBOT).isPresent()) {
            AccessConfig config = target.getCapability(ModCapabilities.ROBOT).resolve().get().getAccess();
            return hasRequiredPermissions(config, player, menuType);
        }
        return false;
    }

    public static boolean hasRequiredPermissions(AccessConfig config, Player player, MenuType<?> menuType) {
        for(EnumPermission permission : REQUIRED_PERMISSIONS.get(ModMenuTypes.of(menuType))) {
            if(!config.hasPermission(player, permission)) return false;
        }
        return true;
    }

    public static void openRobotMenu(Player player, MenuType<?> type, Entity target) {
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        if(target == null || !target.getCapability(ModCapabilities.ROBOT).isPresent()) return;
        if(!hasRequiredPermissions(player, type, target)) return;
        IRobot robot = target.getCapability(ModCapabilities.ROBOT).resolve().get();
        if(type == ModMenuTypes.ROBOT.get()) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, playerInv, f3) -> new RobotMenu(id, playerInv, target), Lang.localise("container.robot")),
                    buf -> {
                        buf.writeInt(target.getId());
                        robot.getAccess().write(buf);
                    });
        }
        if(type == ModMenuTypes.ROBOT_INFO.get()) {
            if(!(target instanceof LivingEntity living)) return;
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, playerInv, f3) -> new RobotInfoMenu(id, playerInv, target), Lang.localise("container.robot_info")),
                    buf -> {
                        buf.writeInt(target.getId());
                        robot.getAccess().write(buf);
                        for(Map.Entry<ResourceKey<Attribute>, Attribute> entry : ForgeRegistries.ATTRIBUTES.getEntries()) {
                            if(living.getAttributes().hasAttribute(entry.getValue()) && !UNNECESSARY_INFO.contains(entry.getValue())) {
                                buf.writeResourceKey(entry.getKey());
                                buf.writeFloat((float) living.getAttributes().getValue(entry.getValue()));
                            }
                        }
                    });
        }
        if(type == ModMenuTypes.ROBOT_COMMANDS.get()) {
            target.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> {
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider((id, f2, f3) -> new RobotCommandMenu(id, target), Lang.localise("container.robot_commands")),
                        buf -> {
                            buf.writeInt(target.getId());
                            CompoundTag tag = new CompoundTag();
                            RobotCommand.writeToNBT(tag, commands.getCommands()); //NOTE: NBT is suboptimal here, but sufficient as this is only called when opening the gui
                            buf.writeNbt(tag);
                            robot.getAccess().write(buf);
                        });
            });
        }
        if(type == ModMenuTypes.COMPUTER.get()) {
            if(!ModList.get().isLoaded(Reference.CC_MOD_ID)) return;
            target.getCapability(ModCapabilities.COMPUTERIZED).ifPresent(computer -> {
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider(
                                ScreenInvokator.invokeProgrammingMenu(target, computer),
                                Lang.localise("container.computer")),
                        buf -> {
                            new ComputerContainerData(computer.getComputer(), Items.APPLE.getDefaultInstance()).toBytes(buf);
                            buf.writeInt(target.getId());
                            robot.getAccess().write(buf);
                        });
            });
        }
    }
}
