package com.abysslasea.anvilinnovate.template;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CarvingTemplateManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, CarvingTemplate> TEMPLATES = new HashMap<>();

    public CarvingTemplateManager() {
        super(GSON, "templates");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources,
                         ResourceManager manager, ProfilerFiller profiler) {
        TEMPLATES.clear();
        resources.forEach((id, json) -> {
            CarvingTemplate template = CarvingTemplate.fromJson(id, json.getAsJsonObject());
            TEMPLATES.put(id, template);
            });
    }

    public static CarvingTemplate getTemplate(ResourceLocation id) {
        return TEMPLATES.get(id);
    }

    public static Collection<ResourceLocation> getTemplateIds() {
        return TEMPLATES.keySet();
    }
}