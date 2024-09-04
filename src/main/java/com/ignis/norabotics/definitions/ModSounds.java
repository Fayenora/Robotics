package com.ignis.norabotics.definitions;

import com.ignis.norabotics.Robotics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Robotics.MODID);

    public static final RegistryObject<SoundEvent> ROBOT_HURT = register("entity.robot.hurt");
    public static final RegistryObject<SoundEvent> ROBOT_DEATH = register("entity.robot.death");
    public static final RegistryObject<SoundEvent> ROBOT_DEATH_ACTIVE = register("entity.robot.death_active");
    public static final RegistryObject<SoundEvent> ROBOT_KILL_COMMAND = register("entity.robot.kill_command");
    public static final RegistryObject<SoundEvent> ASSEMBLER = register("machine.assembler");
    public static final RegistryObject<SoundEvent> WIRE_CUTTER = register("machine.wire_cutter");
    public static final RegistryObject<SoundEvent> SHIELD_UP = register("shield.start");
    public static final RegistryObject<SoundEvent> SHIELD_IMPACT = register("shield.impact");
    public static final RegistryObject<SoundEvent> SHIELD_IMPACT_FATAL = register("shield.fatal_impact");
    public static final RegistryObject<SoundEvent> INFOPAD_OPEN = register("book_open");
    public static final RegistryObject<SoundEvent> INFOPAD_FLIP = register("book_flip");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Robotics.MODID, name)));
    }
}
