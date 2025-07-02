package com.abysslasea.anvilinnovate.block.flint;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChiseledFlintSlabBlock extends Block implements EntityBlock {
    public ChiseledFlintSlabBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(0.5f)
                .noOcclusion()
                .noCollission()
                .instabreak()
        );
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChiseledFlintSlabBlockEntity(pos, state);
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0, 0, 0, 16, 1.6, 16);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (player.getItemInHand(hand).getItem() == Items.FLINT) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ChiseledFlintSlabBlockEntity slab)) {
            return InteractionResult.PASS;
        }

        if (slab.getTemplateId() == null) {
            return InteractionResult.PASS;
        }

        Vec3 hitPos = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());

        float offset = (1f - 13f / 16f) / 2f;
        float cellSize = (13f / 16f) / 10f;

        float localX = (float) hitPos.x() - offset;
        float localZ = (float) hitPos.z() - offset;

        int gridX = (int) (localX / cellSize);
        int gridY = (int) (localZ / cellSize);

        if (gridX >= 0 && gridX < 10 && gridY >= 0 && gridY < 10) {
            if (slab.tryCarve(gridX, gridY)) {
                level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.5f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
