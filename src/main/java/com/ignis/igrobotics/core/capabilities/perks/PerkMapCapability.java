package com.ignis.igrobotics.core.capabilities.perks;

import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.util.Tuple;
import com.ignis.igrobotics.definitions.ModAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class PerkMapCapability implements IPerkMapCap {

    protected LivingEntity entity;
    protected AdvancedPerkMap perkMap = new AdvancedPerkMap();
    protected SimpleDataManager values = new SimpleDataManager();

    public PerkMapCapability(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void updateAttributeModifiers() {
        @SuppressWarnings("unchecked")
        AttributeSupplier defaults = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) entity.getType());
        AttributeMap tempAttributeMap = new AttributeMap(defaults);
        for(Tuple<Perk, Integer> tup : perkMap) {
            tempAttributeMap.addTransientAttributeModifiers(tup.getFirst().getAttributeModifiers(tup.getSecond()));
        }
        //Copy the values to the actual attribute map
        for(Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            AttributeInstance instance = entity.getAttributes().getInstance(attribute);
            if(tempAttributeMap.hasAttribute(attribute) && instance != null) {
                instance.setBaseValue(tempAttributeMap.getValue(attribute));
                ModAttributes.onAttributeChanged(entity, instance);
            }
        }
    }

    @Override
    public void add(Perk perk, int level) {
        perkMap.add(perk, level);
    }

    @Override
    public void remove(Perk perk, int level) {
        perkMap.remove(perk, level);
    }

    @Override
    public void merge(IPerkMap other) {
        perkMap.merge(other);
    }

    @Override
    public void diff(IPerkMap toRemove) {
        perkMap.diff(toRemove);
    }

    @Override
    public void clear() {
        perkMap.clear();
    }

    @Override
    public boolean contains(Perk perk) {
        return perkMap.contains(perk);
    }

    @Override
    public int getLevel(Perk perk) {
        return perkMap.getLevel(perk);
    }

    @Override
    public SimpleDataManager values() {
        return values;
    }

    @NotNull
    @Override
    public Iterator<Tuple<Perk, Integer>> iterator() {
        return perkMap.iterator();
    }
}
