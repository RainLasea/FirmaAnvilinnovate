package com.abysslasea.anvilinnovate.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBiomeModifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, "anvilinnovate");

    public static final RegistryObject<Codec<AddFlintBiomeModifier>> ADD_FLINT_CODEC =
            BIOME_MODIFIERS.register("add_flint", () -> AddFlintBiomeModifier.CODEC);

    public record AddFlintBiomeModifier(Holder<PlacedFeature> featureHolder) implements BiomeModifier {
        public static final Codec<AddFlintBiomeModifier> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        PlacedFeature.CODEC.fieldOf("feature").forGetter(AddFlintBiomeModifier::featureHolder)
                ).apply(inst, AddFlintBiomeModifier::new)
        );

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD) {
                builder.getGenerationSettings().addFeature(
                        GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                        featureHolder
                );
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }
}
