package com.abysslasea.anvilinnovate.world;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class ModFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_FLINT =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    new ResourceLocation("anvilinnovate", "flint_groundcover"));

    public static final ResourceKey<PlacedFeature> PLACED_FLINT =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    new ResourceLocation("anvilinnovate", "flint_groundcover_placed"));

    public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(CONFIGURED_FLINT, new ConfiguredFeature<>(
                new FlintGroundCoverFeature(),
                new SimpleBlockConfiguration(
                        SimpleStateProvider.simple(Blocks.AIR)
                )
        ));
    }

    public static void bootstrapPlaced(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> featureHolder = configured.getOrThrow(CONFIGURED_FLINT);

        context.register(PLACED_FLINT, new PlacedFeature(
                featureHolder,
                List.of(
                        RarityFilter.onAverageOnceEvery(3),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BiomeFilter.biome()
                )
        ));
    }
}