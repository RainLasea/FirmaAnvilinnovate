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

    private int currentHeight = 0;
    private int minValidY = 0;

    public ClayFormingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CLAY_FORMING_BE.get(), pos, state);
    }

    private int idx(int x, int y, int z) {
        return x + y * SIZE + z * SIZE * SIZE;
    }

    public boolean tryForm(int x, int y, int z) {
        if (level == null || level.isClientSide() || template == null) return false;

        if (!template.shouldCarve(x, y, z)) {
            return false;
        }

        if (voxels.get(idx(x, y, z))) {
            return false;
        }

        if (y > currentHeight) {
            return false;
        }

        voxels.set(idx(x, y, z));
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        setChanged();

        checkLayerCompletion(y);

        checkTemplateCompletion();

        return true;
    }

    private void checkLayerCompletion(int layerY) {
        boolean layerComplete = true;
        for (int z = 0; z < SIZE; z++) {
            for (int x = 0; x < SIZE; x++) {
                if (template.shouldCarve(x, layerY, z) && !voxels.get(idx(x, layerY, z))) {
                    layerComplete = false;
                    break;
                }
            }
            if (!layerComplete) break;
        }

        if (layerComplete) {
            currentHeight = Math.max(currentHeight, layerY + 1);
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            setChanged();
        }
    }

    private void checkTemplateCompletion() {
        boolean templateComplete = true;
        for (int z = 0; z < SIZE; z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (template.shouldCarve(x, y, z) && !voxels.get(idx(x, y, z))) {
                        templateComplete = false;
                        break;
                    }
                }
                if (!templateComplete) break;
            }
            if (!templateComplete) break;
        }

        if (templateComplete) {
            if (level instanceof ServerLevel serverLevel) {
                ItemStack result = template.getResult();
                ItemEntity itemEntity = new ItemEntity(serverLevel,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                        result);
                serverLevel.addFreshEntity(itemEntity);
            }
            level.removeBlock(worldPosition, false);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (templateId != null) {
            tag.putString("TemplateId", templateId.toString());
        }
        tag.putByteArray("Voxels", voxels.toByteArray());
        tag.putInt("CurrentHeight", currentHeight);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TemplateId")) {
            this.templateId = new ResourceLocation(tag.getString("TemplateId"));
        }
        if (tag.contains("Voxels")) {
            this.voxels.clear();
            this.voxels.or(BitSet.valueOf(tag.getByteArray("Voxels")));
        }
        this.currentHeight = tag.getInt("CurrentHeight");
        if (level != null && level.isClientSide() && this.templateId != null) {
            this.template = CarvingTemplateManager.getTemplate(this.templateId);
            if (this.template != null) {
                recalcRange();
            }
        }
    }

    @Override
    public void setTemplateId(@NotNull ResourceLocation id) {
        this.templateId = id;
        if (level != null && !level.isClientSide()) {
            this.template = CarvingTemplateManager.getTemplate(id);
            if (this.template != null) {
                recalcRange();
            }
            setChanged();
        }
    }

    public ResourceLocation getTemplateId() {
        return templateId;
    }

    public CarvingTemplate getTemplate() {
        return template;
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

        if (level != null && level.isClientSide()) {
            if (this.templateId != null) {
                this.template = CarvingTemplateManager.getTemplate(this.templateId);
                if (this.template != null) {
                    recalcRange();
                }
            }
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }

    public int getCurrentHeight() {
        return currentHeight;
    }

    private void recalcRange() {
        minValidY = SIZE;
        if (template == null) {
            minValidY = 0;
            return;
        }
        for (int z = 0; z < SIZE; z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (template.shouldCarve(x, y, z)) {
                        minValidY = Math.min(minValidY, y);
                        return;
                    }
                }
            }
        }
        minValidY = 0;
    }
}