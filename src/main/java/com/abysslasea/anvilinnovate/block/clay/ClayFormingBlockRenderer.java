package com.abysslasea.anvilinnovate.block.clay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ClayFormingBlockRenderer implements BlockEntityRenderer<ClayFormingBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("anvilinnovate","textures/block/clay_colors.png");
    private static final float UNIT = 1f/ClayFormingBlockEntity.SIZE;
    private static final float OFFSET = 0.002f;

    public ClayFormingBlockRenderer(BlockEntityRendererProvider.Context ctx){}

    @Override
    public void render(ClayFormingBlockEntity be,
                       float pt, PoseStack ps,
                       MultiBufferSource buf, int light, int overlay) {
        boolean[][][] tpl = be.getTemplateData();
        boolean[][][] vox = be.getVoxelStates();
        if (tpl == null || vox == null) return;

        int minTemplateY = be.getMinValidY();

        int maxTemplateY = -1;
        for (int z = 0; z < ClayFormingBlockEntity.SIZE; z++) {
            for (int y = ClayFormingBlockEntity.SIZE - 1; y >= 0; y--) {
                for (int x = 0; x < ClayFormingBlockEntity.SIZE; x++) {
                    if (tpl[z][y][x]) {
                        maxTemplateY = Math.max(maxTemplateY, y);
                    }
                }
            }
        }

        int renderMinY = minTemplateY;
        int renderMaxY = Math.max(maxTemplateY, be.getCurrentHeight() - 1);

        ps.pushPose();

        ps.translate(0, OFFSET - minTemplateY * UNIT, 0);

        VertexConsumer solid = buf.getBuffer(RenderType.entityTranslucent(TEXTURE));
        VertexConsumer line = buf.getBuffer(RenderType.lines());

        for (int z = 0; z < ClayFormingBlockEntity.SIZE; z++) {
            for (int y = renderMinY; y <= renderMaxY; y++) {
                if (y < 0 || y >= ClayFormingBlockEntity.SIZE) continue;

                for (int x = 0; x < ClayFormingBlockEntity.SIZE; x++) {
                    if (tpl[z][y][x]) {
                        ps.pushPose();
                        ps.translate(x * UNIT, y * UNIT, z * UNIT);
                        Matrix4f matrix = ps.last().pose();

                        if (vox[z][y][x]) {
                            int hash = x * 73856093 ^ y * 19349663 ^ z * 83492791;
                            float u1 = (Math.abs(hash) % 16) / 16f;
                            float u2 = u1 + 1f / 16f;
                            drawCube(solid, matrix, light, u1, u2);
                        } else {
                            if (y <= be.getCurrentHeight()) {
                                ps.pushPose();
                                ps.translate(0, -UNIT, 0);
                                drawWire(line, ps.last().pose());
                                ps.popPose();
                            }
                        }
                        ps.popPose();
                    }
                }
            }
        }
        ps.popPose();
    }

    private void drawCube(VertexConsumer c, Matrix4f m, int light, float u1, float u2){
        float v1=0,v2=1, m0=0,M=UNIT;
        float[][] n={{0,1,0},{0,-1,0},{0,0,1},{0,0,-1},{1,0,0},{-1,0,0}};
        quad(c,m,M,M,0, 0,M,0, u1,v1,u2,v2, n[0],light);
        quad(c,m,0,M,0, M,M,0, u1,v1,u2,v2, n[0],light);
        quad(c,m,0,0,M, M,0,M, u1,v1,u2,v2, n[1],light);
        quad(c,m,M,0,M, 0,0,M, u1,v1,u2,v2, n[1],light);
        quad(c,m,0,0,M, 0,M,M, u1,v1,u2,v2, n[2],light);
        quad(c,m,M,0,M, M,M,M, u1,v1,u2,v2, n[2],light);
        quad(c,m,M,0,0, M,M,0, u1,v1,u2,v2, n[3],light);
        quad(c,m,0,0,0, 0,M,0, u1,v1,u2,v2, n[3],light);
        quad(c,m,M,0,M, M,0,0, u1,v1,u2,v2, n[4],light);
        quad(c,m,M,M,M, M,M,0, u1,v1,u2,v2, n[4],light);
        quad(c,m,0,0,0, 0,0,M, u1,v1,u2,v2, n[5],light);
        quad(c,m,0,M,0, 0,M,M, u1,v1,u2,v2, n[5],light);
    }

    private void quad(VertexConsumer c, Matrix4f m,
                      float x1,float y1,float z1,
                      float x2,float y2,float z2,
                      float u1,float v1,float u2,float v2,
                      float[] n, int l){
        c.vertex(m,x1,y1,z1).color(255,255,255,255).uv(u1,v2).overlayCoords(0,0).uv2(l).normal(n[0],n[1],n[2]).endVertex();
        c.vertex(m,x2,y1,z2).color(255,255,255,255).uv(u2,v2).overlayCoords(0,0).uv2(l).normal(n[0],n[1],n[2]).endVertex();
        c.vertex(m,x2,y2,z2).color(255,255,255,255).uv(u2,v1).overlayCoords(0,0).uv2(l).normal(n[0],n[1],n[2]).endVertex();
        c.vertex(m,x1,y2,z1).color(255,255,255,255).uv(u1,v1).overlayCoords(0,0).uv2(l).normal(n[0],n[1],n[2]).endVertex();
    }

    private void drawWire(VertexConsumer c, Matrix4f m) {
        float M = UNIT, c0 = 0;
        float r = 0, g = 0, b = 1, a = 1;

        line(c, m, c0, c0, c0, M, c0, c0, r, g, b, a);
        line(c, m, M, c0, c0, M, c0, M, r, g, b, a);
        line(c, m, M, c0, M, c0, c0, M, r, g, b, a);
        line(c, m, c0, c0, M, c0, c0, c0, r, g, b, a);

        line(c, m, c0, M, c0, M, M, c0, r, g, b, a);
        line(c, m, M, M, c0, M, M, M, r, g, b, a);
        line(c, m, M, M, M, c0, M, M, r, g, b, a);
        line(c, m, c0, M, M, c0, M, c0, r, g, b, a);

        line(c, m, c0, c0, c0, c0, M, c0, r, g, b, a);
        line(c, m, M, c0, c0, M, M, c0, r, g, b, a);
        line(c, m, M, c0, M, M, M, M, r, g, b, a);
        line(c, m, c0, c0, M, c0, M, M, r, g, b, a);
    }

    private void line(VertexConsumer c, Matrix4f m,
                      float x1,float y1,float z1,
                      float x2,float y2,float z2,
                      float r,float g,float b,float a){
        c.vertex(m,x1,y1,z1).color(r,g,b,a).normal(0,1,0).endVertex();
        c.vertex(m,x2,y2,z2).color(r,g,b,a).normal(0,1,0).endVertex();
    }
}