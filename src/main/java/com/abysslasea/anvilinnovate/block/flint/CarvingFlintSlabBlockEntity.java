package com.abysslasea.anvilinnovate.block.flint;

import com.abysslasea.anvilinnovate.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.BitSet;

public class CarvingFlintSlabBlockEntity extends BlockEntity {
    private ResourceLocation templateId;
    private final BitSet carvedGrid = new BitSet(144); // 12x12网格

    public CarvingFlintSlabBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CARVING_SLAB_BE.get(), pos, state);
    }

    public ResourceLocation getTemplateId() {
        return templateId;
    }

    public void setTemplate(ResourceLocation id) {
        this.templateId = id;
        setChanged();
    }

    public boolean tryCarve(int x, int y) {
        if (x < 0 || x >= 12 || y < 0 || y >= 12 || carvedGrid.get(y * 12 + x)) {
            return false;
        }

        carvedGrid.set(y * 12 + x, true);
        setChanged();
        return true;
    }

    public boolean isCarved(int x, int y) {
        return carvedGrid.get(y * 12 + x);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (templateId != null) {
            tag.putString("TemplateId", templateId.toString());
        }
        tag.putByteArray("CarvedGrid", carvedGrid.toByteArray());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TemplateId")) {
            templateId = new ResourceLocation(tag.getString("TemplateId"));
        }
        if (tag.contains("CarvedGrid")) {
            carvedGrid.clear();
            carvedGrid.or(BitSet.valueOf(tag.getByteArray("CarvedGrid")));
        }
    }
}