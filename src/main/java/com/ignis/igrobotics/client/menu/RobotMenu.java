public class RobotMenu extends AbstractContainerMenu {
    public final RobotEntity robot;
    private final Level level;
    public final ContainerData data;

    public RobotMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getEntity(extraData.readInt()), new SimpleContainerData(3))
    }

    public RobotMenu(int id, Inventory playerInv, Entity entity, ContainerData data) {
        super(ModMenuTypes.ROBOT_MENU.get(), id);
        this.robot = (RobotEntity) entity;
        this.level = playerInv.player.level;
        this.data = data;

        addPlayerInv(playerInv, Reference.GUI_DEFAULT_DIMENSIONS);
        addDataSlots(data);

        //TODO: Enable/Disable Arms
        //TODO: Armor slots should only accept valid items
        robot.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for(int i = 0; i < 4; i++) {
                this.addSlot(new SlotItemHandler(handler, 5 - i, 8, 8 + 18 * i));
            }
            for(int x = 0; x < 3; x++) {
                for(int y = 0; y < 4; y++) {
                    this.addSlot(new SlotItemHandler(handler, x * 4 + y + 6, 98 + 18 * x, 8 + 18 * y));
                }
            }

            this.addSlot(new SlotItemHandler(handler, 0, 77, 44)); //Mainhand
            this.addSlot(new SlotItemHandler(handler, 1, 77, 62)); //Offhand
        });
    }

    protected void addPlayerInv(Inventory playerInv, Dimension size) {
        int offsetX = size.width / 2 - (9 * 17 / 2) + 1;
        int offsetY = size.height - 82;
        //Inventory
        for(int x = 0; x < 9; x++) {
            for(int y = 0; y < 3; y++) {
                this.addSlot(new Slot(playerInv, x + y * 9 + 9, x * 18 + offsetX, y * 18 + offsetY));
            }
        }

        //Hotbar
        for(int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, i * 18 + offsetX, 58 + offsetY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !robot.isDead();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        // The quick moved slot stack
        ItemStack quickMovedStack = ItemStack.EMPTY;
        // The quick moved slot
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        // If the slot is in the valid range and the slot is not empty
        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            // Get the raw stack to move
            ItemStack rawStack = quickMovedSlot.getItem();
            // Set the slot stack to a copy of the raw stack
            quickMovedStack = rawStack.copy();

            // If the quick move was performed on the data inventory result slot
            if (quickMovedSlotIndex == 0) {
                // Try to move the result slot into the player inventory/hotbar
                if (!this.moveItemStackTo(rawStack, 5, 41, true)) {
                    // If cannot move, no longer quick move
                    return ItemStack.EMPTY;
                }
            }
            // Else if the quick move was performed on the player inventory or hotbar slot
            else if (quickMovedSlotIndex >= 5 && quickMovedSlotIndex < 41) {
                // Try to move the inventory/hotbar slot into the data inventory input slots
                if (!this.moveItemStackTo(rawStack, 1, 5, false)) {
                    // If cannot move and in player inventory slot, try to move to hotbar
                    if (quickMovedSlotIndex < 32) {
                        if (!this.moveItemStackTo(rawStack, 32, 41, false)) {
                            // If cannot move, no longer quick move
                            return ItemStack.EMPTY;
                        }
                    }
                    // Else try to move hotbar into player inventory slot
                    else if (!this.moveItemStackTo(rawStack, 5, 32, false)) {
                        // If cannot move, no longer quick move
                        return ItemStack.EMPTY;
                    }
                }
            }
            // Else if the quick move was performed on the data inventory input slots, try to move to player inventory/hotbar
            else if (!this.moveItemStackTo(rawStack, 5, 41, false)) {
                // If cannot move, no longer quick move
                return ItemStack.EMPTY;
            }

            if (rawStack.isEmpty()) {
                // If the raw stack has completely moved out of the slot, set the slot to the empty stack
                quickMovedSlot.set(ItemStack.EMPTY);
            } else {
                // Otherwise, notify the slot that that the stack count has changed
                quickMovedSlot.setChanged();
            }
            if (rawStack.getCount() == quickMovedStack.getCount()) {
                // If the raw stack was not able to be moved to another slot, no longer quick move
                return ItemStack.EMPTY;
            }
            // Execute logic on what to do post move with the remaining stack
            quickMovedSlot.onTake(player, rawStack);
        }

        return quickMovedStack; // Return the slot stack
    }
}
