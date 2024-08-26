package com.ignis.norabotics.datagen;

import com.ignis.norabotics.common.robot.EnumRobotMaterial;
import com.ignis.norabotics.common.robot.EnumRobotPart;
import com.ignis.norabotics.definitions.robotics.ModPerks;
import net.minecraft.data.PackOutput;

public class ModuleGenerator extends ModuleProvider {
    public ModuleGenerator(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildModules() {

        // Perks
        universal(EnumRobotMaterial.IRON, ModPerks.PERK_MAGNETIC.get());
        universal(EnumRobotMaterial.GOLD, ModPerks.PERK_INSPIRE.get());
        addCorePerk(EnumRobotMaterial.GOLD, ModPerks.PERK_PRECIOUS.get());
        addPerk(EnumRobotMaterial.GOLD, EnumRobotPart.BODY, ModPerks.PERK_INSPIRE.get());
        universal(EnumRobotMaterial.DIAMOND, ModPerks.PERK_REFLECTIVE.get());
        universal(EnumRobotMaterial.DIAMOND, ModPerks.PERK_ROBUST.get());
        addPerk(EnumRobotMaterial.DIAMOND, EnumRobotPart.BODY, ModPerks.PERK_REFLECTIVE.get());
        addPerk(EnumRobotMaterial.DIAMOND, EnumRobotPart.BODY, ModPerks.PERK_ROBUST.get());
        addCorePerk(EnumRobotMaterial.NETHERITE, ModPerks.PERK_INVULNERABILITY.get());
        addPerk(EnumRobotMaterial.NETHERITE, EnumRobotPart.BODY, ModPerks.PERK_ROBUST.get());
        addArmPerk(EnumRobotMaterial.NETHERITE, ModPerks.PERK_IMPACT.get(), 2);
        addLegPerk(EnumRobotMaterial.NETHERITE, ModPerks.PERK_ROBUST.get());
        universal(EnumRobotMaterial.COPPER, ModPerks.PERK_CORRODABLE.get());
        universal(EnumRobotMaterial.WEATHERED_COPPER, ModPerks.PERK_CORRODABLE.get());
        universal(EnumRobotMaterial.WEATHERED_COPPER, ModPerks.PERK_UNRELIABLE.get());
        universal(EnumRobotMaterial.OXIDIZED_COPPER, ModPerks.PERK_CORRODABLE.get());
        universal(EnumRobotMaterial.OXIDIZED_COPPER, ModPerks.PERK_UNRELIABLE.get(), 2);
        universal(EnumRobotMaterial.TIN, ModPerks.PERK_VULNERABILITY.get());
        addArmPerk(EnumRobotMaterial.TIN, ModPerks.PERK_IMPACT.get(), 4);
        addLegPerk(EnumRobotMaterial.TIN, ModPerks.PERK_EFFICIENT.get(), 2);
        addPerk(EnumRobotMaterial.ALUMINIUM, EnumRobotPart.HEAD, ModPerks.PERK_MODULAR.get());
        addPerk(EnumRobotMaterial.ALUMINIUM, EnumRobotPart.BODY, ModPerks.PERK_VOIDANT.get());
        addLimbPerk(EnumRobotMaterial.ALUMINIUM, ModPerks.PERK_MODULAR.get());
        universal(EnumRobotMaterial.NICKEL, ModPerks.PERK_MAGNETIC.get());
        universal(EnumRobotMaterial.SILVER, ModPerks.PERK_ATTRACTANT.get());
        universal(EnumRobotMaterial.SILVER, ModPerks.PERK_THERMAL_CONDUCTIVITY.get());
        universal(EnumRobotMaterial.SILVER, ModPerks.PERK_ELECTRICAL_CONDUCTIVITY.get());
        addPerk(EnumRobotMaterial.SILVER, EnumRobotPart.BODY, ModPerks.PERK_ELECTRICAL_CONDUCTIVITY.get(), 2);
        universal(EnumRobotMaterial.LEAD, ModPerks.PERK_STEADY.get());
        addPerk(EnumRobotMaterial.LEAD, EnumRobotPart.BODY, ModPerks.PERK_STEADY.get(), 2);
        universal(EnumRobotMaterial.BRONZE, ModPerks.PERK_MODULE_BUFF.get());
        universal(EnumRobotMaterial.BRONZE, ModPerks.PERK_MODULE_EFFICIENCY.get());
        addPerk(EnumRobotMaterial.BRONZE, EnumRobotPart.HEAD, ModPerks.PERK_MODULE_BUFF.get(), 2);
        addPerk(EnumRobotMaterial.BRONZE, EnumRobotPart.BODY, ModPerks.PERK_MODULE_EFFICIENCY.get(), 2);
        addPerk(EnumRobotMaterial.CONSTANTAN, EnumRobotPart.BODY, ModPerks.PERK_STACKED.get());
        addArmPerk(EnumRobotMaterial.CONSTANTAN, ModPerks.PERK_STACKED.get());
        addLegPerk(EnumRobotMaterial.CONSTANTAN, ModPerks.PERK_ACCELERATION.get());
        universal(EnumRobotMaterial.STEEL, ModPerks.PERK_MASS_PRODUCED.get());
        addPerk(EnumRobotMaterial.STEEL, EnumRobotPart.HEAD, ModPerks.PERK_MODULE_BUFF.get());
        addPerk(EnumRobotMaterial.STEEL, EnumRobotPart.BODY, ModPerks.PERK_MODULE_BUFF.get(), 2);
        addLegPerk(EnumRobotMaterial.STEEL, ModPerks.PERK_MODULE_EFFICIENCY.get());
        universal(EnumRobotMaterial.ELECTRUM, ModPerks.PERK_EFFICIENT.get());
        addPerk(EnumRobotMaterial.ELECTRUM, EnumRobotPart.BODY, ModPerks.PERK_EFFICIENT.get(), 2);
        addLimbPerk(EnumRobotMaterial.ELECTRUM, ModPerks.PERK_CHARGE.get());
        universal(EnumRobotMaterial.PLATINUM, ModPerks.PERK_SHIELD.get(), 2);
        universal(EnumRobotMaterial.PLATINUM, ModPerks.PERK_ROBUST.get());
        universal(EnumRobotMaterial.PLATINUM, ModPerks.PERK_MODULE_EFFICIENCY.get());
        addPerk(EnumRobotMaterial.PLATINUM, EnumRobotPart.BODY, ModPerks.PERK_SHIELD.get(), 2);
        addPerk(EnumRobotMaterial.PLATINUM, EnumRobotPart.BODY, ModPerks.PERK_SHIELD.get(), 4);
        addArmPerk(EnumRobotMaterial.PLATINUM, ModPerks.PERK_IMPACT.get(), 2);
        universal(EnumRobotMaterial.IRIDIUM, ModPerks.PERK_ARMOR_SHRED.get());
        universal(EnumRobotMaterial.SIGNALUM, ModPerks.PERK_MODULE_COOLDOWN.get());
        addArmPerk(EnumRobotMaterial.SIGNALUM, ModPerks.PERK_LOGISTICIAN.get());
        universal(EnumRobotMaterial.LUMIUM, ModPerks.PERK_LUMINOUS.get());
        universal(EnumRobotMaterial.ENDERIUM, ModPerks.PERK_LINKED.get());
        addCorePerk(EnumRobotMaterial.ENDERIUM, ModPerks.PERK_CONNEXIOM.get());
        universal(EnumRobotMaterial.DARK_STEEL, ModPerks.PERK_MASS_PRODUCED.get());
        addCorePerk(EnumRobotMaterial.DARK_STEEL, ModPerks.PERK_INVULNERABILITY.get());
        universal(EnumRobotMaterial.END_STEEL, ModPerks.PERK_INVULNERABILITY.get());
        addPerk(EnumRobotMaterial.END_STEEL, EnumRobotPart.BODY, ModPerks.PERK_VOIDANT.get());
        addCorePerk(EnumRobotMaterial.PSIMETAL, ModPerks.PERK_LINKED.get());

        // Stats
        stats(EnumRobotMaterial.IRON, 3, 0, 0, 5, 0, 0, 2, 0, 0);
        stats(EnumRobotMaterial.GOLD, 2, 0, 0, 4, 0, 0, 1, 0, 0);
        stats(EnumRobotMaterial.DIAMOND, 5, 2, 0, 9, 3, 0, 3, 1, 0);
        stats(EnumRobotMaterial.NETHERITE, 5, 3, 1, 9, 4, 2, 3, 2, 1);
        stats(EnumRobotMaterial.COPPER, 1, 0, 0, 3, 0, 0, 1, 0, 0);
        stats(EnumRobotMaterial.TIN, 2, 1, 0, 4, 2, 0, 2, 0, 0);
        stats(EnumRobotMaterial.ALUMINIUM, 3, 0, 0, 5, 0, 0, 2, 0, 0);
        stats(EnumRobotMaterial.NICKEL, 4, 3, 0, 7, 4, 0, 2, 1, 0);
        stats(EnumRobotMaterial.SILVER, 3, 0, 0, 5, 0, 0, 2, 0, 0);
        stats(EnumRobotMaterial.LEAD, 4, 0, 0, 6, 0, 0, 2, 0, 0);
        stats(EnumRobotMaterial.BRONZE, 4, 0, 0, 8, 0, 0, 2, 0, 0);
        stats(EnumRobotMaterial.CONSTANTAN, 4, 0, 0, 6, 0, 0, 2, 0, 0);
        stats(EnumRobotMaterial.STEEL, 4, 0, 0, 6, 0, 0, 2, 0, 0);
        stats(EnumRobotMaterial.ELECTRUM, 4, 0, 0, 6, 0, 0, 2, 0, 0);
        stats(EnumRobotMaterial.PLATINUM, 5, 0, 1, 8, 0, 2, 3, 0, 0);
        stats(EnumRobotMaterial.IRIDIUM, 5, 0, 1, 8, 0, 2, 3, 0, 0);
        stats(EnumRobotMaterial.SIGNALUM, 5, 2, 0, 8, 3, 0, 3, 1, 0);
        stats(EnumRobotMaterial.LUMIUM, 6, 0, 0, 10, 0, 0, 4, 0, 0);
        stats(EnumRobotMaterial.ENDERIUM, 5, 2, 0, 8, 3, 0, 3, 1, 0);
        stats(EnumRobotMaterial.DARK_STEEL, 5, 1, 0, 7, 2, 0, 3, 1, 0);
        stats(EnumRobotMaterial.END_STEEL, 5, 1, 1, 9, 2, 2, 3, 1, 1);
        stats(EnumRobotMaterial.OSMIUM, 4, 0, 0, 6, 0, 0, 2, 0, 0);
        stats(EnumRobotMaterial.PSIMETAL, 3, 1, 0, 5, 1, 0, 2, 1, 0);
    }
}
