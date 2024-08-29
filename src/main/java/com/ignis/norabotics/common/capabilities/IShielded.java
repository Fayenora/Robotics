package com.ignis.norabotics.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

@AutoRegisterCapability
public interface IShielded extends INBTSerializable<CompoundTag> {

    void damage(float damage);

    void tick();

    void recharge(float health);

    boolean isShielded();

    float getHealth();

    float getMaxHealth();

    boolean setActive(boolean activation);
}
