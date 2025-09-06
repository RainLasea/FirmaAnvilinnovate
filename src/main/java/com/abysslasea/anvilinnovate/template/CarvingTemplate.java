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

    private CarvingTemplate(ResourceLocation id, String name, String type, boolean[][][] pattern, ItemStack result) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.result = result;
        this.pattern = validatePattern(pattern);
        this.sizeZ = pattern.length;
        this.sizeY = pattern[0].length;
        this.sizeX = pattern[0][0].length;
    }

    public static CarvingTemplate fromJson(ResourceLocation id, JsonObject json) {
        String name = getStringOrDefault(json, "name", id.getPath());
        String type = getStringOrDefault(json, "type", "carving_template");
        ItemStack result = parseResultItem(json);
        boolean[][][] pattern = parsePattern(json.getAsJsonArray("pattern"));

        return new CarvingTemplate(id, name, type, pattern, result);
    }

    public static CarvingTemplate readFromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        String name = buf.readUtf();
        String type = buf.readUtf();
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

    private static String getStringOrDefault(JsonObject json, String key, String defaultValue) {
        return json.has(key) ? json.get(key).getAsString() : defaultValue;
    }

    private static ItemStack parseResultItem(JsonObject json) {
        if (!json.has("output")) {
            return ItemStack.EMPTY;
        }

        String resultId = json.get("output").getAsString();
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(resultId));

        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    private static boolean[][][] parsePattern(JsonArray patternArray) {
        if (patternArray.get(0).isJsonPrimitive()) {
            return parse2DPattern(patternArray);
        } else {
            return parse3DPattern(patternArray);
        }
    }

    private static boolean[][][] parse2DPattern(JsonArray patternArray) {
        int size = patternArray.size();
        boolean[][][] pattern = new boolean[1][size][];

        for (int y = 0; y < size; y++) {
            String row = patternArray.get(y).getAsString();
            validateRowLength(y, row, size);

            pattern[0][y] = parseRow(row);
        }

        return pattern;
    }

    private static boolean[][][] parse3DPattern(JsonArray patternArray) {
        int ySize = patternArray.size();
        JsonArray firstLayer = patternArray.get(0).getAsJsonArray();
        int zSize = firstLayer.size();
        int xSize = firstLayer.get(0).getAsString().length();

        boolean[][][] pattern = new boolean[zSize][ySize][xSize];

        for (int y = 0; y < ySize; y++) {
            JsonArray layerArray = patternArray.get(y).getAsJsonArray();
            validateLayerSize(y, layerArray, zSize);

            for (int z = 0; z < zSize; z++) {
                String row = layerArray.get(z).getAsString();
                validateRowLength(y, z, row, xSize);

                boolean[] parsedRow = parseRow(row);
                System.arraycopy(parsedRow, 0, pattern[z][y], 0, xSize);
            }
        }

        return pattern;
    }

    private static boolean[] parseRow(String row) {
        boolean[] result = new boolean[row.length()];
        for (int i = 0; i < row.length(); i++) {
            result[i] = row.charAt(i) == '#';
        }
        return result;
    }

    private static boolean[][][] validatePattern(boolean[][][] pattern) {
        if (pattern.length == 0 || pattern[0].length == 0 || pattern[0][0].length == 0) {
            throw new IllegalArgumentException("Pattern must not be empty");
        }

        int expectedRows = pattern[0].length;
        for (int z = 1; z < pattern.length; z++) {
            if (pattern[z].length != expectedRows) {
                throw new IllegalArgumentException("All layers must have the same number of rows");
            }
        }

        int expectedCols = pattern[0][0].length;
        for (int z = 0; z < pattern.length; z++) {
            for (int y = 0; y < pattern[z].length; y++) {
                if (pattern[z][y].length != expectedCols) {
                    throw new IllegalArgumentException("All rows must have the same number of columns");
                }
            }
        }

        return pattern;
    }

    private static void validateRowLength(int y, String row, int expectedLength) {
        if (row.length() != expectedLength) {
            throw new IllegalArgumentException(String.format(
                    "Row %d must have %d columns, but had %d", y, expectedLength, row.length()));
        }
    }

    private static void validateRowLength(int y, int z, String row, int expectedLength) {
        if (row.length() != expectedLength) {
            throw new IllegalArgumentException(String.format(
                    "Layer %d, row %d must have %d columns, but had %d", y, z, expectedLength, row.length()));
        }
    }

    private static void validateLayerSize(int y, JsonArray layerArray, int expectedSize) {
        if (layerArray.size() != expectedSize) {
            throw new IllegalArgumentException(String.format(
                    "Layer %d must have %d rows, but had %d", y, expectedSize, layerArray.size()));
        }
    }

    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeUtf(this.name);
        buf.writeUtf(this.type);
        buf.writeItem(this.result);
        buf.writeVarInt(this.sizeZ);
        buf.writeVarInt(this.sizeY);
        buf.writeVarInt(this.sizeX);

        for (int z = 0; z < this.sizeZ; z++) {
            for (int y = 0; y < this.sizeY; y++) {
                for (int x = 0; x < this.sizeX; x++) {
                    buf.writeBoolean(this.pattern[z][y][x]);
                }
            }
        }
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
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ && pattern[z][y][x];
    }

    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }
}