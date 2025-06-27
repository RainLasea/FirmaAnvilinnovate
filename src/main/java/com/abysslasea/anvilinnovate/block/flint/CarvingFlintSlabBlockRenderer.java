package com.abysslasea.anvilinnovate.block.flint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class CarvingFlintSlabBlockRenderer implements BlockEntityRenderer<CarvingFlintSlabBlockEntity> {
    private static final float GRID_HEIGHT = 0.01f;
    private static final int OUTLINE_COLOR = 0xFF3366FF; // 蓝色描边

    public CarvingFlintSlabBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CarvingFlintSlabBlockEntity entity, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (entity.getTemplateId() == null) return;

        poseStack.pushPose();
        poseStack.translate(0, GRID_HEIGHT, 0); // 防止Z-fighting

        VertexConsumer outline = buffer.getBuffer(RenderType.lines());
        renderGridOutline(poseStack, outline, entity);

        poseStack.popPose();
    }

    private void renderGridOutline(PoseStack poseStack, VertexConsumer consumer,
                                   CarvingFlintSlabBlockEntity entity) {
        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                if (!entity.isCarved(x, y)) {
                    renderCellOutline(poseStack, consumer, x, y);
                }
            }
        }
    }

    private void renderCellOutline(PoseStack poseStack, VertexConsumer consumer, int x, int y) {
        float startX = x / 12f;
        float startZ = y / 12f;
        float endX = (x + 1) / 12f;
        float endZ = (y + 1) / 12f;

        // 绘制四边线框
        Vec3[] vertices = {
                new Vec3(startX, 0, startZ),
                new Vec3(endX, 0, startZ),
                new Vec3(endX, 0, endZ),
                new Vec3(startX, 0, endZ)
        };

        for (int i = 0; i < 4; i++) {
            Vec3 from = vertices[i];
            Vec3 to = vertices[(i + 1) % 4];
            consumer.vertex(poseStack.last().pose(), (float)from.x, (float)from.y, (float)from.z)
                    .color(OUTLINE_COLOR)
                    .normal(0, 1, 0)
                    .endVertex();
            consumer.vertex(poseStack.last().pose(), (float)to.x, (float)to.y, (float)to.z)
                    .color(OUTLINE_COLOR)
                    .normal(0, 1, 0)
                    .endVertex();
        }
    }
}