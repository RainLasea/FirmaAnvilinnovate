package com.abysslasea.anvilinnovate;

import com.abysslasea.anvilinnovate.block.flint.ChiseledFlintSlabBlockRenderer;
import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.item.stonethrower.ThrowingStones;
import com.abysslasea.anvilinnovate.item.stonethrower.StoneSkipRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "anvilinnovate", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(
                    ModBlocks.CARVING_SLAB_BE.get(),
                    ChiseledFlintSlabBlockRenderer::new
            );
            EntityRenderers.register(ThrowingStones.STONE_SKIP.get(), StoneSkipRenderer::new);
        });
    }
}