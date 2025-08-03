package com.abysslasea.anvilinnovate.template;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CarvingTemplate {
    private final ResourceLocation id;
    private final String name;
    private final String type;
    private final ItemStack result;
    private final boolean[][][] pattern;
    private final int sizeX, sizeY, sizeZ;

    public CarvingTemplate(ResourceLocation id, String name, String type, boolean[][][] pattern, ItemStack result) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.result = result;
        this.sizeZ = pattern.length;
        this.sizeY = pattern[0].length;
        this.sizeX = pattern[0][0].length;
        this.pattern = validatePattern(pattern);
    }

    private boolean[][][] validatePattern(boolean[][][] pattern) {
        for (int z = 0; z < pattern.length; z++) {
            if (pattern[z].length != sizeY) throw new IllegalArgumentException("All layers must have " + sizeY + " rows");
            for (int y = 0; y < sizeY; y++) {
                if (pattern[z][y].length != sizeX) throw new IllegalArgumentException("All rows must have " + sizeX + " columns");
            }
        }
        return pattern;
    }

    public static CarvingTemplate fromJson(ResourceLocation id, JsonObject json) {
        String name = json.has("name") ? json.get("name").getAsString() : id.getPath();
        String type = json.has("type") ? json.get("type").getAsString() : "carving_template";

        ItemStack result = ItemStack.EMPTY;
        if (json.has("output")) {
            String resultId = json.get("output").getAsString();
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(resultId));
            if (item != null) {
                result = new ItemStack(item);
            }
        }

        JsonArray patternArray = json.getAsJsonArray("pattern");

        boolean[][][] pattern;
        if (patternArray.get(0).isJsonPrimitive()) { // 2D flat
            int size = patternArray.size();
            pattern = new boolean[1][size][size];
            for (int y = 0; y < size; y++) {
                String row = patternArray.get(y).getAsString();
                if (row.length() != size) {
                    throw new IllegalArgumentException("Row " + y + " must have " + size + " columns");
                }
                for (int x = 0; x < size; x++) {
                    pattern[0][y][x] = row.charAt(x) == '#';
                }
            }
        } else {
            int layers = patternArray.size();
            pattern = new boolean[layers][][];
            for (int z = 0; z < layers; z++) {
                JsonArray layerArray = patternArray.get(z).getAsJsonArray();
                int rows = layerArray.size();
                pattern[z] = new boolean[rows][];
                for (int y = 0; y < rows; y++) {
                    String row = layerArray.get(y).getAsString();
                    int cols = row.length();
                    pattern[z][y] = new boolean[cols];
                    for (int x = 0; x < cols; x++) {
                        pattern[z][y][x] = row.charAt(x) == '#';
                    }
                }
            }
        }

        return new CarvingTemplate(id, name, type, pattern, result);
    }

    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeUtf(name);
        buf.writeUtf(type);
        buf.writeItem(result);
        buf.writeVarInt(sizeZ);
        buf.writeVarInt(sizeY);
        buf.writeVarInt(sizeX);
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    buf.writeBoolean(pattern[z][y][x]);
                }
            }
        }
    }

    public static CarvingTemplate readFromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        String name = buf.readUtf();
        String type = buf.readUtf(); // 读取模板类型
        ItemStack result = buf.readItem();
        int z = buf.readVarInt();
        int y = buf.readVarInt();
        int x = buf.readVarInt();
        boolean[][][] pattern = new boolean[z][y][x];
        for (int dz = 0; dz < z; dz++) {
            for (int dy = 0; dy < y; dy++) {
                for (int dx = 0; dx < x; dx++) {
                    pattern[dz][dy][dx] = buf.readBoolean();
                }
            }
        }
        return new CarvingTemplate(id, name, type, pattern, result);
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Component getDisplayName() {
        return Component.translatable(name);
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public boolean[][][] getPattern() {
        return this.pattern;
    }

    public boolean shouldCarve(int x, int y, int z) {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ && !pattern[z][y][x];
    }

    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }
}