package com.ignis.igrobotics.core.events;

import com.ignis.igrobotics.core.capabilities.perks.IPerkMap;
import com.ignis.igrobotics.core.robot.EnumModuleSlot;
import com.ignis.igrobotics.core.robot.RobotModule;
import com.ignis.igrobotics.core.robot.RobotPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 *  PerkChangeEvent is fired whenever the perks of a robot change <br>
 *  The event is fired whenever a part about the robot changes in {@link com.ignis.igrobotics.core.capabilities.parts.PartsCapability#setBodyPart(RobotPart)} or {@link com.ignis.igrobotics.core.capabilities.robot.RobotCapability#setModule(EnumModuleSlot, int, ItemStack)}
 *  <br>
 *  This event is not {@link Cancelable}.<br>
 *  <br>
 *  This event does not have a result. {@link HasResult}<br>
 *  <br>
 *  This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 */
public class PerkChangeEvent extends Event {

    private final LivingEntity entity;
    private final IPerkMap perks;

    public PerkChangeEvent(LivingEntity entity, IPerkMap perks) {
        this.entity = entity;
        this.perks = perks;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public IPerkMap getPerks() {
        return perks;
    }
}
