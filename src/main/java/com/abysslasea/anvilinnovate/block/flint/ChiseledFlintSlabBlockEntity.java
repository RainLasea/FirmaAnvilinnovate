package com.abysslasea.anvilinnovate.block.flint;

import com.abysslasea.anvilinnovate.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ChiseledFlintSlabBlockEntity extends BlockEntity {

    private ResourceLocation templateId;
    private final boolean[][] carved = new boolean[12][12];

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
        return carved[y][x];
    }

    public void setCarved(int x, int y, boolean carvedState) {
        carved[y][x] = carvedState;
        setChanged();
    }

    public boolean tryCarve(int x, int y) {
        if (x < 0 || x >= 12 || y < 0 || y >= 12 || carved[y][x]) {
            return false;
        }
        carved[y][x] = true;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (templateId != null) {
            tag.putString("template", templateId.toString());
        }

        CompoundTag carvedTag = new CompoundTag();
        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                carvedTag.putBoolean(y + "_" + x, carved[y][x]);
            }
        }
        tag.put("carved", carvedTag);
    }


    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("template")) {
            this.templateId = new ResourceLocation(tag.getString("template"));
        } else {
            this.templateId = null;
        }

        if (tag.contains("carved")) {
            CompoundTag carvedTag = tag.getCompound("carved");
            for (int y = 0; y < 12; y++) {
                for (int x = 0; x < 12; x++) {
                    String key = y + "_" + x;
                    this.carved[y][x] = carvedTag.getBoolean(key);
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        if (templateId != null) {
            tag.putString("template", templateId.toString());
        }

        CompoundTag carvedTag = new CompoundTag();
        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
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
            for (int y = 0; y < 12; y++) {
                for (int x = 0; x < 12; x++) {
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
}
