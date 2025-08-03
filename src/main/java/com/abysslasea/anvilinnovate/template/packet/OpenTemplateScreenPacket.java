package com.abysslasea.anvilinnovate.template.packet;

import com.abysslasea.anvilinnovate.client.screen.TemplateSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenTemplateScreenPacket {
    private final BlockPos pos;
    private final String templateType;

    public OpenTemplateScreenPacket(BlockPos pos, String templateType) {
        this.pos = pos;
        this.templateType = templateType;
    }

    public static void encode(OpenTemplateScreenPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeUtf(pkt.templateType);
    }

    public static OpenTemplateScreenPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String templateType = buf.readUtf();
        return new OpenTemplateScreenPacket(pos, templateType);
    }

    public static void handle(OpenTemplateScreenPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.setScreen(new TemplateSelectionScreen(
                        pkt.pos,
                        Component.translatable("gui.anvilinnovate.template_selection"),
                        pkt.templateType
                ));
            }
        });
        ctx.get().setPacketHandled(true);
    }

}