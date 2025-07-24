package com.abysslasea.anvilinnovate.block.flint;

import com.abysslasea.anvilinnovate.block.FloatingUIRenderer;
import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.template.CarvingTemplate;
import com.abysslasea.anvilinnovate.template.CarvingTemplateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

public class ChiseledFlintSlabBlockEntity extends BlockEntity implements FloatingUIRenderer.FloatingUIRenderable {

    private ResourceLocation templateId;
    private final boolean[][] carved = new boolean[10][10];

    private boolean lookedAt = false;

    public ChiseledFlintSlabBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CARVING_SLAB_BE.get(), pos, state);
    }

    public void setTemplateId(ResourceLocation id) {
        this.templateId = id;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ResourceLocation getTemplateId() {
        return templateId;
    }

    public boolean isCarved(int x, int y) {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) return false;
        return carved[y][x];
    }

    public void setCarved(int x, int y, boolean carvedState) {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) return;
        carved[y][x] = carvedState;
        setChanged();
    }

    public boolean tryCarve(int x, int y) {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) {
            return false;
        }

        if (carved[y][x]) {
            return false;
        }

        CarvingTemplate template = CarvingTemplateManager.getTemplate(templateId);
        if (template == null) {
            return false;
        }

        if (template.shouldCarve(x, y)) {
            carved[y][x] = true;
            setChanged();

            if (level != null && !level.isClientSide) {
                checkAndBreakIsolatedRegions(template);

                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                if (isFinished(template)) {
                    finishCarving(template);
                }
            }
            return true;
        }
        return false;
    }

    private void checkAndBreakIsolatedRegions(CarvingTemplate template) {
        boolean[][] visited = new boolean[10][10];
        List<List<int[]>> regions = new ArrayList<>();

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (!carved[y][x] && template.shouldCarve(x, y) && !visited[y][x]) {
                    List<int[]> region = new ArrayList<>();
                    findConnectedRegion(template, x, y, visited, region);
                    regions.add(region);
                }
            }
        }

        for (List<int[]> region : regions) {
            if (isIsolatedFromKept(region, template)) {
                for (int[] pos : region) {
                    int px = pos[0], py = pos[1];
                    carved[py][px] = true;
                }
                setChanged();
            }
        }
    }

    private void findConnectedRegion(CarvingTemplate template, int x, int y,
                                     boolean[][] visited, List<int[]> region) {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) return;
        if (visited[y][x]) return;
        if (carved[y][x] || !template.shouldCarve(x, y)) return;

        visited[y][x] = true;
        region.add(new int[]{x, y});

        findConnectedRegion(template, x+1, y, visited, region);
        findConnectedRegion(template, x-1, y, visited, region);
        findConnectedRegion(template, x, y+1, visited, region);
        findConnectedRegion(template, x, y-1, visited, region);
    }

    private boolean isIsolatedFromKept(List<int[]> region, CarvingTemplate template) {
        for (int[] pos : region) {
            int x = pos[0], y = pos[1];
            if ((x > 0 && !template.shouldCarve(x-1, y)) ||
                    (x < 9 && !template.shouldCarve(x+1, y)) ||
                    (y > 0 && !template.shouldCarve(x, y-1)) ||
                    (y < 9 && !template.shouldCarve(x, y+1))) {
                return false;
            }
        }
        return true;
    }

    private boolean isFinished(CarvingTemplate template) {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (template.shouldCarve(x, y) && !carved[y][x]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void finishCarving(CarvingTemplate template) {
        if (!(level instanceof ServerLevel)) return;

        ItemStack output = template.getResult();
        if (output.isEmpty()) return;

        level.removeBlock(worldPosition, false);
        ItemEntity itemEntity = new ItemEntity(level,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5,
                output.copy());
        level.addFreshEntity(itemEntity);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (templateId != null) {
            tag.putString("template", templateId.toString());
        }

        CompoundTag carvedTag = new CompoundTag();
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                carvedTag.putBoolean(y + "_" + x, carved[y][x]);
            }
        }
        tag.put("carved", carvedTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("template")) {
            String id = tag.getString("template");
            if (CarvingTemplateManager.getTemplate(new ResourceLocation(id)) != null) {
                this.templateId = new ResourceLocation(id);
            } else {
                this.templateId = null;
            }
        } else {
            this.templateId = null;
        }

        if (tag.contains("carved")) {
            CompoundTag carvedTag = tag.getCompound("carved");
            for (int y = 0; y < 10; y++) {
                for (int x = 0; x < 10; x++) {
                    String key = y + "_" + x;
                    this.carved[y][x] = carvedTag.getBoolean(key);
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (templateId != null) {
            tag.putString("template", templateId.toString());
        }

        CompoundTag carvedTag = new CompoundTag();
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                carvedTag.putBoolean(y + "_" + x, carved[y][x]);
            }
        }
        tag.put("carved", carvedTag);

        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag == null) return;

        if (tag.contains("template")) {
            this.templateId = new ResourceLocation(tag.getString("template"));
        } else {
            this.templateId = null;
        }

        if (tag.contains("carved")) {
            CompoundTag carvedTag = tag.getCompound("carved");
            for (int y = 0; y < 10; y++) {
                for (int x = 0; x < 10; x++) {
                    String key = y + "_" + x;
                    this.carved[y][x] = carvedTag.getBoolean(key);
                }
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public boolean shouldRenderFloatingUI() {
        return lookedAt;
    }

    @Override
    public void setLookedAt(boolean lookedAt) {
        this.lookedAt = lookedAt;
    }
}