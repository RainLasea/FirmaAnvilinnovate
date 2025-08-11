package com.abysslasea.anvilinnovate.block.clay;

import com.abysslasea.anvilinnovate.NetworkHandler;
import com.abysslasea.anvilinnovate.template.packet.OpenTemplateScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.server.level.ServerPlayer;

public class ClayFormingBlock extends Block implements EntityBlock {

    private static final VoxelShape FULL_SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    private static final VoxelShape BASE_SHAPE = Block.box(0, 0, 0, 16, 1, 16);

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
            if (clayBE.getTemplateId() == null) {
                return FULL_SHAPE;
            }
            double height = Math.max(1, clayBE.getCurrentAbsoluteHeight() + 1) * (16.0 / ClayFormingBlockEntity.SIZE);
            return Block.box(0, 0, 0, 16, Math.min(16, height), 16);
        }
        return FULL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ClayFormingBlockEntity clayBE) {
            if (clayBE.getTemplateId() == null) {
                return FULL_SHAPE;
            }
            int maxLayer = clayBE.getCurrentAbsoluteHeight();
            if (maxLayer < 0) maxLayer = clayBE.getMinValidY();
            double height = (maxLayer + 1) * (16.0 / ClayFormingBlockEntity.SIZE);
            return Block.box(0, 0, 0, 16, Math.min(16, height), 16);
        }
        return Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ClayFormingBlockEntity clayBE)) {
            return InteractionResult.PASS;
        }

        if (clayBE.getTemplateId() != null && clayBE.getTemplate() == null && level.isClientSide()) {
            clayBE.refreshTemplate();
        }

        if (clayBE.getTemplateId() == null) {
            if (!level.isClientSide()) {
                if (player instanceof ServerPlayer sp) {
                    NetworkHandler.sendToClient(sp, new OpenTemplateScreenPacket(pos, "clay_template"));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (clayBE.getTemplate() == null) {
            return InteractionResult.CONSUME;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() == Items.CLAY_BALL) {
            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }
}