package com.abysslasea.anvilinnovate.block.flint;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import com.abysslasea.anvilinnovate.template.CarvingTemplate;
import com.abysslasea.anvilinnovate.template.CarvingTemplateManager;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.InputStream;

public class ChiseledFlintSlabBlockRenderer implements BlockEntityRenderer<ChiseledFlintSlabBlockEntity> {
    private static final float GRID_HEIGHT = 0.1f;
    private static final int GRID_SIZE = 10;
    private static final float TOTAL_RENDER_SIZE = 13f / 16f;
    private static final float CELL_SIZE = TOTAL_RENDER_SIZE / GRID_SIZE;
    private static final float OFFSET = (1f - TOTAL_RENDER_SIZE) / 2f;
    private static final float BASE_Y_OFFSET = 0.002f;

    private static final int BORDER_R = 0x00;
    private static final int BORDER_G = 0x00;
    private static final int BORDER_B = 0xFF;
    private static final int BORDER_A = 0xFF;
    private static final ResourceLocation CHISELED_FLINT_TEXTURE =
            new ResourceLocation("anvilinnovate", "textures/block/chiseledflintslabblock.png");

    private NativeImage textureImage;

    public ChiseledFlintSlabBlockRenderer(BlockEntityRendererProvider.Context context) {
        loadTexture();
    }

    private void loadTexture() {
        try {
            Resource resource = Minecraft.getInstance()
                    .getResourceManager()
                    .getResource(CHISELED_FLINT_TEXTURE)
                    .orElseThrow();
            try (InputStream stream = resource.open()) {
                textureImage = NativeImage.read(stream);
                if (textureImage.getWidth() != GRID_SIZE || textureImage.getHeight() != GRID_SIZE) {
                    createFallbackTexture();
                }
            }
        } catch (Exception e) {
            createFallbackTexture();
        }
    }

