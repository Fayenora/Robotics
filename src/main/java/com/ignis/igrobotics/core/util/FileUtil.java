package com.ignis.igrobotics.core.util;

import com.google.gson.Gson;
import com.ignis.igrobotics.Robotics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Optional;

public class FileUtil {

    public static void copyFromDefault(String defaultConfigFile, File to) {
        try(CloseableResourceManager datapack = ServerLifecycleHooks.getCurrentServer().getServerResources().resourceManager()) {
            ResourceLocation path = new ResourceLocation(Robotics.MODID, "configs/" + defaultConfigFile);
            Optional<Resource> resource = datapack.getResource(path);
            if(resource.isEmpty()) {
                throw new RuntimeException("Unable to copy file! No file found at " + path);
            }
            InputStream in = resource.get().open();
            FileOutputStream out = new FileOutputStream(to);
            out.write(in.readAllBytes());
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
