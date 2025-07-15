package com.abysslasea.anvilinnovate.item.stonethrower;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;

public class StoneSkipRenderer extends EntityRenderer<StoneSkipEntity> {
    public StoneSkipRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StoneSkipEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(new Quaternionf().rotationAxis(entity.getYRot() * ((float)Math.PI / 180F), 0.0F, 1.0F, 0.0F));

        ItemStack itemStack = entity.getItemStack();
        if (itemStack.isEmpty()) {
            itemStack = new ItemStack(Items.STONE);
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(
                null,
                itemStack,
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                bufferSource,
                entity.level(),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(StoneSkipEntity entity) {
        return null;
    }
}
