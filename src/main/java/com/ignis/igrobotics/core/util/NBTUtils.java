package com.ignis.igrobotics.core.util;

import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.core.robot.EnumRobotPart;
import com.ignis.igrobotics.core.robot.RobotPart;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.Tag;

public class NBTUtils {

    public static Tag serializeParts(RobotPart[] parts) {
        if(parts.length != 6) throw new IllegalArgumentException();
        byte[] ids = new byte[parts.length];
        for(RobotPart part : parts) {
            ids[part.getPart().getID()] = (byte) part.getMaterial().getID();
        }
        return new ByteArrayTag(ids);
    }

    public static RobotPart[] deserializeParts(Tag tag) {
        if(!(tag instanceof ByteArrayTag byteTag)) return new RobotPart[0];
        RobotPart[] parts = new RobotPart[byteTag.size()];
        for(int i = 0; i < parts.length; i++) {
            parts[i] = RobotPart.get(EnumRobotPart.byId(i), EnumRobotMaterial.byId(byteTag.getAsByteArray()[i]));
        }
        return parts;
    }
}
