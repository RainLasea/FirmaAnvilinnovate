package com.abysslasea.anvilinnovate.world;

import net.dries007.tfc.common.blocks.GroundcoverBlockType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.dries007.tfc.common.blocks.TFCBlocks;

public class FlintGroundCoverFeature extends Feature<SimpleBlockConfiguration> {
    public FlintGroundCoverFeature() {
        super(SimpleBlockConfiguration.CODEC);
    }

    private BlockState getFlintState() {
        return TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.FLINT).get().defaultBlockState();
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> context) {
        WorldGenLevel level = context.level();
        ServerLevel serverLevel = level.getLevel();
        if (!WorldTypeUtils.isTFCWorld(serverLevel)) {
            return false;
        }

        BlockPos pos = context.origin();
        RandomSource random = context.random();
        BlockState flintState = getFlintState();

        if (level.isEmptyBlock(pos) && flintState.canSurvive(level, pos)) {
            if (random.nextInt(5) == 0) {
                level.setBlock(pos, flintState, 2);
                return true;
            }
        }
        return false;
    }
}