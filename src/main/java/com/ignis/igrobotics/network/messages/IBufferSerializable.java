package com.ignis.igrobotics.network.messages;

import net.minecraft.network.FriendlyByteBuf;

public interface IBufferSerializable {
	
	void read(FriendlyByteBuf buf);
	
	void write(FriendlyByteBuf buf);

}
