package com.ignis.norabotics.client.rendering;

import com.ignis.norabotics.Robotics;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PartialModelEventHandler {

    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation modelLocation : PartialModel.ALL.keySet()) {
            event.register(modelLocation);
        }
    }

    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        PartialModel.populateOnInit = true;
        Map<ResourceLocation, BakedModel> models = event.getModels();

        for (PartialModel partial : PartialModel.ALL.values()) {
            partial.bakedModel = models.get(partial.modelLocation());
        }
    }

}
