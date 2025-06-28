package com.abysslasea.anvilinnovate.template;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CarvingTemplate {
    private final ResourceLocation id;
    private final String name;
    private final boolean[][] pattern;
    private final ItemStack result;

    public CarvingTemplate(ResourceLocation id, String name, boolean[][] pattern, ItemStack result) {
        this.id = id;
        this.name = name;
        this.pattern = validatePattern(pattern);
        this.result = result;
    }

    public CarvingTemplate(ResourceLocation id, String name, boolean[][] pattern) {
        this(id, name, pattern, ItemStack.EMPTY);
    }

    private boolean[][] validatePattern(boolean[][] pattern) {
        if (pattern.length != 12) {
            throw new IllegalArgumentException("Pattern must have 12 rows");
        }
        boolean[][] validated = new boolean[12][12];
        for (int y = 0; y < 12; y++) {
            if (pattern[y].length != 12) {
                throw new IllegalArgumentException("Row " + y + " must have 12 columns");
            }
            System.arraycopy(pattern[y], 0, validated[y], 0, 12);
        }
        return validated;
    }

    public static CarvingTemplate fromJson(ResourceLocation id, JsonObject json) {
        String name = json.has("name") ? json.get("name").getAsString() : id.getPath();
        boolean[][] pattern = parsePattern(json.getAsJsonArray("pattern"));

        ItemStack result = ItemStack.EMPTY;
        if (json.has("output")) {
            String resultId = json.get("output").getAsString();
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(resultId));
            if (item != null) {
                result = new ItemStack(item);
            }
        }

        return new CarvingTemplate(id, name, pattern, result);
    }

    private static boolean[][] parsePattern(JsonArray patternArray) {
        if (patternArray.size() != 12) {
            throw new IllegalArgumentException("Pattern must have 12 rows");
        }

        boolean[][] pattern = new boolean[12][12];
        for (int y = 0; y < 12; y++) {
            String row = patternArray.get(y).getAsString();
            validateRow(y, row);
            for (int x = 0; x < 12; x++) {
                pattern[y][x] = (row.charAt(x) == '#');
            }
        }
        return pattern;
    }

    private static void validateRow(int y, String row) {
        if (row.length() != 12) {
            throw new IllegalArgumentException(
                    String.format("Row %d must be 12 characters (got '%s')", y + 1, row)
            );
        }
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putString("name", name);
        tag.putString("result", BuiltInRegistries.ITEM.getKey(result.getItem()).toString());

        byte[] patternData = new byte[12 * 12];
        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                patternData[y * 12 + x] = (byte) (pattern[y][x] ? 1 : 0);
            }
        }
        tag.putByteArray("pattern", patternData);

        return tag;
    }

    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeUtf(name);
        buf.writeItem(result);
        for (boolean[] row : pattern) {
            for (boolean cell : row) {
                buf.writeBoolean(cell);
            }
        }
    }

    public static CarvingTemplate readFromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        String name = buf.readUtf(32767);
        ItemStack result = buf.readItem();
        boolean[][] pattern = new boolean[12][12];
        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                pattern[y][x] = buf.readBoolean();
            }
        }
        return new CarvingTemplate(id, name, pattern, result);
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public boolean shouldCarve(int x, int y) {
        return x >= 0 && x < 12 && y >= 0 && y < 12 && pattern[y][x];
    }
}