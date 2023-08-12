package com.ignis.igrobotics.common.items;

import com.ignis.igrobotics.client.menu.CommanderMenu;
import com.ignis.igrobotics.common.RobotBehavior;
import com.ignis.igrobotics.common.WorldData;
import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.common.entity.ai.QuickMoveToBlock;
import com.ignis.igrobotics.core.RoboticsFinder;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.network.messages.EntityByteBufUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

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
        if(player.level.isClientSide()) return InteractionResult.SUCCESS;
        Optional<IRobot> optRobot = target.getCapability(ModCapabilities.ROBOT).resolve();

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

        rememberEntity(stack, target);
        player.sendSystemMessage(Component.translatable("commandGroup.selected.robot", target.getDisplayName()));
        return InteractionResult.CONSUME;
    }

    //Called on block right click
    @Override
    public InteractionResult useOn(UseOnContext cxt) {
        ItemStack stack = cxt.getItemInHand();
        Level level = cxt.getLevel();
        BlockPos pos = cxt.getClickedPos();
        
        if(level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos saved_pos = getRememberedPos(stack);
        LivingEntity living = getRememberedEntity(level, stack);

        //If the commander remembers a robot, make it move here

        if(living instanceof Mob mob && living.getCapability(ModCapabilities.ROBOT).isPresent()) {
            living.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                if(robot.isActive()) {
                    mob.goalSelector.addGoal(2, new QuickMoveToBlock(mob, pos));
                    RobotBehavior.playAggressionSound(living);
                }
            });
            return InteractionResult.CONSUME;
        }
        //If the commander is pointing towards a robot in storage, make it exit and move here
        if(saved_pos != null) {
            //FIXME: Handle trans-dimensional positions
            //If the currently remembered BlockPos points towards a valid robot in storage make it exit and move here
            if(level.getBlockState(saved_pos).getBlock() == ModBlocks.ROBOT_STORAGE.get()) {
                BlockEntity storage = level.getBlockEntity(level.getBlockState(saved_pos).getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? saved_pos.below() : saved_pos);
                if(orderEntityOutOfStorage(storage, pos)) {
                    return InteractionResult.CONSUME;
                }
            }
        }
        //Otherwise just remember this position (for f.e. commands)
        CommanderItem.rememberPos(stack, pos);
        cxt.getPlayer().sendSystemMessage(Component.translatable("commandGroup.selected.pos"));

        return InteractionResult.CONSUME;
    }

    private boolean orderEntityOutOfStorage(@Nullable BlockEntity blockEntity, BlockPos to) {
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
        Collection<LivingEntity> robotsOfCommandGroup = RoboticsFinder.getRobotics(level, robot -> robot.getCommandGroup() == commandGroup);
        NetworkHooks.openScreen((ServerPlayer) player,
                new SimpleMenuProvider((id, ign1, ign2) -> new CommanderMenu(id, robotsOfCommandGroup), stack.getHoverName().copy().withStyle(Style.EMPTY)),
                buf -> EntityByteBufUtil.writeEntities(robotsOfCommandGroup, buf));

        return InteractionResultHolder.consume(stack);
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

    public static void rememberPos(ItemStack stack, BlockPos pos) {
        clearRememberedEntity(stack);
        getTagCompound(stack).put(NBT_POS, NbtUtils.writeBlockPos(pos));
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
    public static BlockPos getRememberedPos(ItemStack stack) {
        if(!getTagCompound(stack).contains(NBT_POS)) return null;
        return NbtUtils.readBlockPos(getTagCompound(stack).getCompound(NBT_POS));
    }

    @Nullable
    public static UUID getRememberedEntity(ItemStack stack) {
        if(!getTagCompound(stack).contains(NBT_ENTITY)) return null;
        return getTagCompound(stack).getUUID(NBT_ENTITY);
    }

    @Nullable
    public static LivingEntity getRememberedEntity(Level level, ItemStack stack) {
        if(!getTagCompound(stack).contains(NBT_ENTITY)) return null;
        if(getTagCompound(stack).contains(NBT_ENTITY_CACHE)) return (LivingEntity) level.getEntity(getTagCompound(stack).getInt(NBT_ENTITY_CACHE));
        UUID uuid = getTagCompound(stack).getUUID(NBT_ENTITY);

        //FIXME Clients may not know of the entity
        Entity entity = RoboticsFinder.getEntity(level, uuid);
        if(!(entity instanceof LivingEntity living)) return null;
        cacheEntity(stack, living.getId());
        return living;
    }
}
