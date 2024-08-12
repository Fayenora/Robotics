package com.ignis.igrobotics.common.access;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.helpers.util.Stable;
import com.ignis.igrobotics.network.messages.IBufferSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;

/**
 * Manages the access rights to an object implementing the {@link ISecuredObject} interface
 * @author Ignis
 */
@SuppressWarnings("unused")
public class AccessConfig implements INBTSerializable<CompoundTag>, IBufferSerializable {
	
	/** {@link Reference#DEFAULT_UUID} if there is no owner*/
	protected UUID owner;
	/** Default permissions are saved under the {@link Reference#DEFAULT_UUID}. In theory, a collision could happen, but the probability approaches 0 */
	protected HashMap<UUID, EnumSet<EnumPermission>> permissions = new HashMap<>();
	
	public AccessConfig() {
		this(Reference.DEFAULT_UUID);
	}
	
	public AccessConfig(UUID owner) {
		permissions.put(Reference.DEFAULT_UUID, EnumPermission.DEFAULT_PERMISSIONS);
		this.owner = owner;
	}
	
	public boolean hasPermission(Player player, EnumPermission permission) {
		return hasPermission(player.getUUID(), permission);
	}

	public boolean hasPermission(UUID player, EnumPermission permission) {
		if(player.equals(owner)) return true;
		if(permissions.containsKey(player)) {
			return permissions.get(player).contains(permission);
		}
		return permissions.get(Reference.DEFAULT_UUID).contains(permission);
	}
	
	public EnumSet<EnumPermission> getPermissions(UUID player) {
		if(player != null && permissions.containsKey(player)) {
			return permissions.get(player);
		}
		return getDefaultPermissions();
	}
	
	private void setPermissions(UUID player, EnumSet<EnumPermission> permissions) {
		this.permissions.put(player, permissions);
	}
	
	public void addPermission(UUID player, EnumPermission permission) {
		permissions.get(player).add(permission);
	}
	
	public void removePermission(UUID player, EnumPermission permission) {
		permissions.get(player).remove(permission);
	}
	
	public void addDefaultPermission(EnumPermission permission) {
		addPermission(Reference.DEFAULT_UUID, permission);
	}
	
	public void removeDefaultPermission(EnumPermission permission) {
		removePermission(Reference.DEFAULT_UUID, permission);
	}
	
	public void removePlayerPermissions(UUID player) {
		if(player != null && player.equals(Reference.DEFAULT_UUID)) return;
		permissions.remove(player);
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putUUID("owner", getOwner());
		CompoundTag permissionNBT = new CompoundTag();
		for(UUID key : permissions.keySet()) {
			permissionNBT.putInt(key.toString(), Stable.encode(permissions.get(key)));
		}
		nbt.put("permissions", permissionNBT);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		owner = nbt.getUUID("owner");
		permissions = new HashMap<>();
		CompoundTag permissionNBT = nbt.getCompound("permissions");
		for(String key : permissionNBT.getAllKeys()) {
			permissions.put(UUID.fromString(key), Stable.decode(permissionNBT.getInt(key), EnumPermission.class));
		}
	}

	@Override
	public void read(FriendlyByteBuf buffer) {
		setOwner(buffer.readUUID());
		int size = buffer.readInt();
		for(int i = 0; i < size; i++) {
			UUID player = buffer.readUUID();
			setPermissions(player, buffer.readEnumSet(EnumPermission.class));
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(getOwner());
		buffer.writeInt(permissions.size());
		for(UUID key : permissions.keySet()) {
			buffer.writeUUID(key);
			buffer.writeEnumSet(getPermissions(key), EnumPermission.class);
		}
	}
	
	public AccessConfig or(AccessConfig config) {
		if(!owner.equals(config.owner)) {
			Robotics.LOGGER.debug("Attempting invalid operation 'or' between access configs with different owner");
			return this;
		}
		AccessConfig toReturn = new AccessConfig();
		toReturn.setOwner(getOwner());
		for(UUID player : permissions.keySet()) {
			toReturn.permissions.put(player, this.getPermissions(player));
		}
		for(UUID player : config.permissions.keySet()) {
			if(toReturn.permissions.containsKey(player)) {
				EnumSet<EnumPermission> combinedPermissions = EnumSet.copyOf(config.getPermissions(player));
				combinedPermissions.addAll(this.getPermissions(player));
				toReturn.permissions.put(player, combinedPermissions);
			} else toReturn.permissions.put(player, config.getPermissions(player));
		}
		return toReturn;
	}
	
	public AccessConfig and(AccessConfig config) {
		if(!owner.equals(config.owner)) {
			Robotics.LOGGER.debug("Attempting invalid operation 'and' between access configs with different owners");
			return this;
		}
		AccessConfig toReturn = new AccessConfig();
		toReturn.setOwner(getOwner());
		for(UUID player : this.permissions.keySet()) {
			if(config.permissions.containsKey(player)) {
				EnumSet<EnumPermission> combinedPermissions = EnumSet.complementOf(config.getPermissions(player));
				combinedPermissions.addAll(EnumSet.complementOf(this.getPermissions(player)));
				toReturn.permissions.put(player, EnumSet.complementOf(combinedPermissions));
			} else toReturn.permissions.put(player, this.getPermissions(player));
		}
		for(UUID player : config.permissions.keySet()) {
			if(this.permissions.containsKey(player)) continue;
			toReturn.permissions.put(player, config.getPermissions(player));
		}
		return toReturn;
	}
	
	public static AccessConfig culminate(AccessConfig player, AccessConfig group, AccessConfig robot) {
		return player.and(group).and(robot);
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public void setOwner(UUID owner) {
		this.owner = owner;
	}
	
	public boolean hasOwner() {
		return !owner.equals(Reference.DEFAULT_UUID);
	}
	
	public EnumSet<EnumPermission> getDefaultPermissions() {
		return permissions.get(Reference.DEFAULT_UUID);
	}

	public Collection<UUID> players() {
		return permissions.keySet();
	}

}