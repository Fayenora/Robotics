package com.ignis.igrobotics.network.messages.server;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.network.NetworkHandler;
import com.ignis.igrobotics.network.messages.IMessage;
import com.ignis.igrobotics.network.messages.client.PacketGuiData;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

public class PacketRequestEntitySearch implements IMessage {
	
	/** type of the query.
	 *  0 - Error state
	 *  1 - match UUID
	 *  2 - match name
	 *  3 - match entity id
	 */
	int type = 0;
	UUID uuid;
	String name;
	int entityId;
	int[] parentGuiPath;
	
	public PacketRequestEntitySearch() {}
	
	private PacketRequestEntitySearch(IElement[] parentGuiPath) {
		this.parentGuiPath = new int[parentGuiPath.length];
		for(int i = 0; i < parentGuiPath.length; i++) {
			this.parentGuiPath[i] = parentGuiPath[i].hashCode();
		}
	}
	
	/**
	 * Looks for an entity with this UUID on the server world. If none is found, look for any offline player with that UUID
	 * @param parentGuiPath the gui path leading to the component requiring the information
	 * @param uuid the uuid to look for
	 */
	public PacketRequestEntitySearch(IElement[] parentGuiPath, UUID uuid) {
		this(parentGuiPath);
		this.uuid = uuid;
		type = 1;
	}
	
	/**
	 * Looks for an entity with this name on the server world. If none is found, look for any offline player with that name
	 * <br><b> Extreme compute requirements </b>
	 * @param parentGuiPath the gui path leading to the component requiring the information
	 * @param name the name to look for
	 */
	public PacketRequestEntitySearch(IElement[] parentGuiPath, String name) {
		this(parentGuiPath);
		this.name = name;
		type = 2;
	}
	
	public PacketRequestEntitySearch(IElement[] parentGuiPath, int entityId) {
		this(parentGuiPath);
		this.entityId = entityId;
		type = 3;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(parentGuiPath.length);
		for(int j : parentGuiPath) {
			buf.writeInt(j);
		}

		//Type specific information
		buf.writeInt(type);
		switch (type) {
			case 1 -> {
				buf.writeLong(uuid.getMostSignificantBits());
				buf.writeLong(uuid.getLeastSignificantBits());
			}
			case 2 -> {
				buf.writeInt(name.length());
				buf.writeCharSequence(name, Charset.defaultCharset());
			}
			case 3 -> buf.writeInt(entityId);
		}
	}

	@Override
	public void decode(FriendlyByteBuf buf) {
		int size = buf.readInt();
		parentGuiPath = new int[size];
		for(int i = 0; i < size; i++) {
			parentGuiPath[i] = buf.readInt();
		}

		//Type specific information
		type = buf.readInt();
		switch(type) {
			case 0: return;
			case 1:
				long most = buf.readLong();
				long least = buf.readLong();
				uuid = new UUID(most, least);
				break;
			case 2:
				int length = buf.readInt();
				name = buf.readCharSequence(length, Charset.defaultCharset()).toString();
				break;

			case 3:
				entityId = buf.readInt();
				break;
		}
	}

	@Override
	public void handle(NetworkEvent.Context cxt) {
		ServerPlayer player = cxt.getSender();
		ServerLevel server = (ServerLevel) player.level;
		Entity result = null;

		switch(type) {
			case 0: return;
			case 1:
				result = server.getEntity(uuid);
				break;
			case 2:
				//Greedily search for the closest entity
				//FIXME: If the client requires a EntityLiving, but a not living entity matching the search is closer to the player, the search will yield the not living entity, causing the client to believe no entity matches the search
				float min_distance = Float.MAX_VALUE;
				for(Entity ent : server.getAllEntities()) {
					if(ent.getName().getString().equals(name)) {
						float distance = player.distanceTo(ent);
						if(distance < min_distance) {
							result = ent;
							min_distance = distance;
						}
					}
				}
				break;
			case 3:
				result = server.getEntity(entityId);
				break;
		}

		//If nothing is found in the world, look in the profile cache
		if(result == null) {
			GameProfileCache cache = player.getServer().getProfileCache();
			Optional<GameProfile> profile = Optional.empty();
			switch(type) {
				case 1: profile = cache.get(uuid); break;
				case 2: profile = cache.get(name); break; //Only search for the exact match here
				case 3: break; //No way to look the player up if he left
			}
			if(profile.isPresent() && profile.get().isComplete()) {
				result = Robotics.proxy.createFakePlayer(server, profile.get()).get();
			}
		}

		//Let the client know, even if the search did not find anything
		NetworkHandler.sendToPlayer(new PacketGuiData(parentGuiPath, result), player);
	}
}
