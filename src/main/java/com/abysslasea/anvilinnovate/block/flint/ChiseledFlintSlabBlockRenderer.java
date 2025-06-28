package com.abysslasea.anvilinnovate.block.flint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ChiseledFlintSlabBlockRenderer implements BlockEntityRenderer<ChiseledFlintSlabBlockEntity> {
    private static final float GRID_HEIGHT = 0.1f;
    private static final float CELL_SIZE = 1f / 12f;
    private static final int COLOR_R = 0x33;
    private static final int COLOR_G = 0x66;
    private static final int COLOR_B = 0xFF;
    private static final int COLOR_A = 0xFF;

    private final BlockRenderDispatcher blockRenderer;

    public ChiseledFlintSlabBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(ChiseledFlintSlabBlockEntity entity, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (entity.getTemplateId() == null) return;

        poseStack.pushPose();

        BlockState smallBlock = Blocks.STONE.defaultBlockState();

        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                if (!entity.isCarved(x, y)) {
                    float dx = x * CELL_SIZE;
                    float dz = y * CELL_SIZE;
                    float dy = -0.1f;

                    poseStack.pushPose();
                    poseStack.translate(dx, dy + GRID_HEIGHT, dz); // 修改Y坐标
                    poseStack.scale(CELL_SIZE, GRID_HEIGHT, CELL_SIZE);
                    blockRenderer.renderSingleBlock(smallBlock, poseStack, buffer, packedLight, packedOverlay);
                    poseStack.popPose();
                }
            }
        }

        MultiBufferSource.BufferSource lineBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lineConsumer = lineBuffer.getBuffer(RenderType.lines());

        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                if (!entity.isCarved(x, y)) {
                    float dx = x * CELL_SIZE;
                    float dz = y * CELL_SIZE;
                    float dy = -0.1f;

                    renderCubeWireframe(poseStack, lineConsumer,
                            dx, dy + GRID_HEIGHT, dz, // 修改Y坐标
                            CELL_SIZE, GRID_HEIGHT, CELL_SIZE);
                }
            }
        }

        lineBuffer.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private void renderCubeWireframe(PoseStack poseStack, VertexConsumer consumer,
                                     float x, float y, float z,
                                     float sx, float sy, float sz) {
        Vec3[] corners = new Vec3[]{
                new Vec3(x, y, z),
                new Vec3(x + sx, y, z),
                new Vec3(x + sx, y, z + sz),
                new Vec3(x, y, z + sz),
                new Vec3(x, y + sy, z),
                new Vec3(x + sx, y + sy, z),
                new Vec3(x + sx, y + sy, z + sz),
                new Vec3(x, y + sy, z + sz)
        };

        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0}, // bottom
                {4, 5}, {5, 6}, {6, 7}, {7, 4}, // top
                {0, 4}, {1, 5}, {2, 6}, {3, 7}  // vertical
        };

        for (int[] edge : edges) {
            Vec3 from = corners[edge[0]];
            Vec3 to = corners[edge[1]];

            consumer.vertex(poseStack.last().pose(), (float) from.x, (float) from.y, (float) from.z)
                    .color(COLOR_R, COLOR_G, COLOR_B, COLOR_A)
                    .normal(0f, 1f, 0f)
                    .endVertex();

            consumer.vertex(poseStack.last().pose(), (float) to.x, (float) to.y, (float) to.z)
                    .color(COLOR_R, COLOR_G, COLOR_B, COLOR_A)
                    .normal(0f, 1f, 0f)
                    .endVertex();
        }
    }
}