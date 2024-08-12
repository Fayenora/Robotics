package com.ignis.norabotics.datagen;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.robot.EnumRobotMaterial;
import com.ignis.norabotics.common.robot.EnumRobotPart;
import com.ignis.norabotics.common.robot.RobotPart;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
public class TextureGenerator implements DataProvider {

    public static final Path INPUT_PATH = Path.of("C:/Users/Nathan/Desktop/MOOOOODS/Minecraft/Modding/Newer Workspaces/Robotics - 1.19.3/src/main/resources/assets");
    public static final String FORMAT = "png";

    private final PackOutput.PathProvider pathProvider;

    public TextureGenerator(PackOutput packOutput) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "textures");
    }

    public void generateTextures(CachedOutput output, Path input) {
        try {
            for(EnumRobotPart part : EnumRobotPart.values()) {
                ResourceLocation itemBaseResource = new ResourceLocation(Robotics.MODID, "textures/item/robot/robot_" + part.getName());
                ResourceLocation robotBaseResource = new ResourceLocation(Robotics.MODID, "textures/robot/limbs/" + part.getName());
                ResourceLocation robotColorResource = new ResourceLocation(Robotics.MODID, "textures/robot/color/" + part.getName());
                BufferedImage itemBaseTexture = read(input, itemBaseResource);
                BufferedImage robotBaseTexture = read(input, robotBaseResource);
                BufferedImage robotColorTexture = read(input, robotColorResource);
                for(EnumRobotMaterial material : EnumRobotMaterial.valuesWithoutEmpty()) {
                    saveStable(output, itemBaseTexture, new ResourceLocation(Robotics.MODID, "item/robot/" + material.getName() + "/robot_" + part.getName()));
                    saveStable(output, robotBaseTexture, RobotPart.get(part, material).getLimbResourceLocation());
                }
                for(DyeColor color : DyeColor.values()) {
                    saveStable(output, robotColorTexture, RobotPart.get(part, EnumRobotMaterial.NONE).getColorResourceLocation(color));
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return CompletableFuture.runAsync(() -> generateTextures(pOutput, INPUT_PATH));
    }

    @Override
    public String getName() {
        return Robotics.MODNAME + " Textures";
    }

    private BufferedImage recolor(BufferedImage image, int color) {
        return image;
    }

    private static CompletableFuture<?> saveStable(CachedOutput pOutput, BufferedImage image, Path pPath) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);
                ByteArrayOutputStream writer = new ByteArrayOutputStream();
                ImageIO.write(image, FORMAT, writer);
                hashingoutputstream.write(writer.toByteArray());
                pOutput.writeIfNeeded(pPath, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
            } catch (IOException ioexception) {
                LOGGER.error("Failed to save file to {}", pPath, ioexception);
            }

        }, Util.backgroundExecutor());
    }

    private CompletableFuture<?> saveStable(CachedOutput output, BufferedImage image, ResourceLocation path) {
        String[] fileWithFormat = path.getPath().split("\\.");
        ResourceLocation file = path.withPath(fileWithFormat[0]);
        String format = fileWithFormat.length > 1 ? fileWithFormat[1] : FORMAT;
        return saveStable(output, image, pathProvider.file(file, format));
    }

    private BufferedImage read(Path input, ResourceLocation resourceLocation) throws IOException {
        File file = new File(input.resolve(resourceLocation.getNamespace() + "/" + resourceLocation.getPath() + "." + FORMAT).toUri());
        return ImageIO.read(file);
    }
}
