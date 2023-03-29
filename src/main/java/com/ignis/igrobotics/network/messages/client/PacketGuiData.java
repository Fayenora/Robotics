package com.ignis.igrobotics.network.messages.client;

import com.ignis.igrobotics.client.screen.base.IElement;
import com.ignis.igrobotics.network.messages.BufferSerializers;
import com.ignis.igrobotics.network.messages.IMessage;
import com.ignis.igrobotics.network.messages.IPacketDataReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Deliver data a gui has requested
 * @author Ignis
 */
public class PacketGuiData implements IMessage {
	
	private int[] gui_path;
	private Object data;
	
	public PacketGuiData() {}
	
	public PacketGuiData(int[] gui_path, Object data) {
		this.gui_path = gui_path;
		this.data = data;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(gui_path.length);
		for(int i = 0; i < gui_path.length; i++) {
			buf.writeInt(gui_path[i]);
		}
		BufferSerializers.writeObject(buf, data);
	}

	@Override
	public void decode(FriendlyByteBuf buf) {
		int size = buf.readInt();
		gui_path = new int[size];
		for(int i = 0; i < size; i++) {
			gui_path[i] = buf.readInt();
		}
		data = BufferSerializers.readObject(buf);
	}

	@Override
	public void handle(NetworkEvent.Context cxt) {
		Screen currScreen = Minecraft.getInstance().screen;
		if(!(currScreen instanceof IElement)) return;

		IElement current = (IElement) currScreen;
		if(gui_path != null) { //If gui path is null, use current screen
			for(int i = gui_path.length - 1; i >= 0; i--) {
				for(GuiEventListener comp : current.children()) {
					if(comp.hashCode() == gui_path[i] && comp instanceof IElement element) {
						current = element;
						break;
					}
				}
			}
		}

		if(!(current instanceof IPacketDataReceiver receiver)) return;
		receiver.receive(data);
	}

	public BufferSerializers.BufferSerializer getType() {
		return BufferSerializers.getType(data);
	}
}
