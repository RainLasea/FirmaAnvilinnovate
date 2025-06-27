package com.abysslasea.anvilinnovate.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenTemplateScreenPacket {
    private final BlockPos pos;

    public OpenTemplateScreenPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(OpenTemplateScreenPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static OpenTemplateScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenTemplateScreenPacket(buf.readBlockPos());
    }

    public static void handle(OpenTemplateScreenPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.execute(() -> {
                    mc.setScreen(new TemplateSelectionScreen(
                            pkt.pos,
                            Component.translatable("gui.anvilinnovate.carving_selection")
                    ));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
