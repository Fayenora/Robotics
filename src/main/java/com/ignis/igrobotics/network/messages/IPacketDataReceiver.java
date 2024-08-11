package com.ignis.igrobotics.network.messages;

import com.ignis.igrobotics.common.access.AccessConfig;
import net.minecraft.world.entity.LivingEntity;

public interface IPacketDataReceiver {

    default void receive(LivingEntity entity) {}

    default void receive(LivingEntity[] entities) {}

    default void receive(AccessConfig config) {}

    default void receive(Object data) {
        BufferSerializers.BufferSerializer<?> type = BufferSerializers.getType(data);
        if(type.equals(BufferSerializers.ENTITY)) receive((LivingEntity) data);
        if(type.equals(BufferSerializers.ENTITIES)) receive((LivingEntity[]) data);
        if(type.equals(BufferSerializers.CONFIG)) receive((AccessConfig) data);
        if(data == null) {
            receive((LivingEntity) data);
            receive((LivingEntity[]) data);
            receive((AccessConfig) data);
        }
    }
}
