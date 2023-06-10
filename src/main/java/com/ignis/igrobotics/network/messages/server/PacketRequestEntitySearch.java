package com.ignis.igrobotics.network.messages.server;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.core.EntitySearch;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.IMessage;
import com.ignis.igrobotics.network.messages.client.PacketGuiData;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

public class PacketRequestEntitySearch implements IMessage {

	EntitySearch search = new EntitySearch();
	int[] parentGuiPath;
	
	public PacketRequestEntitySearch() {}

	@OnlyIn(Dist.CLIENT)
	private PacketRequestEntitySearch(IElement[] parentGuiPath) {
		this.parentGuiPath = new int[parentGuiPath.length];
		for(int i = 0; i < parentGuiPath.length; i++) {
			this.parentGuiPath[i] = parentGuiPath[i].hashCode();
		}
	}

	@OnlyIn(Dist.CLIENT)
	public PacketRequestEntitySearch(IElement[] parentGuiPath, EntitySearch search) {
		this(parentGuiPath);
		this.search = search;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeByte(parentGuiPath.length);
		for(int j : parentGuiPath) {
			buf.writeInt(j);
		}
		search.write(buf);
	}

	@Override
	public void decode(FriendlyByteBuf buf) {
		int size = buf.readByte();
		parentGuiPath = new int[size];
		for(int i = 0; i < size; i++) {
			parentGuiPath[i] = buf.readInt();
		}
		search.read(buf);
	}

	@Override
	public void handle(NetworkEvent.Context cxt) {
		ServerPlayer player = cxt.getSender();
		if(player == null) return;
		if(!(player.level instanceof ServerLevel server)) return;
		Entity result = search.commence(server, player.blockPosition());
		//Let the client know, even if the search did not find anything
		NetworkHandler.sendToPlayer(new PacketGuiData(parentGuiPath, result), player);
	}
}
