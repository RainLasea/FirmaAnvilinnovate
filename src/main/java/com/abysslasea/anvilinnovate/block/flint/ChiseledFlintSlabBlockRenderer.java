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
import net.minecraft.world.phys.Vec3;
import com.abysslasea.anvilinnovate.template.CarvingTemplate;
import com.abysslasea.anvilinnovate.template.CarvingTemplateManager;

public class ChiseledFlintSlabBlockRenderer implements BlockEntityRenderer<ChiseledFlintSlabBlockEntity> {
    // 渲染常量
    private static final float GRID_HEIGHT = 0.1f;
    private static final int GRID_SIZE = 10;
    private static final float TOTAL_RENDER_SIZE = 13f / 16f; // 13像素大小
    private static final float CELL_SIZE = TOTAL_RENDER_SIZE / GRID_SIZE; // 每格大小
    private static final float OFFSET = (1f - TOTAL_RENDER_SIZE) / 2f; // 居中偏移
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

        CarvingTemplate template = CarvingTemplateManager.getTemplate(entity.getTemplateId());
        if (template == null) return;
        poseStack.pushPose();
        renderStoneBlocks(entity, poseStack, buffer, packedLight, packedOverlay);
        renderCarvingWireframes(entity, template, poseStack);
        poseStack.popPose();
    }

    private void renderStoneBlocks(ChiseledFlintSlabBlockEntity entity,
                                   PoseStack poseStack, MultiBufferSource buffer,
                                   int packedLight, int packedOverlay) {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (!entity.isCarved(x, y)) {
                    float dx = OFFSET + x * CELL_SIZE;
                    float dz = OFFSET + y * CELL_SIZE;
                    float dy = -0.1f;

                    poseStack.pushPose();
                    poseStack.translate(dx, dy + GRID_HEIGHT, dz);
                    poseStack.scale(CELL_SIZE, GRID_HEIGHT, CELL_SIZE);
                    blockRenderer.renderSingleBlock(Blocks.STONE.defaultBlockState(),
                            poseStack, buffer,
                            packedLight, packedOverlay);
                    poseStack.popPose();
                }
            }
        }
    }

    private void renderCarvingWireframes(ChiseledFlintSlabBlockEntity entity,
                                         CarvingTemplate template,
                                         PoseStack poseStack) {
        MultiBufferSource.BufferSource lineBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lineConsumer = lineBuffer.getBuffer(RenderType.lines());

        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (!entity.isCarved(x, y) && template.shouldCarve(x, y)) {
                    float dx = OFFSET + x * CELL_SIZE;
                    float dz = OFFSET + y * CELL_SIZE;
                    float dy = -0.1f;

                    renderCubeWireframe(poseStack, lineConsumer,
                            dx, dy + GRID_HEIGHT, dz,
                            CELL_SIZE, GRID_HEIGHT, CELL_SIZE);
                }
            }
        }

        lineBuffer.endBatch();
    }
    private void renderCubeWireframe(PoseStack poseStack, VertexConsumer consumer,
                                     float x, float y, float z,
                                     float sx, float sy, float sz) {
        Vec3[] corners = {
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
                {0, 1}, {1, 2}, {2, 3}, {3, 0}, // 底面
                {4, 5}, {5, 6}, {6, 7}, {7, 4}, // 顶面
                {0, 4}, {1, 5}, {2, 6}, {3, 7}  // 垂直边
        };

        for (int[] edge : edges) {
            Vec3 from = corners[edge[0]];
            Vec3 to = corners[edge[1]];
            consumer.vertex(poseStack.last().pose(), (float)from.x, (float)from.y, (float)from.z)
                    .color(COLOR_R, COLOR_G, COLOR_B, COLOR_A)
                    .normal(0, 1, 0)
                    .endVertex();
            consumer.vertex(poseStack.last().pose(), (float)to.x, (float)to.y, (float)to.z)
                    .color(COLOR_R, COLOR_G, COLOR_B, COLOR_A)
                    .normal(0, 1, 0)
                    .endVertex();
        }
    }
    @Override
    public int getViewDistance() {
        return 64;
    }
}