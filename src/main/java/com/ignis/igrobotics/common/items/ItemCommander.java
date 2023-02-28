package com.ignis.igrobotics.common.items;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.network.messages.NetworkHandler;
import com.ignis.igrobotics.network.messages.server.TestMessage;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class ItemCommander extends Item {

    public ItemCommander() {
        super(new Item.Properties().tab(Robotics.TAB_ROBOTICS));
    }
}
