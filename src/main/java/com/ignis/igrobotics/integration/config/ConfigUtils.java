package com.ignis.igrobotics.integration.config;

import com.google.gson.Gson;
import com.ignis.igrobotics.Robotics;
import org.apache.commons.compress.utils.IOUtils;

import javax.annotation.Nullable;
import java.io.*;

public class ConfigUtils {

    public static void copyFromDefault(String defaultConfigFile, File to) {
        try {
            String path = "assets/" + Robotics.MODID + "/configs/" + defaultConfigFile;
            InputStream in = Robotics.class.getClassLoader().getResourceAsStream(path);
            FileOutputStream out = new FileOutputStream(to);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static Object readJson(Gson gson, File file, Class type) {
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
