package com.ignis.norabotics.network.messages;

import com.ignis.norabotics.Robotics;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class EntityByteBufUtil implements IBufferSerializable {
	
	public static final String PLAYER = "EntityPlayer";
	
	private CompoundTag nbt;
	private int entityId;  //While it is usually not required, temporary entityIds are used to communicate robots to guis, so it has to be saved & transported here
	
	public EntityByteBufUtil(Entity entity) {
		entityId = entity.getId();
		if(entity instanceof Player player) {
			CompoundTag nbtProfile = new CompoundTag();
			NbtUtils.writeGameProfile(nbtProfile, player.getGameProfile());

			nbt = player.serializeNBT();
			nbt.putString("id", PLAYER);
			nbt.put("profile", nbtProfile);
		} else {
			nbt = entity.serializeNBT();
		}
	}
	
	public EntityByteBufUtil(FriendlyByteBuf buf) {
		read(buf);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeNbt(nbt);
	}
	
	@Override
	public void read(FriendlyByteBuf buf) {
		entityId = buf.readInt();
		nbt = buf.readNbt();
	}

	@Nullable
	public LivingEntity constructEntity(Level level) {
		if(nbt == null) return null;
		
		String entity_type = nbt.getString("id");
		Optional<EntityType<?>> entityType = EntityType.byString(entity_type);
		
		//Handle Entities
		if(entityType.isPresent()) {
			try {
				Entity ent = entityType.get().create(level);
				if(!(ent instanceof LivingEntity)) return null;
				ent.deserializeNBT(nbt);
				ent.setId(entityId);
				return (LivingEntity) ent;
			} catch (IllegalArgumentException e) {
				e.printStackTrace(); //Invocation seems to have failed for whatever reason
			}
		} else
		//Handle Players
		if(entity_type.equals(PLAYER)) {
			AtomicReference<GameProfile> profile = new AtomicReference<>(NbtUtils.readGameProfile(nbt.getCompound("profile")));
			SkullBlockEntity.updateGameprofile(profile.get(), profile::set);
			Player other = Robotics.proxy.createFakePlayer(level, profile.get()).get();
			other.deserializeNBT(nbt);
			other.setId(entityId);
			return other;
		}
		return null;
	}

	public static void writeEntity(Entity entity, FriendlyByteBuf buf) {
		new EntityByteBufUtil(entity).write(buf);
	}

	/**
	 * Assumes overworld as dimension
	 * @param buf byte buffer
	 * @return a constructed entity on the overworld. (does not spawn the entity)
	 */
	@Nullable
	public static LivingEntity readEntity(FriendlyByteBuf buf) {
		return new EntityByteBufUtil(buf).constructEntity(Robotics.proxy.getLevel());
	}

	public static void writeEntities(Collection<LivingEntity> entities, FriendlyByteBuf buf) {
		buf.writeShort(entities.size() & 65535); //Write an unsigned short
		for(LivingEntity living : entities) {
			writeEntity(living, buf);
		}
	}

	public static Collection<LivingEntity> readEntities(FriendlyByteBuf buf) {
		int size = buf.readShort() & 65535;
		Collection<LivingEntity> entities = new HashSet<>(size);
		for(int i = 0; i < size; i++) {
			LivingEntity living = readEntity(buf);
			if(living == null) continue;
			entities.add(living);
		}
		return entities;
	}

}
