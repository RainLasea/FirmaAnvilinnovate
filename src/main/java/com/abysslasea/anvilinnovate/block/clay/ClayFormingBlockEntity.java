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

        int relativeHeight = y - minValidY;
        updateCurrentHeight();

        if (relativeHeight > currentHeight) {
            return false;
        }

        voxels.set(idx(x, y, z));
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        setChanged();

        checkLayerCompletion(y);
        checkTemplateCompletion();

        return true;
    }

    private void updateCurrentHeight() {
        if (template == null) return;

        int newCurrentHeight = 0;

        for (int checkY = minValidY; checkY < template.getSizeY(); checkY++) {
            boolean layerComplete = true;
            boolean hasContent = false;

            for (int z = 0; z < SIZE && z < template.getSizeZ(); z++) {
                for (int x = 0; x < SIZE && x < template.getSizeX(); x++) {
                    if (template.shouldCarve(x, checkY, z)) {
                        hasContent = true;
                        if (!voxels.get(idx(x, checkY, z))) {
                            layerComplete = false;
                            break;
                        }
                    }
                }
                if (!layerComplete) break;
            }

            if (hasContent) {
                int relativeHeight = checkY - minValidY;

                if (layerComplete) {
                    newCurrentHeight = relativeHeight + 1;
                } else {
                    break;
                }
            }
        }

        int maxPossibleHeight = template.getSizeY() - 1 - minValidY;
        newCurrentHeight = Math.min(newCurrentHeight, maxPossibleHeight);

        if (newCurrentHeight != currentHeight) {
            currentHeight = newCurrentHeight;
            setChanged();
        }
    }

    private void checkLayerCompletion(int layerY) {
        if (template == null) return;

        boolean layerComplete = true;
        int totalPositions = 0;
        int filledPositions = 0;

        for (int z = 0; z < SIZE && z < template.getSizeZ(); z++) {
            for (int x = 0; x < SIZE && x < template.getSizeX(); x++) {
                if (template.shouldCarve(x, layerY, z)) {
                    totalPositions++;
                    if (voxels.get(idx(x, layerY, z))) {
                        filledPositions++;
                    } else {
                        layerComplete = false;
                    }
                }
            }
        }

        if (layerComplete) {
            int relativeHeight = layerY - minValidY;
            int newCurrentHeight = relativeHeight + 1;
            int maxPossibleHeight = template.getSizeY() - 1 - minValidY;
            newCurrentHeight = Math.min(newCurrentHeight, maxPossibleHeight);
            currentHeight = Math.max(currentHeight, newCurrentHeight);

            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            setChanged();
        }
    }

    private boolean hasContentAtLayer(int y) {
        if (template == null) return false;

        for (int z = 0; z < SIZE && z < template.getSizeZ(); z++) {
            for (int x = 0; x < SIZE && x < template.getSizeX(); x++) {
                if (template.shouldCarve(x, y, z)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkTemplateCompletion() {
        if (template == null) return;

        boolean templateComplete = true;
        for (int z = 0; z < SIZE && z < template.getSizeZ(); z++) {
            for (int y = 0; y < SIZE && y < template.getSizeY(); y++) {
                for (int x = 0; x < SIZE && x < template.getSizeX(); x++) {
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
        tag.putInt("MinValidY", minValidY);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        if (tag.contains("TemplateId")) {
            this.templateId = new ResourceLocation(tag.getString("TemplateId"));

            if (level != null && level.isClientSide()) {
                this.template = null;
                tryLoadTemplate();
            }
        }

        if (tag.contains("Voxels")) {
            this.voxels.clear();
            this.voxels.or(BitSet.valueOf(tag.getByteArray("Voxels")));
        }
        this.currentHeight = tag.getInt("CurrentHeight");
        this.minValidY = tag.getInt("MinValidY");
    }

    private void tryLoadTemplate() {
        if (this.templateId != null && this.template == null && level != null && level.isClientSide()) {
            this.template = CarvingTemplateManager.getTemplate(this.templateId);
            if (this.template != null) {
                recalcRange();
                updateCurrentHeight();
            }
        }
    }

    public void refreshTemplate() {
        if (level != null && level.isClientSide() && templateId != null) {
            this.template = null;
            tryLoadTemplate();
        }
    }

    @Override
    public void setTemplateId(@NotNull ResourceLocation id) {
        this.templateId = id;

        if (level != null && !level.isClientSide()) {
            this.template = CarvingTemplateManager.getTemplate(id);
            if (this.template != null) {
                recalcRange();
                this.currentHeight = 0;
            }
            setChanged();

            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, 3);

        } else if (level != null && level.isClientSide()) {
            this.template = null;
            tryLoadTemplate();
        }
    }

    public ResourceLocation getTemplateId() {
        return templateId;
    }

    public CarvingTemplate getTemplate() {
        if (template == null && templateId != null && level != null && level.isClientSide()) {
            tryLoadTemplate();
        }
        return template;
    }

    public boolean[][][] getTemplateData() {
        CarvingTemplate currentTemplate = getTemplate();
        return currentTemplate == null ? null : currentTemplate.getPattern();
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

    public int getCurrentHeight() {
        return currentHeight;
    }

    public int getCurrentAbsoluteHeight() {
        return minValidY + currentHeight;
    }

    public int getMaxFillableY() {
        if (template == null) return 0;
        updateCurrentHeight();
        return minValidY + currentHeight;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
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
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide() && templateId != null && template == null) {
            tryLoadTemplate();
        }
    }

    private void recalcRange() {
        minValidY = SIZE;
        if (template == null) {
            minValidY = 0;
            return;
        }

        for (int y = 0; y < SIZE && y < template.getSizeY(); y++) {
            if (hasContentAtLayer(y)) {
                minValidY = y;
                break;
            }
        }

        if (minValidY == SIZE) {
            minValidY = 0;
        }
    }
}