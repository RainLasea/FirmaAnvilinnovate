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
    private static final int SIZE = 10;

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
        if (pattern.length != SIZE) {
            throw new IllegalArgumentException("Pattern must have " + SIZE + " rows");
        }
        boolean[][] validated = new boolean[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            if (pattern[y].length != SIZE) {
                throw new IllegalArgumentException("Row " + y + " must have " + SIZE + " columns");
            }
            System.arraycopy(pattern[y], 0, validated[y], 0, SIZE);
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
        if (patternArray.size() != SIZE) {
            throw new IllegalArgumentException("Pattern must have " + SIZE + " rows");
        }

        boolean[][] pattern = new boolean[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            String row = patternArray.get(y).getAsString();
            validateRow(y, row);
            for (int x = 0; x < SIZE; x++) {
                pattern[y][x] = (row.charAt(x) == '#');
            }
        }
        return pattern;
    }

    private static void validateRow(int y, String row) {
        if (row.length() != SIZE) {
            throw new IllegalArgumentException(
                    String.format("Row %d must be %d characters (got '%s')", y + 1, SIZE, row)
            );
        }
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
        boolean[][] pattern = new boolean[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
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

    public Component getDisplayName() {
        return Component.translatable(name);
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public boolean shouldCarve(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE && !pattern[y][x];
    }
}
