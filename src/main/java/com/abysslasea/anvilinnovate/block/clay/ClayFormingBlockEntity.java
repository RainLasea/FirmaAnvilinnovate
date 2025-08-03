package com.abysslasea.anvilinnovate.block.clay;

import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.template.CarvingTemplate;
import com.abysslasea.anvilinnovate.template.CarvingTemplateManager;
import com.abysslasea.anvilinnovate.template.packet.SetTemplatePacket;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public class ClayFormingBlockEntity extends BlockEntity implements SetTemplatePacket.TemplateAssignable {

    public static final int SIZE = 14;
    private final BitSet voxels = new BitSet(SIZE * SIZE * SIZE);

    @Nullable
    private ResourceLocation templateId;

    @Nullable
    private CarvingTemplate template;

    private int minValidY = 0, maxValidY = SIZE - 1;

    private boolean lookedAt = false;

    public ClayFormingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CLAY_FORMING_BE.get(), pos, state);
    }

    @Override
    public void setTemplateId(@NotNull ResourceLocation id) {
        this.templateId = id;
        this.template = CarvingTemplateManager.getTemplate(id);
        recalcRange();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Nullable
    public ResourceLocation getTemplateId() {
        return templateId;
    }

    private int idx(int x, int y, int z) {
        return x + SIZE * (y + SIZE * z);
    }

    private void recalcRange() {
        if (template == null) {
            minValidY = 0;
            maxValidY = SIZE - 1;
            return;
        }
        boolean[][][] pat = template.getPattern();
        minValidY = SIZE;
        maxValidY = -1;
        for (int y = 0; y < SIZE; y++) {
            outer:
            for (int z = 0; z < SIZE; z++) {
                for (int x = 0; x < SIZE; x++) {
                    if (pat[z][y][x]) {
                        minValidY = Math.min(minValidY, y);
                        maxValidY = Math.max(maxValidY, y);
                        break outer;
                    }
                }
            }
        }
        if (minValidY > maxValidY) {
            minValidY = 0;
            maxValidY = 0;
        }
    }

    public boolean tryForm(int x, int y, int z) {
        if (template == null) return false;
        if (x < 0 || x >= SIZE || y < minValidY || y > maxValidY || z < 0 || z >= SIZE) return false;

        boolean[][][] pattern = template.getPattern();
        if (!pattern[z][y][x]) return false;

        int topComplete = getMaxCompleteLayer();
        if (y > topComplete + 1) return false;

        for (int ly = minValidY; ly < y; ly++) {
            for (int zz = 0; zz < SIZE; zz++) {
                for (int xx = 0; xx < SIZE; xx++) {
                    if (pattern[zz][ly][xx] && !voxels.get(idx(xx, ly, zz))) return false;
                }
            }
        }

        int i = idx(x, y, z);
        if (!voxels.get(i)) {
            voxels.set(i);
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return false;
    }

    public int getMaxCompleteLayer() {
        if (template == null) return minValidY - 1;
        boolean[][][] pattern = template.getPattern();
        for (int y = maxValidY; y >= minValidY; y--) {
            boolean full = true;
            outer:
            for (int z = 0; z < SIZE; z++) {
                for (int x = 0; x < SIZE; x++) {
                    if (pattern[z][y][x] && !voxels.get(idx(x, y, z))) {
                        full = false;
                        break outer;
                    }
                }
            }
            if (full) return y;
        }
        return minValidY - 1;
    }

    public int getCurrentHeight() {
        int mc = getMaxCompleteLayer();
        int h = mc < minValidY ? 1 : mc + 1;
        return Math.min(h, maxValidY + 1);
    }

    public boolean isFinished() {
        if (template == null) return false;
        boolean[][][] pattern = template.getPattern();
        for (int z = 0; z < SIZE; z++) {
            for (int y = minValidY; y <= maxValidY; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (pattern[z][y][x] && !voxels.get(idx(x, y, z))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByteArray("Voxels", voxels.toByteArray());
        if (templateId != null) tag.putString("TemplateId", templateId.toString());
        tag.putInt("MinY", minValidY);
        tag.putInt("MaxY", maxValidY);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        voxels.clear();
        voxels.or(BitSet.valueOf(tag.getByteArray("Voxels")));

        if (tag.contains("TemplateId")) {
            templateId = new ResourceLocation(tag.getString("TemplateId"));
            template = null;
        } else {
            templateId = null;
            template = null;
        }

        minValidY = tag.getInt("MinY");
        maxValidY = tag.getInt("MaxY");
    }

    public boolean[][][] getTemplateData() {
        return template == null ? null : template.getPattern();
    }

    public boolean[][][] getVoxelStates() {
        boolean[][][] states = new boolean[SIZE][SIZE][SIZE];
        for (int z = 0; z < SIZE; z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    states[z][y][x] = voxels.get(idx(x, y, z));
                }
            }
        }
        return states;
    }

    public int getMinValidY() {
        return minValidY;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    public boolean isLookedAt() {
        return lookedAt;
    }

    public void setLookedAt(boolean lookedAt) {
        this.lookedAt = lookedAt;
    }
}