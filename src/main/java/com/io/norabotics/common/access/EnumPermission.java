package com.io.norabotics.common.access;

import com.io.norabotics.common.helpers.util.Stable;

import java.util.EnumSet;

public enum EnumPermission implements Stable {
	VIEW(1),
	INVENTORY(2), 
	CONFIGURATION(3),
	COMMANDS(4),
	ALLY(5);
	
	public static final EnumSet<EnumPermission> DEFAULT_PERMISSIONS = EnumSet.of(VIEW, INVENTORY);
	
	private final int flag;
	
	EnumPermission(int flag) {
		this.flag = flag;
	}

	@Override
	public int getStableId() {
		return flag;
	}
}

