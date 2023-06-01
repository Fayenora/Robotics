package com.ignis.igrobotics.integration.config;

import com.google.gson.Gson;
import com.ignis.igrobotics.Robotics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.compress.utils.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Optional;

public class ConfigUtils {

    public static void copyFromDefault(String defaultConfigFile, File to) {
        try {
            ResourceLocation path = new ResourceLocation(Robotics.MODID, "configs/" + defaultConfigFile);
            //FIXME: Servers need to read the file from a data pack!
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(path);
            if(resource.isEmpty()) {
                throw new RuntimeException("Unable to copy file! No file found at " + path);
            }
            InputStream in = resource.get().open();
            FileOutputStream out = new FileOutputStream(to);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static Object readJson(Gson gson, File file, Class<?> type) {
        Object obj = null;
        try {
            FileReader reader = new FileReader(file);
            obj = gson.fromJson(reader, type);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
