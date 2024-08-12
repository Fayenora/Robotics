package com.ignis.norabotics.network.messages;

import com.ignis.norabotics.common.access.AccessConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BufferSerializers {

    private static final List<BufferSerializer<?>> SERIALIZERS = new ArrayList<>();

    public static final BufferSerializer<?> NONE = new BufferSerializer<>(0, Dummy.class, (ignored, buf) -> {}, buf -> null);
    public static final BufferSerializer<LivingEntity> ENTITY = new BufferSerializer<>(1, LivingEntity.class, EntityByteBufUtil::writeEntity, EntityByteBufUtil::readEntity);
    public static final BufferSerializer<LivingEntity[]> ENTITIES = new BufferSerializer<>(2, LivingEntity[].class, (entities, buf) -> {
        buf.writeInt(entities.length);
        for(LivingEntity entity : entities) {
            EntityByteBufUtil.writeEntity(entity, buf);
        }
    }, buf -> {
        LivingEntity[] entities = new LivingEntity[buf.readInt()];
        for(int i = 0; i < entities.length; i++) {
            entities[i] = EntityByteBufUtil.readEntity(buf);
        }
        return entities;
    });
    public static final BufferSerializer<AccessConfig> CONFIG = new BufferSerializer<>(3, AccessConfig.class, AccessConfig::write, buf -> {
        AccessConfig config = new AccessConfig();
        config.read(buf);
        return config;
    });
    public static final BufferSerializer<BlockPos> BLOCKPOS = new BufferSerializer<>(4, BlockPos.class, (pos, buf) -> buf.writeBlockPos(pos), FriendlyByteBuf::readBlockPos);
    public static final BufferSerializer<Integer> INTEGER = new BufferSerializer<>(5, Integer.class, (i, buf) -> buf.writeInt(i), FriendlyByteBuf::readInt);

    public static <T> void writeObject(FriendlyByteBuf buf, T data) {
        buf.writeInt(getType(data).id);
        getType(data).writer().accept(data, buf);
    }

    public static Object readObject(FriendlyByteBuf buf) {
        return byId(buf.readInt()).reader().apply(buf);
    }

    public static <T> BufferSerializer<T> getType(T obj) {
        for(BufferSerializer<?> serializer : SERIALIZERS) {
            if(serializer.type.isInstance(obj)) {
                return (BufferSerializer<T>) serializer;
            }
        }
        return (BufferSerializer<T>) NONE;
    }

    public static BufferSerializer<?> byId(int id) {
        for(BufferSerializer<?> serializer : SERIALIZERS) {
            if(serializer.id() == id) {
                return serializer;
            }
        }
        return NONE;
    }

    public record BufferSerializer<T>(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader) {
        public BufferSerializer {
            SERIALIZERS.add(this);
        }
    }

    private static class Dummy {}
}
