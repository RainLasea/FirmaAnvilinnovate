package com.abysslasea.anvilinnovate;

import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.template.CarvingTemplateManager;
import com.abysslasea.anvilinnovate.world.ModBiomeModifiers;
import com.abysslasea.anvilinnovate.world.ModFeaturesRegistry;
import com.abysslasea.anvilinnovate.world.ModWorldGenProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Anvilinnovate.MODID)
public class Anvilinnovate {
    public static final String MODID = "anvilinnovate";
    private MinecraftServer server;

    public Anvilinnovate(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModBlocks.BLOCK_ENTITIES.register(bus);
        ModFeaturesRegistry.FEATURES.register(bus);
        NetworkHandler.register();

        bus.addListener(this::onGatherData);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(CarvingTemplateManager.INSTANCE);

        ModBiomeModifiers.BIOME_MODIFIERS.register(bus);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        this.server = event.getServer();
        CarvingTemplateManager.INSTANCE.setServer(server);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CarvingTemplateManager.INSTANCE.syncToPlayer(player);
        }
    }

    private void onGatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            event.getGenerator().addProvider(true, new ModWorldGenProvider(
                    event.getGenerator().getPackOutput(),
                    event.getLookupProvider()
            ));
        }
    }

}