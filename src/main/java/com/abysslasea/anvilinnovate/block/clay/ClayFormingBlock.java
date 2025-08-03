package com.abysslasea.anvilinnovate.block.clay;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClayFormingBlock extends Block implements EntityBlock {

    private static final VoxelShape FULL_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public ClayFormingBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(0.5f)
                .noOcclusion()
                .instabreak()
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClayFormingBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ClayFormingBlockEntity clayBE) {
            int maxLayer = clayBE.getMaxCompleteLayer();
            if (maxLayer < 0) maxLayer = 0;
            double height = (maxLayer + 1) * (16.0 / ClayFormingBlockEntity.SIZE);
            return Block.box(0, 0, 0, 16, height, 16);
        }
        return FULL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ClayFormingBlockEntity clayBE) {
            int maxLayer = clayBE.getMaxCompleteLayer();
            if (maxLayer < 0) maxLayer = 0;
            double height = (maxLayer + 1) * (16.0 / ClayFormingBlockEntity.SIZE);
            return Block.box(0, 0, 0, 16, height, 16);
        }
        return Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ClayFormingBlockEntity clayBE)) return InteractionResult.PASS;

        double hitX = hit.getLocation().x - pos.getX();
        double hitY = hit.getLocation().y - pos.getY();
        double hitZ = hit.getLocation().z - pos.getZ();

        int voxelX = (int) (hitX * ClayFormingBlockEntity.SIZE);
        int voxelY = (int) (hitY * ClayFormingBlockEntity.SIZE);
        int voxelZ = (int) (hitZ * ClayFormingBlockEntity.SIZE);

        if (voxelX < 0 || voxelX >= ClayFormingBlockEntity.SIZE ||
                voxelY < 0 || voxelY >= ClayFormingBlockEntity.SIZE ||
                voxelZ < 0 || voxelZ >= ClayFormingBlockEntity.SIZE) {
            return InteractionResult.FAIL;
        }

        boolean changed = clayBE.tryForm(voxelX, voxelY, voxelZ);
        if (changed) {
            level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 0.5f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}