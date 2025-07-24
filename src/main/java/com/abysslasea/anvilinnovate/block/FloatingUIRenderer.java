package com.abysslasea.anvilinnovate.block;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

@Mod.EventBusSubscriber(modid = "anvilinnovate", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FloatingUIRenderer {

    private static long lastSwitchTime = 0;
    private static int currentFlintIndex = 0;

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel world = mc.level;
        if (world == null) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;

        BlockEntity blockEntity = world.getBlockEntity(blockHit.getBlockPos());
        if (!(blockEntity instanceof FloatingUIRenderable renderable)) return;

        BlockPos abovePos = blockEntity.getBlockPos().above();
        BlockState aboveState = world.getBlockState(abovePos);
        if (!aboveState.isAir()) return;

        renderable.setLookedAt(true);

        PoseStack matrix = event.getPoseStack();
        matrix.pushPose();

        double x = blockEntity.getBlockPos().getX() + 0.5;
        double y = blockEntity.getBlockPos().getY() + 0.5;
        double z = blockEntity.getBlockPos().getZ() + 0.5;

        matrix.translate(
                x - mc.getEntityRenderDispatcher().camera.getPosition().x,
                y - mc.getEntityRenderDispatcher().camera.getPosition().y,
                z - mc.getEntityRenderDispatcher().camera.getPosition().z
        );

        float camYaw = mc.gameRenderer.getMainCamera().getYRot();
        float camPitch = mc.gameRenderer.getMainCamera().getXRot();
        matrix.mulPose(new Quaternionf().rotateY((float) Math.toRadians(-camYaw)));
        matrix.mulPose(new Quaternionf().rotateX((float) Math.toRadians(camPitch)));

        float baseScale = 0.04f;
        matrix.scale(baseScale, baseScale, baseScale);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        RegistryAccess registryAccess = mc.level.registryAccess();
        TagKey<Item> carvingFlintsTagKey = TagKey.create(Registries.ITEM, new ResourceLocation("anvilinnovate:carving_flints"));

        List<ItemStack> carvingFlintsItems = registryAccess.registryOrThrow(Registries.ITEM)
                .getTag(carvingFlintsTagKey)
                .map(tag -> tag.stream().map(holder -> new ItemStack(holder.value())).toList())
                .orElse(List.of());

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSwitchTime > 5000) {
            lastSwitchTime = currentTime;
            currentFlintIndex = (currentFlintIndex + 1) % Math.max(carvingFlintsItems.size(), 1);
        }

        float halfSpacing = 4f;

        double dx = 0, dy = 0, dz = 0;
        if (mc.getCameraEntity() != null) {
            dx = x - mc.getCameraEntity().getX();
            dy = y - mc.getCameraEntity().getY();
            dz = z - mc.getCameraEntity().getZ();
        }
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float maxFadeDistance = 1.0f;
        float fadeIntensity = 0.1f;

        float alpha;
        if (distance <= maxFadeDistance) {
            float normalized = (float)(distance / maxFadeDistance);
            alpha = fadeIntensity + (1.0f - fadeIntensity) * normalized;
        } else {
            alpha = 1.0f;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        matrix.pushPose();
        matrix.translate(halfSpacing - 0.5f, 1f, 0f);
        matrix.scale(2.5f, 2.5f, 2.5f);
        itemRenderer.renderStatic(
                null,
                carvingFlintsItems.get(currentFlintIndex),
                ItemDisplayContext.GUI,
                false,
                matrix,
                bufferSource,
                null,
                15728880,
                OverlayTexture.NO_OVERLAY,
                0
        );
        matrix.popPose();

        matrix.pushPose();
        matrix.scale(0.25f, 0.25f, 0.25f);
        int fontColor = ((int)(alpha * 255) << 24) | 0x00FFFFFF;
        mc.font.drawInBatch(
                "+",
                0f,
                0f,
                fontColor,
                false,
                matrix.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0xF000F0,
                OverlayTexture.NO_OVERLAY
        );
        matrix.popPose();

        matrix.pushPose();
        matrix.translate(halfSpacing - 8f, -0.3f, 0f);
        matrix.scale(0.09f, 0.09f, 0.09f);

        RenderSystem.setShaderTexture(0, new ResourceLocation("anvilinnovate", "textures/gui/right_click.png"));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f pose = matrix.last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(pose, 32, 32, 0).uv(0, 0).endVertex();
        buffer.vertex(pose, 32, 0, 0).uv(0, 1).endVertex();
        buffer.vertex(pose, 0, 0, 0).uv(1, 1).endVertex();
        buffer.vertex(pose, 0, 32, 0).uv(1, 0).endVertex();
        tesselator.end();

        matrix.popPose();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        bufferSource.endBatch();
        matrix.popPose();
    }

    public interface FloatingUIRenderable {
        boolean shouldRenderFloatingUI();
        void setLookedAt(boolean lookedAt);
    }
}