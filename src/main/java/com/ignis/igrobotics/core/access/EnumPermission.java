package com.ignis.igrobotics.core.access;

public enum EnumPermission {
	VIEW(1),
	INVENTORY(2), 
	CONFIGURATION(4),
	COMMANDS(8), 
	ALLY(16);
	
	public static final int DEFAULT_PERMISSIONS = (int) (Math.pow(2, values().length) - 1);
	
	private final int flag;
	
	EnumPermission(int flag) {
		this.flag = flag;
	}
	
	public boolean fulfills(int permissions) {
		return (permissions | getFlag()) == permissions;
	}
	
	public static int combine(EnumPermission... permissions) {
		return combine(0, permissions);
	}
	
	public static int combine(int currPermissions, EnumPermission... permissions) {
		int flag = currPermissions;
		for(EnumPermission perm : permissions) {
			flag |= perm.flag;
		}
		return flag;
	}
	
	public static int remove(int currPermissions, EnumPermission... permissions) {
		int flag = currPermissions;
		for(EnumPermission perm : permissions) {
			//Set the according bit of the permission to 0; there may be a better way to do this
			if(perm.fulfills(flag)) {
				flag -= perm.flag;
			}
		}
		return flag;
	}
	
	public int getFlag() {
		return flag;
	}
}
