package com.abysslasea.anvilinnovate.block.clay;

import com.abysslasea.anvilinnovate.template.CarvingTemplate;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.InputStream;

public class ClayFormingBlockRenderer implements BlockEntityRenderer<ClayFormingBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("anvilinnovate", "textures/block/clay_colors.png");
    private static final float UNIT = 1f / ClayFormingBlockEntity.SIZE;
    private static final float OFFSET = 0.002f;

    private static final int WIRE_R = 0x00;
    private static final int WIRE_G = 0x00;
    private static final int WIRE_B = 0xFF;
    private static final int WIRE_A = 0xFF;

    private NativeImage paletteImage;
    private int paletteSize;

    public ClayFormingBlockRenderer(BlockEntityRendererProvider.Context context) {
        loadPalette();
    }

    private void loadPalette() {
        paletteImage = null;
        paletteSize = 0;

        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(TEXTURE).orElse(null);
            if (resource != null) {
                try (InputStream is = resource.open()) {
                    paletteImage = NativeImage.read(is);
                    paletteSize = paletteImage.getWidth() * paletteImage.getHeight();
                }
            }
        } catch (Exception e) {
            paletteImage = null;
            paletteSize = 0;
        }
    }

    @Override
    public void render(ClayFormingBlockEntity entity, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {

        CarvingTemplate template = entity.getTemplate();
        if (template == null && entity.getTemplateId() != null) {
            return;
        }

        boolean[][][] templateData = entity.getTemplateData();
        boolean[][][] voxelStates = entity.getVoxelStates();

        if (templateData == null || voxelStates == null) {
            return;
        }

        if (paletteImage == null) {
            loadPalette();
        }

        poseStack.pushPose();
        poseStack.translate(0, OFFSET, 0);

        renderSolidVoxels(entity, templateData, voxelStates, template, poseStack, bufferSource, packedLight, packedOverlay);
        renderWireframeVoxels(entity, templateData, voxelStates, template, poseStack, bufferSource, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderSolidVoxels(ClayFormingBlockEntity entity, boolean[][][] templateData, boolean[][][] voxelStates,
                                   CarvingTemplate template, PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedLight, int packedOverlay) {

        VertexConsumer solidBuffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        for (int z = 0; z < ClayFormingBlockEntity.SIZE; z++) {
            for (int y = 0; y < ClayFormingBlockEntity.SIZE; y++) {
                for (int x = 0; x < ClayFormingBlockEntity.SIZE; x++) {
                    if (x >= template.getSizeX() || y >= template.getSizeY() || z >= template.getSizeZ()) {
                        continue;
                    }

                    if (template.shouldCarve(x, y, z) && voxelStates[z][y][x]) {
                        int hash = x * 73856093 ^ y * 19349663 ^ z * 83492791;
                        int r, g, b;

                        if (paletteSize > 0) {
                            int idx = Math.abs(hash) % paletteSize;
                            int px = paletteImage.getPixelRGBA(idx, 0);

                            b = (px >> 16) & 0xFF;
                            g = (px >> 8) & 0xFF;
                            r = px & 0xFF;
                        } else {
                            r = (Math.abs(hash) % 256);
                            g = (Math.abs(hash >> 8) % 256);
                            b = (Math.abs(hash >> 16) % 256);
                        }

                        renderFullCube(solidBuffer, pose, normalMatrix,
                                x * UNIT, y * UNIT, z * UNIT,
                                UNIT, UNIT, UNIT,
                                r, g, b, 255,
                                packedOverlay, packedLight);
                    }
                }
            }
        }
    }

    private void renderWireframeVoxels(ClayFormingBlockEntity entity, boolean[][][] templateData, boolean[][][] voxelStates,
                                       CarvingTemplate template, PoseStack poseStack, MultiBufferSource bufferSource,
                                       int packedLight, int packedOverlay) {

        VertexConsumer lineBuffer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f pose = poseStack.last().pose();

        int maxFillableY = entity.getMaxFillableY();

        for (int z = 0; z < ClayFormingBlockEntity.SIZE; z++) {
            for (int y = 0; y < ClayFormingBlockEntity.SIZE; y++) {
                for (int x = 0; x < ClayFormingBlockEntity.SIZE; x++) {
                    if (x >= template.getSizeX() || y >= template.getSizeY() || z >= template.getSizeZ()) {
                        continue;
                    }

                    if (template.shouldCarve(x, y, z) && !voxelStates[z][y][x] && y <= maxFillableY) {
                        renderVoxelWireframe(lineBuffer, pose, x * UNIT, y * UNIT, z * UNIT);
                    }
                }
            }
        }
    }

    private void renderFullCube(VertexConsumer buffer, Matrix4f pose, Matrix3f normalMatrix,
                                float x, float y, float z,
                                float w, float h, float d,
                                int r, int g, int b, int a,
                                int overlay, int lightmap) {

        float x1 = x, x2 = x + w;
        float y1 = y, y2 = y + h;
        float z1 = z, z2 = z + d;

        float u1 = 0.5f, u2 = 0.5f;
        float v1 = 0.5f, v2 = 0.5f;

        Vector3f top = new Vector3f(0, 1, 0);
        Vector3f bottom = new Vector3f(0, -1, 0);
        Vector3f north = new Vector3f(0, 0, -1);
        Vector3f south = new Vector3f(0, 0, 1);
        Vector3f west = new Vector3f(-1, 0, 0);
        Vector3f east = new Vector3f(1, 0, 0);

        addQuad(buffer, pose,
                x1, y2, z1,
                x1, y2, z2,
                x2, y2, z2,
                x2, y2, z1,
                top, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(buffer, pose,
                x1, y1, z1,
                x2, y1, z1,
                x2, y1, z2,
                x1, y1, z2,
                bottom, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(buffer, pose,
                x1, y1, z1,
                x1, y2, z1,
                x2, y2, z1,
                x2, y1, z1,
                north, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(buffer, pose,
                x1, y1, z2,
                x2, y1, z2,
                x2, y2, z2,
                x1, y2, z2,
                south, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(buffer, pose,
                x1, y1, z1,
                x1, y1, z2,
                x1, y2, z2,
                x1, y2, z1,
                west, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);

        addQuad(buffer, pose,
                x2, y1, z1,
                x2, y2, z1,
                x2, y2, z2,
                x2, y1, z2,
                east, u1, v1, u2, v2, r, g, b, a, overlay, lightmap);
    }

    private void addQuad(VertexConsumer buffer, Matrix4f pose,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         Vector3f normal,
                         float u1, float v1, float u2, float v2,
                         int r, int g, int b, int a,
                         int overlay, int lightmap) {

        buffer.vertex(pose, x1, y1, z1).color(r, g, b, a).uv(u1, v2)
                .overlayCoords(overlay).uv2(lightmap)
                .normal(normal.x(), normal.y(), normal.z()).endVertex();

        buffer.vertex(pose, x2, y2, z2).color(r, g, b, a).uv(u1, v1)
                .overlayCoords(overlay).uv2(lightmap)
                .normal(normal.x(), normal.y(), normal.z()).endVertex();

        buffer.vertex(pose, x3, y3, z3).color(r, g, b, a).uv(u2, v1)
                .overlayCoords(overlay).uv2(lightmap)
                .normal(normal.x(), normal.y(), normal.z()).endVertex();

        buffer.vertex(pose, x4, y4, z4).color(r, g, b, a).uv(u2, v2)
                .overlayCoords(overlay).uv2(lightmap)
                .normal(normal.x(), normal.y(), normal.z()).endVertex();
    }

    private void renderVoxelWireframe(VertexConsumer buffer, Matrix4f pose,
                                      float baseX, float baseY, float baseZ) {
        float min = 0f;
        float max = UNIT;

        renderLine(buffer, pose, baseX + min, baseY + min, baseZ + min, baseX + max, baseY + min, baseZ + min);
        renderLine(buffer, pose, baseX + max, baseY + min, baseZ + min, baseX + max, baseY + min, baseZ + max);
        renderLine(buffer, pose, baseX + max, baseY + min, baseZ + max, baseX + min, baseY + min, baseZ + max);
        renderLine(buffer, pose, baseX + min, baseY + min, baseZ + max, baseX + min, baseY + min, baseZ + min);

        renderLine(buffer, pose, baseX + min, baseY + max, baseZ + min, baseX + max, baseY + max, baseZ + min);
        renderLine(buffer, pose, baseX + max, baseY + max, baseZ + min, baseX + max, baseY + max, baseZ + max);
        renderLine(buffer, pose, baseX + max, baseY + max, baseZ + max, baseX + min, baseY + max, baseZ + max);
        renderLine(buffer, pose, baseX + min, baseY + max, baseZ + max, baseX + min, baseY + max, baseZ + min);

        renderLine(buffer, pose, baseX + min, baseY + min, baseZ + min, baseX + min, baseY + max, baseZ + min);
        renderLine(buffer, pose, baseX + max, baseY + min, baseZ + min, baseX + max, baseY + max, baseZ + min);
        renderLine(buffer, pose, baseX + max, baseY + min, baseZ + max, baseX + max, baseY + max, baseZ + max);
        renderLine(buffer, pose, baseX + min, baseY + min, baseZ + max, baseX + min, baseY + max, baseZ + max);
    }

    private void renderLine(VertexConsumer buffer, Matrix4f pose,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2) {
        buffer.vertex(pose, x1, y1, z1).color(WIRE_R, WIRE_G, WIRE_B, WIRE_A).normal(0, 1, 0).endVertex();
        buffer.vertex(pose, x2, y2, z2).color(WIRE_R, WIRE_G, WIRE_B, WIRE_A).normal(0, 1, 0).endVertex();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}