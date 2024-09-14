package com.io.norabotics.common.content.events;

import com.io.norabotics.common.robot.RobotModule;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 *  ModuleActivationEvent is fired when a module is activated <br>
 *  The event is fired in {@link RobotModule#activate(LivingEntity)} directly prior to executing the action
 *  <br>
 *  This event is {@link Cancelable}.<br>
 *  If this event is canceled, the Action does not take place. No energy is used.<br>
 *  <br>
 *  This event does not have a result. {@link HasResult}<br>
 *  <br>
 *  This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 */
@Cancelable
public class ModuleActivationEvent extends Event {

    private final LivingEntity caster;
    private final RobotModule module;

    public ModuleActivationEvent(LivingEntity caster, RobotModule module) {
        this.caster = caster;
        this.module = module;
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public RobotModule getModule() {
        return module;
    }
}
