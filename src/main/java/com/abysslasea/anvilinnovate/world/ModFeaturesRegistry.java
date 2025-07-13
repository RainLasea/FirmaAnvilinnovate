package com.abysslasea.anvilinnovate.world;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeaturesRegistry {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, "anvilinnovate");

    public static final RegistryObject<Feature<SimpleBlockConfiguration>> FLINT_GROUNDCOVER =
            FEATURES.register("flint_groundcover", FlintGroundCoverFeature::new);
}