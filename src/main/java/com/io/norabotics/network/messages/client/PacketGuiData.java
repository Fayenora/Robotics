package com.io.norabotics.network.messages.client;

import com.io.norabotics.Robotics;
import com.io.norabotics.network.messages.BufferSerializers;
import com.io.norabotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Deliver data a gui has requested
 * @author Ignis
 */
public class PacketGuiData implements IMessage {

	private int[] guiPath;
	private Object data;

	public PacketGuiData() {}

	public PacketGuiData(int[] guiPath, Object data) {
		this.guiPath = guiPath;
		this.data = data;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(guiPath.length);
		for(int j : guiPath) {
			buf.writeInt(j);
		}
		BufferSerializers.writeObject(buf, data);
	}

	@Override
	public void decode(FriendlyByteBuf buf) {
		int size = buf.readInt();
		guiPath = new int[size];
		for(int i = 0; i < size; i++) {
			guiPath[i] = buf.readInt();
		}
		data = BufferSerializers.readObject(buf);
	}

	@Override
	public void handle(NetworkEvent.Context cxt) {
		Robotics.proxy.handleGuiData(guiPath, data);
	}

	public BufferSerializers.BufferSerializer<?> getType() {
		return BufferSerializers.getType(data);
	}
}