    private void createFallbackTexture() {
        textureImage = new NativeImage(GRID_SIZE, GRID_SIZE, false);
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                textureImage.setPixelRGBA(x, y, (x + y) % 2 == 0
                        ? 0xFFFFFFFF
                        : 0xFF000000);
            }
        }
    }

    @Override
    public void render(ChiseledFlintSlabBlockEntity entity, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (entity.getTemplateId() == null || textureImage == null) return;
        CarvingTemplate template = CarvingTemplateManager.getTemplate(entity.getTemplateId());
        if (template == null) return;

        poseStack.pushPose();
        poseStack.translate(0, BASE_Y_OFFSET, 0);

        renderColoredCubes(entity, poseStack, buffer, packedLight, packedOverlay);
        renderCarvingWireframes(entity, template, poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderColoredCubes(ChiseledFlintSlabBlockEntity entity,
                                    PoseStack poseStack, MultiBufferSource buffer,
                                    int packedLight, int packedOverlay) {

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(CHISELED_FLINT_TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (!entity.isCarved(x, y)) {
                    float dx = OFFSET + x * CELL_SIZE;
                    float dz = OFFSET + y * CELL_SIZE;

                    int r = 255, g = 255, b = 255, a = 255;

                    renderFullCube(consumer, pose, normalMatrix,
                            dx, 0, dz,
                            CELL_SIZE, GRID_HEIGHT, CELL_SIZE,
                            r, g, b, a,
                            packedOverlay, packedLight, x, y);
                }
            }
        }
    }

    private void renderFullCube(VertexConsumer c, Matrix4f pose, Matrix3f nm,
                                float x, float y, float z,
                                float w, float h, float d,
                                int r, int g, int b, int a,
                                int overlay, int lightmap,
                                int gridX, int gridY) {
        float x1 = x, x2 = x + w;
        float y1 = y, y2 = y + h;
        float z1 = z, z2 = z + d;

        float u1 = (float) gridX / GRID_SIZE;
        float u2 = (float) (gridX + 1) / GRID_SIZE;
        float v1 = (float) gridY / GRID_SIZE;
        float v2 = (float) (gridY + 1) / GRID_SIZE;

        Vector3f top    = new Vector3f(0, 1, 0);
        Vector3f bottom = new Vector3f(0, -1, 0);
        Vector3f north  = new Vector3f(0, 0, -1);
        Vector3f south  = new Vector3f(0, 0, 1);
        Vector3f west   = new Vector3f(-1, 0, 0);
        Vector3f east   = new Vector3f(1, 0, 0);

        addQuad(c, pose,
                x1, y2, z1,
                x1, y2, z2,
                x2, y2, z2,
                x2, y2, z1,
                top, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(c, pose,
                x1, y1, z1,
                x2, y1, z1,
                x2, y1, z2,
                x1, y1, z2,
                bottom, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(c, pose,
                x1, y1, z1,
                x1, y2, z1,
                x2, y2, z1,
                x2, y1, z1,
                north, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(c, pose,
                x1, y1, z2,
                x2, y1, z2,
                x2, y2, z2,
                x1, y2, z2,
                south, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(c, pose,
                x1, y1, z1,
                x1, y1, z2,
                x1, y2, z2,
                x1, y2, z1,
                west, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(c, pose,
                x2, y1, z1,
                x2, y2, z1,
                x2, y2, z2,
                x2, y1, z2,
                east, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);
    }

    private void addQuad(VertexConsumer c, Matrix4f pose,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         Vector3f normal,
                         float u1, float v1, float u2, float v2,
                         int r, int g, int b, int a,
                         int overlay, int lightmap) {

        c.vertex(pose, x1, y1, z1).color(r, g, b, a).uv(u1, v2)
                .overlayCoords(overlay).uv2(lightmap)
                .normal(normal.x(), normal.y(), normal.z()).endVertex();

        c.vertex(pose, x2, y2, z2).color(r, g, b, a).uv(u1, v1)
                .overlayCoords(overlay).uv2(lightmap)
                .normal(normal.x(), normal.y(), normal.z()).endVertex();

        c.vertex(pose, x3, y3, z3).color(r, g, b, a).uv(u2, v1)
                .overlayCoords(overlay).uv2(lightmap)
                .normal(normal.x(), normal.y(), normal.z()).endVertex();

        c.vertex(pose, x4, y4, z4).color(r, g, b, a).uv(u2, v2)
                .overlayCoords(overlay).uv2(lightmap)
                .normal(normal.x(), normal.y(), normal.z()).endVertex();
    }

    private void renderCarvingWireframes(ChiseledFlintSlabBlockEntity entity,
                                         CarvingTemplate template,
                                         PoseStack poseStack,
                                         MultiBufferSource buffer,
                                         int packedLight, int packedOverlay) {
        VertexConsumer lc = buffer.getBuffer(RenderType.lines());
        Matrix4f pose = poseStack.last().pose();
        float nx = 0, ny = 1, nz = 0;

        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (!entity.isCarved(x, y) && template.shouldCarve(x, y,0)) {
                    float dx = OFFSET + x * CELL_SIZE;
                    float dz = OFFSET + y * CELL_SIZE;

                    renderCubeWireframe(lc, pose, nx, ny, nz, dx, 0, dz, CELL_SIZE, GRID_HEIGHT, CELL_SIZE);
                }
            }
        }
    }

    private void renderCubeWireframe(VertexConsumer c, Matrix4f pose,
                                     float nx, float ny, float nz,
                                     float x, float y, float z,
                                     float w, float h, float d) {
        float x1 = x, x2 = x + w;
        float y1 = y, y2 = y + h;
        float z1 = z, z2 = z + d;

        renderLine(c, pose, nx, ny, nz, x1, y2, z1, x2, y2, z1);
        renderLine(c, pose, nx, ny, nz, x2, y2, z1, x2, y2, z2);
        renderLine(c, pose, nx, ny, nz, x2, y2, z2, x1, y2, z2);
        renderLine(c, pose, nx, ny, nz, x1, y2, z2, x1, y2, z1);

        renderLine(c, pose, nx, ny, nz, x1, y1, z1, x2, y1, z1);
        renderLine(c, pose, nx, ny, nz, x2, y1, z1, x2, y1, z2);
        renderLine(c, pose, nx, ny, nz, x2, y1, z2, x1, y1, z2);
        renderLine(c, pose, nx, ny, nz, x1, y1, z2, x1, y1, z1);

        renderLine(c, pose, nx, ny, nz, x1, y1, z1, x1, y2, z1);
        renderLine(c, pose, nx, ny, nz, x2, y1, z1, x2, y2, z1);
        renderLine(c, pose, nx, ny, nz, x2, y1, z2, x2, y2, z2);
        renderLine(c, pose, nx, ny, nz, x1, y1, z2, x1, y2, z2);
    }

    private void renderLine(VertexConsumer c, Matrix4f pose,
                            float nx, float ny, float nz,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2) {
        c.vertex(pose, x1, y1, z1).color(BORDER_R, BORDER_G, BORDER_B, BORDER_A)
                .normal(nx, ny, nz).endVertex();
        c.vertex(pose, x2, y2, z2).color(BORDER_R, BORDER_G, BORDER_B, BORDER_A)
                .normal(nx, ny, nz).endVertex();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}