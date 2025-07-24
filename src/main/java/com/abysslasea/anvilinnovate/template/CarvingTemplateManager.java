package com.abysslasea.anvilinnovate.template;

import com.abysslasea.anvilinnovate.NetworkHandler;
import com.abysslasea.anvilinnovate.template.packet.SyncTemplatesPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.*;

public class CarvingTemplateManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final CarvingTemplateManager INSTANCE = new CarvingTemplateManager();

    private static final Map<ResourceLocation, CarvingTemplate> SERVER_TEMPLATES = new HashMap<>();
    private static final Map<ResourceLocation, CarvingTemplate> CLIENT_TEMPLATES = new HashMap<>();

    private MinecraftServer server;

    public CarvingTemplateManager() {
        super(GSON, "templates");
    }

    @SubscribeEvent
    public void onReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources,
                         ResourceManager manager, ProfilerFiller profiler) {
        SERVER_TEMPLATES.clear();
        resources.forEach((id, json) -> {
            try {
                CarvingTemplate template = CarvingTemplate.fromJson(id, json.getAsJsonObject());
                SERVER_TEMPLATES.put(id, template);
            } catch (Exception ignored) {}
        });
        if (server != null) {
            syncToPlayers();
        }
    }

    private void syncToPlayers() {
        List<CarvingTemplate> templates = new ArrayList<>(SERVER_TEMPLATES.values());
        server.getPlayerList().getPlayers().forEach(player ->
                NetworkHandler.sendToClient(player, new SyncTemplatesPacket(templates))
        );
    }

    public void syncToPlayer(ServerPlayer player) {
        List<CarvingTemplate> templates = new ArrayList<>(SERVER_TEMPLATES.values());
        NetworkHandler.sendToClient(player, new SyncTemplatesPacket(templates));
    }

    public static CarvingTemplate getTemplate(ResourceLocation id) {
        return isClient() ? CLIENT_TEMPLATES.get(id) : SERVER_TEMPLATES.get(id);
    }

    public static Collection<ResourceLocation> getTemplateIds() {
        return isClient() ? CLIENT_TEMPLATES.keySet() : SERVER_TEMPLATES.keySet();
    }

    @OnlyIn(Dist.CLIENT)
    public static void clearClientTemplates() {
        CLIENT_TEMPLATES.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerClientTemplate(CarvingTemplate template) {
        if (template != null && template.getId() != null) {
            CLIENT_TEMPLATES.put(template.getId(), template);
        }
    }

    private static boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }
}
