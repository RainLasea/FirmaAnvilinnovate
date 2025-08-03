package com.abysslasea.anvilinnovate.template.packet;

import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.template.CarvingTemplate;
import com.abysslasea.anvilinnovate.template.CarvingTemplateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SetTemplatePacket {

    private final BlockPos pos;
    private final ResourceLocation templateId;

    public SetTemplatePacket(BlockPos pos, ResourceLocation templateId) {
        this.pos = pos;
        this.templateId = templateId;
    }

    public static void encode(@NotNull SetTemplatePacket pkt, @NotNull net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeResourceLocation(pkt.templateId);
    }

    public static SetTemplatePacket decode(@NotNull net.minecraft.network.FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceLocation templateId = buf.readResourceLocation();
        return new SetTemplatePacket(pos, templateId);
    }

    public static void handle(@NotNull SetTemplatePacket pkt, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Level world = player.getCommandSenderWorld();
            CarvingTemplate template = CarvingTemplateManager.getTemplate(pkt.templateId);
            if (template == null) return;

            if ("clay_template".equals(template.getType())) {
                CLAY_PLACE_STRATEGY.placeTemplateBlock(world, player, pkt.pos, pkt.templateId);
            } else {
                FLINT_PLACE_STRATEGY.placeTemplateBlock(world, player, pkt.pos, pkt.templateId);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @FunctionalInterface
    public interface TemplatePlaceStrategy {
        void placeTemplateBlock(@NotNull Level world,
                                @NotNull ServerPlayer player,
                                @NotNull BlockPos pos,
                                @NotNull ResourceLocation templateId);
    }

    public interface TemplateAssignable {
        void setTemplateId(@NotNull ResourceLocation id);
    }

    public static TemplatePlaceStrategy FLINT_PLACE_STRATEGY = (world, player, pos, templateId) -> {
        Block blockToPlace = ModBlocks.CARVING_SLAB.get();
        placeTemplateBlock(world, player, pos, blockToPlace, templateId);
    };

    public static TemplatePlaceStrategy CLAY_PLACE_STRATEGY = (world, player, pos, templateId) -> {
        Block blockToPlace = ModBlocks.CLAY_FORMING.get();
        placeTemplateBlock(world, player, pos, blockToPlace, templateId);
    };

    public static boolean placeTemplateBlock(
            @NotNull Level world,
            @NotNull ServerPlayer player,
            @NotNull BlockPos pos,
            @NotNull Block blockToPlace,
            @NotNull ResourceLocation templateId
    ) {
        BlockPos placePos = pos.above();

        if (!world.isEmptyBlock(placePos)) return false;

        boolean placed = world.setBlock(placePos, blockToPlace.defaultBlockState(), 3);
        if (!placed) return false;

        BlockEntity be = world.getBlockEntity(placePos);
        if (be instanceof TemplateAssignable assignable) {
            assignable.setTemplateId(templateId);
            return true;
        }

        return false;
    }
}