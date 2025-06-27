package com.abysslasea.anvilinnovate.network;

import com.abysslasea.anvilinnovate.block.flint.CarvingFlintSlabBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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

    public static void encode(SetTemplatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.templateId);
    }

    public static SetTemplatePacket decode(FriendlyByteBuf buffer) {
        return new SetTemplatePacket(
                buffer.readBlockPos(),
                buffer.readResourceLocation()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Level world = player.getCommandSenderWorld();
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof CarvingFlintSlabBlockEntity slab) {
                    slab.setTemplate(templateId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}