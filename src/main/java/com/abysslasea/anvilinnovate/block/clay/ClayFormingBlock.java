package com.abysslasea.anvilinnovate.block.clay;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClayFormingBlock extends Block {
    private static final VoxelShape PLACEHOLDER_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public ClayFormingBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(0.5f)
                .noOcclusion()
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PLACEHOLDER_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PLACEHOLDER_SHAPE;
    }
}
