package com.abysslasea.anvilinnovate.item.stonethrower;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class StoneSkipRenderer extends EntityRenderer<StoneSkipEntity> {

    public StoneSkipRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StoneSkipEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationYXZ(0, (float) Math.toRadians(entity.getYRot()), 0));

        ItemStack itemStack = entity.getItemStack();
        if (itemStack.isEmpty()) {
            itemStack = new ItemStack(Items.STONE);
        }

        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(-0.5, 0, -0.5);
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            blockRenderer.renderSingleBlock(state, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        } else {
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    null,
                    itemStack,
                    net.minecraft.world.item.ItemDisplayContext.GROUND,
                    false,
                    poseStack,
                    bufferSource,
                    entity.level(),
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(StoneSkipEntity entity) {
        return null;
    }
}