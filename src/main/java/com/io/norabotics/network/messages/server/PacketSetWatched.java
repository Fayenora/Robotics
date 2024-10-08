package com.io.norabotics.network.messages.server;

import com.io.norabotics.common.handlers.EffectSynchronizer;
import com.io.norabotics.network.NetworkHandler;
import com.io.norabotics.network.messages.IMessage;
import com.io.norabotics.network.messages.client.PacketSetEntityEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

public class PacketSetWatched implements IMessage {

    private int entityId;
    private boolean watch;

    public PacketSetWatched() {}

    public PacketSetWatched(LivingEntity watched, boolean watch) {
        this.entityId = watched.getId();
        this.watch = watch;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(watch);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        watch = buf.readBoolean();
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        Entity ent = cxt.getSender().level().getEntity(entityId);
        if(!(ent instanceof LivingEntity)) return;

        if(watch) {
            if(!EffectSynchronizer.isWatchedBy(ent, cxt.getSender())) {
                NetworkHandler.sendToPlayer(new PacketSetEntityEffects((LivingEntity) ent), cxt.getSender());
            }
            EffectSynchronizer.addWatcher(ent, cxt.getSender());
        } else {
            EffectSynchronizer.removeWatcher(ent, cxt.getSender());
        }
    }
}
