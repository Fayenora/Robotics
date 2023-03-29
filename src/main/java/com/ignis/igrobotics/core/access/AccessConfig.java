package com.ignis.igrobotics.core.access;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.UUID;

/**
 * Manages the access rights to an object implementing the {@link ISecuredObject} interface
 * @author Ignis
 */
public class AccessConfig implements INBTSerializable<CompoundTag> {
	
	/** {@link Reference#DEFAULT_UUID} if there is no owner*/
	protected UUID owner;
	/** Default permissions are saved under the {@link Reference#DEFAULT_UUID}. In theory, a collision could happen, but the probability approaches 0 */
	protected HashMap<UUID, Integer> permissions = new HashMap<>();
	
	public AccessConfig() {
		this(Reference.DEFAULT_UUID);
	}
	
	public AccessConfig(UUID owner) {
		permissions.put(Reference.DEFAULT_UUID, EnumPermission.DEFAULT_PERMISSIONS);
		this.owner = owner;
	}
	
	public boolean hasPermission(Player player, EnumPermission permission) {
		UUID uuid = player.getUUID();
		if(uuid.equals(owner)) return true;
		if(permissions.containsKey(uuid)) {
			return permission.fulfills(permissions.get(uuid));
		}
		return permission.fulfills(permissions.get(Reference.DEFAULT_UUID));
	}
	
	private int getPermissions(UUID player) {
		if(player != null && permissions.containsKey(player)) {
			return permissions.get(player);
		}
		return getDefaultPermissions();
	}
	
	private void setPermissions(UUID player, int permissions) {
		this.permissions.put(player, permissions);
	}
	
	public void addPermission(UUID player, EnumPermission permission) {
		if(player == null) return; 
		int currentPermissions = getPermissions(player);
		setPermissions(player, EnumPermission.combine(currentPermissions, permission));
	}
	
	public void removePermission(UUID player, EnumPermission permission) {
		if(player == null) return;
		int currentPermissions = getPermissions(player);
		setPermissions(player, EnumPermission.remove(currentPermissions, permission));
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
			permissionNBT.putInt(key.toString(), permissions.get(key));
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
			permissions.put(UUID.fromString(key), permissionNBT.getInt(key));
		}
	}
	
	public void read(FriendlyByteBuf buffer) {
		setOwner(buffer.readUUID());
		int size = buffer.readInt();
		for(int i = 0; i < size; i++) {
			UUID player = buffer.readUUID();
			int permissions = buffer.readInt();
			setPermissions(player, permissions);
		}
	}
	
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(getOwner());
		buffer.writeInt(getPermissions().size());
		for(UUID key : getPermissions().keySet()) {
			buffer.writeUUID(key);
			buffer.writeInt(getPermissions().get(key));
		}
	}
	
	public AccessConfig or(AccessConfig config) {
		if(!owner.equals(config.owner)) {
			Robotics.LOGGER.debug("Attempting invalid operation 'or' between access configs with different owner");
			return this;
		}
		AccessConfig toReturn = new AccessConfig();
		toReturn.setOwner(getOwner());
		for(UUID player : this.getPermissions().keySet()) {
			toReturn.permissions.put(player, this.getPermissions(player));
		}
		for(UUID player : config.getPermissions().keySet()) {
			if(toReturn.permissions.containsKey(player)) {
				toReturn.permissions.put(player, toReturn.permissions.get(player) | config.getPermissions(player));
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
		for(UUID player : this.getPermissions().keySet()) {
			if(config.permissions.containsKey(player)) {
				toReturn.permissions.put(player, config.getPermissions(player) & this.getPermissions(player));
			} else toReturn.permissions.put(player, this.getPermissions(player));
		}
		for(UUID player : config.getPermissions().keySet()) {
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
	
	public int getDefaultPermissions() {
		return permissions.get(Reference.DEFAULT_UUID);
	}
	
	public HashMap<UUID, Integer> getPermissions() {
		return new HashMap<>(permissions);
	}

}