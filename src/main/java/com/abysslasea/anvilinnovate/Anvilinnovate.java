package com.abysslasea.anvilinnovate;

import com.abysslasea.anvilinnovate.block.ModBlocks;

import com.abysslasea.anvilinnovate.network.CarvingTemplateManager;
import com.abysslasea.anvilinnovate.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Anvilinnovate.MODID)
public class Anvilinnovate {
    public static final String MODID = "anvilinnovate";

    public Anvilinnovate(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModBlocks.BLOCK_ENTITIES.register(bus);
        NetworkHandler.register();
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CarvingTemplateManager());
    }
}