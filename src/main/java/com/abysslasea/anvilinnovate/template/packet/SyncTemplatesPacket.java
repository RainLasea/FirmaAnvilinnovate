package com.abysslasea.anvilinnovate.template.packet;

import com.abysslasea.anvilinnovate.template.CarvingTemplate;
import com.abysslasea.anvilinnovate.template.CarvingTemplateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncTemplatesPacket {
    private final List<CarvingTemplate> templates;

    public SyncTemplatesPacket(List<CarvingTemplate> templates) {
        this.templates = templates;
    }

    public static void encode(SyncTemplatesPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.templates.size());
        for (CarvingTemplate template : msg.templates) {
            template.writeToNetwork(buf);
        }
    }

    public static SyncTemplatesPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<CarvingTemplate> templates = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            templates.add(CarvingTemplate.readFromNetwork(buf));
        }
        return new SyncTemplatesPacket(templates);
    }

    public static void handle(SyncTemplatesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            CarvingTemplateManager.clearClientTemplates();
            for (CarvingTemplate template : msg.templates) {
                CarvingTemplateManager.registerClientTemplate(template);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}