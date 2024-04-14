package com.ignis.igrobotics.common.items;

import com.ignis.igrobotics.client.menu.CommanderMenu;
import com.ignis.igrobotics.common.WorldData;
import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.common.entity.ai.QuickMoveToBlock;
import com.ignis.igrobotics.common.handlers.RobotBehavior;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.core.util.EntityFinder;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.robot.RobotCommand;
import com.ignis.igrobotics.core.robot.RobotView;
import com.ignis.igrobotics.core.robot.Selection;
import com.ignis.igrobotics.core.util.InventoryUtil;
import com.ignis.igrobotics.core.util.PosUtil;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModCommands;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommanderItem extends Item {

    public static final String NBT_ID = "id";
    public static final String NBT_ENTITY = "selected";
    public static final String NBT_ENTITY_CACHE = "selected_cache";
    public static final String NBT_POS = "selected_pos";

    public CommanderItem() {
        super(new Properties().stacksTo(1));
    }

    private static CompoundTag getTagCompound(ItemStack stack) {
        if(!stack.hasTag()) {
            CompoundTag nbt = new CompoundTag();
            stack.setTag(nbt);
            return nbt;
        }
        return stack.getTag();
    }

    @Override
    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
        assignID(pStack);
    }

    //Called on entity right click
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if(player.level().isClientSide()) return InteractionResult.SUCCESS;
        Optional<IRobot> optRobot = target.getCapability(ModCapabilities.ROBOT).resolve();
        LivingEntity currentEntity = getRememberedEntity(player.level(), stack);

        if(player.isShiftKeyDown() && optRobot.isPresent()) {
            IRobot robot = optRobot.get();
            if(!robot.hasAccess(player, EnumPermission.COMMANDS)) return InteractionResult.PASS;

            assignID(stack);
            int commandGroup = getID(stack);
            if(robot.getCommandGroup() == commandGroup) {
                robot.setCommandGroup(0);
                player.sendSystemMessage(Component.translatable("commandGroup.robot.remove"));
            } else {
                robot.setCommandGroup(commandGroup);
                player.sendSystemMessage(Component.translatable("commandGroup.robot.add"));
            }
            return InteractionResult.CONSUME;
        }

        if(currentEntity instanceof Mob mob && currentEntity.getCapability(ModCapabilities.COMMANDS).isPresent()) {
            Selection<EntitySearch> targetSelection = Selection.of(new EntitySearch(target.getUUID()));
            if(!target.getType().getCategory().isFriendly()) {
                RobotCommand attack = new RobotCommand(ModCommands.ATTACK_SPECIFIC, List.of(targetSelection));
                if(addNewCommand(player, mob, attack, true, "commandGroup.command.attack", target.getDisplayName())) {
                    return InteractionResult.CONSUME;
                }
            } else if(target.getType().getCategory() == MobCategory.CREATURE) {
                RobotCommand defend = new RobotCommand(ModCommands.DEFEND, List.of(targetSelection));
                if(addNewCommand(player, mob, defend, false, "commandGroup.command.defend", target.getDisplayName())) {
                    return InteractionResult.CONSUME;
                }
            }
        }

        rememberEntity(stack, target);
        player.sendSystemMessage(Component.translatable("commandGroup.selected.robot", target.getDisplayName()));
        return InteractionResult.CONSUME;
    }

    //Called on block right click
    @Override
    public InteractionResult useOn(UseOnContext cxt) {
        ItemStack stack = cxt.getItemInHand();
        Level level = cxt.getLevel();
        Player player = cxt.getPlayer();
        BlockPos pos = cxt.getClickedPos();
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        
        if(level.isClientSide()) return InteractionResult.SUCCESS;

        GlobalPos savedPos = getRememberedPos(stack);
        LivingEntity living = getRememberedEntity(level, stack);

        //If the commander remembers a robot, either add a fitting command based on circumstances, or simply make it move there

        if(living instanceof Mob mob && living.getCapability(ModCapabilities.ROBOT).isPresent()) {
            IRobot robot = living.getCapability(ModCapabilities.ROBOT).resolve().get();
            if(robot.isActive()) {
                addContextDependentCommand(mob, cxt);
                return InteractionResult.CONSUME;
            }
        }

        //If the commander is pointing towards a robot in storage, make it exit and move here

        if(savedPos != null) {
            Level savedLevel = ServerLifecycleHooks.getCurrentServer().getLevel(savedPos.dimension());
            if(savedLevel != null) {
                BlockState state = savedLevel.getBlockState(savedPos.pos());
                if(state.getBlock() == ModBlocks.ROBOT_STORAGE.get()) {
                    BlockEntity storage = savedLevel.getBlockEntity(state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? savedPos.pos().below() : savedPos.pos());
                    if(orderEntityOutOfStorage(storage, globalPos)) {
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        //Otherwise just remember this position (for f.e. commands)
        CommanderItem.rememberPos(stack, globalPos);
        cxt.getPlayer().sendSystemMessage(Component.translatable("commandGroup.selected.pos"));

        return InteractionResult.CONSUME;
    }

    //Called on empty right click
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(level.isClientSide()) return InteractionResultHolder.success(stack);

        if(player.isShiftKeyDown()) {
            if(getRememberedPos(stack) != null) {
                clearRememberedPos(stack);
                player.sendSystemMessage(Component.translatable("commandGroup.cleared.pos"));
                return InteractionResultHolder.consume(stack);
            }
            if(getRememberedEntity(level, stack) != null) {
                clearRememberedEntity(stack);
                player.sendSystemMessage(Component.translatable("commandGroup.cleared.robot"));
                return InteractionResultHolder.consume(stack);
            }
            return InteractionResultHolder.pass(stack);
        }

        assignID(stack);
        int commandGroup = getID(stack);
        Collection<RobotView> robotsOfCommandGroupView = WorldData.get().getRobotsOfCommandGroup(commandGroup);
        NetworkHooks.openScreen((ServerPlayer) player,
                new SimpleMenuProvider((id, ign1, ign2) -> new CommanderMenu(id, robotsOfCommandGroupView), stack.getHoverName().copy().withStyle(Style.EMPTY)),
                buf -> RobotView.writeViews(buf, robotsOfCommandGroupView));

        return InteractionResultHolder.consume(stack);
    }

    private void addContextDependentCommand(Mob mob, UseOnContext cxt) {
        Player player = cxt.getPlayer();
        Level level = cxt.getLevel();
        BlockPos pos = cxt.getClickedPos();
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity != null && blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            if(blockEntity instanceof StorageBlockEntity) return;
            if(state.getAnalogOutputSignal(level, pos) > 10) {
                ItemStack dominantItem = InventoryUtil.dominantItem(blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get());
                RobotCommand retrieve = new RobotCommand(ModCommands.RETRIEVE, List.of(Selection.of(dominantItem), Selection.of(globalPos)));
                if(addNewCommand(player, mob, retrieve, true, "commandGroup.command.retrieve", dominantItem.getItem())) {
                    return;
                }
            } else if(mob.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                ItemStack dominantItem = InventoryUtil.dominantItem(mob.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get());
                RobotCommand store = new RobotCommand(ModCommands.STORE, List.of(Selection.of(dominantItem), Selection.of(globalPos)));
                if(addNewCommand(player, mob, store, true, "commandGroup.command.store", dominantItem.getItem())) {
                    return;
                }
            }
        }
        if(mob.getMainHandItem().getItem() instanceof BlockItem) {
            GlobalPos sidePos = GlobalPos.of(globalPos.dimension(), pos.relative(cxt.getClickedFace()));
            RobotCommand place = new RobotCommand(ModCommands.PLACE, List.of(Selection.of(sidePos), Selection.of(sidePos)));
            if(addNewCommand(player, mob, place, true, "commandGroup.command.place")) {
                return;
            }
        }
        if(mob.getMainHandItem().isCorrectToolForDrops(level.getBlockState(pos))) {
            RobotCommand break_command = new RobotCommand(ModCommands.BREAK, List.of(Selection.of(globalPos), Selection.of(globalPos)));
            if(addNewCommand(player, mob, break_command, true, "commandGroup.command.break")) {
                return;
            }
        }
        mob.goalSelector.addGoal(2, new QuickMoveToBlock(mob, globalPos));
        RobotBehavior.playAggressionSound(mob);
    }

    private boolean addNewCommand(Player player, Mob mob, RobotCommand command, boolean strict, String message, Object... additionalInfo) {
        if(!mob.getCapability(ModCapabilities.COMMANDS).isPresent()) return false;
        if(player.level().isClientSide) return false;
        if(strict) {
            Goal goal = command.getGoal(mob);
            if(goal == null || !goal.canUse()) return false;
        }
        mob.getCapability(ModCapabilities.COMMANDS).ifPresent(commands -> commands.addCommand(command));
        List<Object> messageObjects = new ArrayList<>(Arrays.stream(additionalInfo).toList());
        messageObjects.add(0, mob.getDisplayName());
        player.sendSystemMessage(Component.translatable(message, messageObjects.toArray()));
        RobotBehavior.playAggressionSound(mob);
        return true;
    }

    private boolean orderEntityOutOfStorage(@Nullable BlockEntity blockEntity, GlobalPos to) {
        if(!(blockEntity instanceof StorageBlockEntity storage)) return false;
        Optional<Entity> spawnedEntity = storage.exitStorage();
        if(spawnedEntity.isEmpty()) return false;
        if(spawnedEntity.get() instanceof Mob mob) {
            mob.goalSelector.addGoal(2, new QuickMoveToBlock(mob, to));
        }
        if(spawnedEntity.get() instanceof RobotEntity robot) {
            RobotBehavior.playAggressionSound(robot);
        }
        return true;
    }

    public static int getID(ItemStack stack) {
        return hasID(stack) ? getTagCompound(stack).getInt(NBT_ID) : 0;
    }

    private static boolean hasID(ItemStack stack) {
        return getTagCompound(stack).contains(NBT_ID);
    }

    public static void assignID(ItemStack stack) {
        if(!hasID(stack)) {
            getTagCompound(stack).putInt(NBT_ID, WorldData.get().nextCommandGroupId());
        }
    }

    public static void rememberPos(ItemStack stack, GlobalPos pos) {
        clearRememberedEntity(stack);
        getTagCompound(stack).put(NBT_POS, PosUtil.writePos(pos));
    }

    public static void rememberEntity(ItemStack stack, LivingEntity entity) {
        clearRememberedPos(stack);
        clearRememberedEntity(stack);

        getTagCompound(stack).putUUID(NBT_ENTITY, entity.getUUID());
        cacheEntity(stack, entity.getId());
    }

    private static void cacheEntity(ItemStack stack, int entityId) {
        if(getTagCompound(stack).contains(NBT_POS)) return;
        getTagCompound(stack).putInt(NBT_ENTITY_CACHE, entityId);
    }

    public static void clearRememberedPos(ItemStack stack) {
        getTagCompound(stack).remove(NBT_POS);
    }

    public static void clearRememberedEntity(ItemStack stack) {
        getTagCompound(stack).remove(NBT_ENTITY);
        getTagCompound(stack).remove(NBT_ENTITY_CACHE);
    }

    @Nullable
    public static GlobalPos getRememberedPos(ItemStack stack) {
        if(!getTagCompound(stack).contains(NBT_POS)) return null;
        return PosUtil.readPos(getTagCompound(stack).getCompound(NBT_POS));
    }

    @Nullable
    public static UUID getRememberedEntity(ItemStack stack) {
        if(!getTagCompound(stack).contains(NBT_ENTITY)) return null;
        return getTagCompound(stack).getUUID(NBT_ENTITY);
    }

    @Nullable
    public static LivingEntity getRememberedEntity(Level level, ItemStack stack) {
        if(!getTagCompound(stack).contains(NBT_ENTITY)) return null;
        if(getTagCompound(stack).contains(NBT_ENTITY_CACHE)) {
            Entity entity = level.getEntity(getTagCompound(stack).getInt(NBT_ENTITY_CACHE));
            if(entity instanceof LivingEntity living) return living;
        }
        UUID uuid = getTagCompound(stack).getUUID(NBT_ENTITY);

        //FIXME Clients may not know of the entity
        Entity entity = EntityFinder.getEntity(level, uuid);
        if(!(entity instanceof LivingEntity living)) return null;
        cacheEntity(stack, living.getId());
        return living;
    }
}
