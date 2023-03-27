package com.ignis.igrobotics.network.messages.client;

import com.ignis.igrobotics.network.messages.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.Iterator;

public class PacketSetEntityEffects implements IMessage<PacketSetEntityEffects> {

    private int entityId;
    private byte[] effectIds;
    private byte[] amplifiers;
    private int[] durations;
    private byte[] flags;

    public PacketSetEntityEffects() {}

    public PacketSetEntityEffects(int entityId, Collection<MobEffectInstance> effects) {
        this.entityId = entityId;
        effectIds = new byte[effects.size()];
        amplifiers = new byte[effects.size()];
        durations = new int[effects.size()];
        flags = new byte[effects.size()];
        int i = 0;
        for(MobEffectInstance effect : effects) {
            effectIds[i] = (byte) (MobEffect.getId(effect.getEffect()) & 255);
            amplifiers[i] = (byte)(effect.getAmplifier() & 255);
            durations[i] = Math.min(effect.getDuration(), 32767);
            flags[i] = 0;
            if (effect.isAmbient()) {
                this.flags[i] = (byte)(this.flags[i] | 1);
            }

            if (effect.isVisible()) {
                this.flags[i] = (byte)(this.flags[i] | 2);
            }
            i++;
        }
    }

    public PacketSetEntityEffects(LivingEntity ent) {
        this(ent.getId(), ent.getActiveEffects());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(effectIds.length);
        for(int i = 0; i < effectIds.length; i++) {
            buf.writeByte(effectIds[i]);
            buf.writeByte(amplifiers[i]);
            buf.writeInt(durations[i]);
            buf.writeByte(flags[i]);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        int length = buf.readInt();
        effectIds = new byte[length];
        amplifiers = new byte[length];
        durations = new int[length];
        flags = new byte[length];
        for(int i = 0; i < length; i++) {
            effectIds[i] = buf.readByte();
            amplifiers[i] = buf.readByte();
            durations[i] = buf.readInt();
            flags[i] = buf.readByte();
        }
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        Entity entity = Minecraft.getInstance().player.level.getEntity(entityId);
        if(!(entity instanceof LivingEntity living)) {
            return;
        }

        Iterator<MobEffectInstance> iterator = living.getActiveEffects().iterator();
        for(; iterator.hasNext(); ) {
            iterator.next();
            iterator.remove();
        }
        for(int i = 0; i < effectIds.length; i++) {
            MobEffect potion = MobEffect.byId(effectIds[i] & 0xFF);
            if (potion == null) continue;

            MobEffectInstance effect = new MobEffectInstance(potion, durations[i], amplifiers[i], isAmbient(i), isVisible(i));
            living.addEffect(effect);
        }
    }

    public boolean isVisible(int index) {
        return (this.flags[index] & 2) == 2;
    }

    public boolean isAmbient(int index) {
        return (this.flags[index] & 1) == 1;
    }
}
