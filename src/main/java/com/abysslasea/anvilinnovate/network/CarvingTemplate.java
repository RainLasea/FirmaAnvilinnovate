package com.abysslasea.anvilinnovate.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class CarvingTemplate {
    private final ResourceLocation id;
    private final String name;  // 新增name字段
    private final ItemStack result;
    private final boolean[][] pattern;

    public CarvingTemplate(ResourceLocation id, String name, ItemStack result, boolean[][] pattern) {
        this.id = id;
        this.name = name;
        this.result = result;
        this.pattern = validatePattern(pattern);
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

        ItemStack result = parseOutputItem(json);
        boolean[][] pattern = parsePattern(json.getAsJsonArray("pattern"));
        return new CarvingTemplate(id, name, result, pattern);
    }

    private static ItemStack parseOutputItem(JsonObject json) {
        ResourceLocation itemId = new ResourceLocation(json.get("output").getAsString());
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Unknown item: " + itemId);
        }
        return new ItemStack(item);
    }

    private static boolean[][] parsePattern(JsonArray patternArray) {
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
                    String.format("Row %d must be 12 characters (got '%s')", y+1, row)
            );
        }
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putString("name", name);  // 序列化时可选写入
        tag.put("result", result.save(new CompoundTag()));

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
        return new CarvingTemplate(id, name, result, pattern);
    }

    public ResourceLocation getId() { return id; }
    public String getName() { return name; }
    public ItemStack getResult() { return result.copy(); }

    public boolean shouldCarve(int x, int y) {
        return x >= 0 && x < 12 && y >= 0 && y < 12 && pattern[y][x];
    }
}