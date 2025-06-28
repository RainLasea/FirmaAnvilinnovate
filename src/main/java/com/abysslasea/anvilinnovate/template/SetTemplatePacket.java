package com.abysslasea.anvilinnovate.template;

import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.block.flint.ChiseledFlintSlabBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetTemplatePacket {

    private final BlockPos pos;
    private final ResourceLocation templateId;

    public SetTemplatePacket(BlockPos pos, ResourceLocation templateId) {
        this.pos = pos;
        this.templateId = templateId;
    }

    public static void encode(SetTemplatePacket pkt, net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeResourceLocation(pkt.templateId);
    }

    public static SetTemplatePacket decode(net.minecraft.network.FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceLocation templateId = buf.readResourceLocation();
        return new SetTemplatePacket(pos, templateId);
    }

    public static void handle(SetTemplatePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Level world = player.getCommandSenderWorld();

            BlockPos placePos = pkt.pos.above();  // 放置在上方一格

            System.out.println("[SetTemplatePacket] Received packet.");
            System.out.println("[SetTemplatePacket] Player position: " + player.blockPosition());
            System.out.println("[SetTemplatePacket] Original pos: " + pkt.pos);
            System.out.println("[SetTemplatePacket] Place block at pos (above): " + placePos);
            System.out.println("[SetTemplatePacket] Template ID: " + pkt.templateId);

            if (!world.isEmptyBlock(placePos)) {
                System.out.println("[SetTemplatePacket] Target placePos is not empty: " + world.getBlockState(placePos));
                System.out.println("[SetTemplatePacket] Aborting block placement.");
                return;
            }

            boolean placed = world.setBlock(placePos, ModBlocks.CARVING_SLAB.get().defaultBlockState(), 3);
            System.out.println("[SetTemplatePacket] Attempted to place block: " + (placed ? "SUCCESS" : "FAILURE"));

            BlockEntity be = world.getBlockEntity(placePos);
            if (be instanceof ChiseledFlintSlabBlockEntity slabBE) {
                slabBE.setTemplateId(pkt.templateId);
                System.out.println("[SetTemplatePacket] Set template ID on BlockEntity.");
            } else {
                System.out.println("[SetTemplatePacket] No BlockEntity found at placePos after placement.");
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
