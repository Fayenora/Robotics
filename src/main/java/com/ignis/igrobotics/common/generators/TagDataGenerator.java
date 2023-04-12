package com.ignis.igrobotics.common.generators;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.robot.EnumRobotMaterial;
import com.ignis.igrobotics.definitions.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TagDataGenerator extends ItemTagsProvider {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), (Factory<TagDataGenerator>) output -> new TagDataGenerator(output, event.getLookupProvider(), event.getExistingFileHelper()));
    }

    public TagDataGenerator(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, new ForgeBlockTagsProvider(pOutput, pLookupProvider, existingFileHelper), Robotics.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        int i = 0;
        for(EnumRobotMaterial material : Reference.WIRE_METALS) {
            this.tag(ItemTags.create(new ResourceLocation("forge", "wires/" + material.getName())))
                    .add(ModItems.WIRES[i++].get())
                    .replace(false);
        }
    }
}
