package com.abysslasea.anvilinnovate;

import com.abysslasea.anvilinnovate.template.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("anvilinnovate", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SetTemplatePacket.class, SetTemplatePacket::encode, SetTemplatePacket::decode, SetTemplatePacket::handle);
        CHANNEL.registerMessage(id++, OpenTemplateScreenPacket.class, OpenTemplateScreenPacket::encode, OpenTemplateScreenPacket::decode, OpenTemplateScreenPacket::handle);
        CHANNEL.registerMessage(id++, SyncTemplatesPacket.class, SyncTemplatesPacket::encode, SyncTemplatesPacket::decode, SyncTemplatesPacket::handle);
    }

    public static void sendToClient(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
