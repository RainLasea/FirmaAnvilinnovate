package com.abysslasea.anvilinnovate.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;

public class WorldTypeUtils {

    public static boolean isTFCWorld(ServerLevel level) {
        if (level == null) return false;

        try {
            Registry<LevelStem> registry = level.getServer().registryAccess().registryOrThrow(Registries.LEVEL_STEM);
            LevelStem overworldStem = registry.get(LevelStem.OVERWORLD);
            if (overworldStem == null) return false;

            ChunkGenerator generator = overworldStem.generator();

            String genClassName = generator.getClass().getName();
            return genClassName.equals("net.dries007.tfc.world.TFCChunkGenerator");

        } catch (Exception e) {
            return false;
        }
    }
}
