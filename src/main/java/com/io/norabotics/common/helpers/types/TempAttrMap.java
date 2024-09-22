package com.io.norabotics.common.helpers.types;


import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class TempAttrMap {

    private final Map<Attribute, AttributeInstance> attributes = Maps.newHashMap();
    private final AttributeSupplier supplier;

    public TempAttrMap(AttributeSupplier pSupplier) {
        this.supplier = pSupplier;
    }

    @Nullable
    public AttributeInstance getInstance(Attribute pAttribute) {
        return this.attributes.computeIfAbsent(pAttribute, (p_22188_) -> {
            return this.supplier.createInstance(a -> {}, p_22188_);
        });
    }

    @Nullable
    public AttributeInstance getInstance(Holder<Attribute> pAttribute) {
        return this.getInstance(pAttribute.value());
    }

    public boolean hasAttribute(Attribute pAttribute) {
        return this.attributes.get(pAttribute) != null || this.supplier.hasAttribute(pAttribute);
    }

    public boolean hasAttribute(Holder<Attribute> pAttribute) {
        return this.hasAttribute(pAttribute.value());
    }

    public boolean hasModifier(Attribute pAttribute, UUID pUuid) {
        AttributeInstance attributeinstance = this.attributes.get(pAttribute);
        return attributeinstance != null ? attributeinstance.getModifier(pUuid) != null : this.supplier.hasModifier(pAttribute, pUuid);
    }

    public boolean hasModifier(Holder<Attribute> pAttribute, UUID pUuid) {
        return this.hasModifier(pAttribute.value(), pUuid);
    }

    public double getValue(Attribute pAttribute) {
        AttributeInstance attributeinstance = this.attributes.get(pAttribute);
        return attributeinstance != null ? attributeinstance.getValue() : this.supplier.getValue(pAttribute);
    }

    public double getBaseValue(Attribute pAttribute) {
        AttributeInstance attributeinstance = this.attributes.get(pAttribute);
        return attributeinstance != null ? attributeinstance.getBaseValue() : this.supplier.getBaseValue(pAttribute);
    }

    public double getModifierValue(Attribute pAttribute, UUID pUuid) {
        AttributeInstance attributeinstance = this.attributes.get(pAttribute);
        return attributeinstance != null ? attributeinstance.getModifier(pUuid).getAmount() : this.supplier.getModifierValue(pAttribute, pUuid);
    }

    public double getModifierValue(Holder<Attribute> pAttribute, UUID pUuid) {
        return this.getModifierValue(pAttribute.value(), pUuid);
    }

    public void removeAttributeModifiers(Multimap<Attribute, AttributeModifier> pMap) {
        pMap.asMap().forEach((p_22152_, p_22153_) -> {
            AttributeInstance attributeinstance = this.attributes.get(p_22152_);
            if (attributeinstance != null) {
                p_22153_.forEach(attributeinstance::removeModifier);
            }
        });
    }

    public void addTransientAttributeModifiers(Multimap<Attribute, AttributeModifier> pMap) {
        pMap.forEach((p_22149_, p_22150_) -> {
            AttributeInstance attributeinstance = this.getInstance(p_22149_);
            if (attributeinstance != null) {
                attributeinstance.removeModifier(p_22150_);
                attributeinstance.addTransientModifier(p_22150_);
            }
        });
    }

    public Collection<Attribute> getAttributes() {
        return attributes.keySet();
    }
}