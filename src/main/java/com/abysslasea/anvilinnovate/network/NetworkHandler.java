package com.abysslasea.anvilinnovate.network;

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
    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                SetTemplatePacket.class,
                SetTemplatePacket::encode,
                SetTemplatePacket::decode,
                SetTemplatePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                OpenTemplateScreenPacket.class,
                OpenTemplateScreenPacket::encode,
                OpenTemplateScreenPacket::decode,
                OpenTemplateScreenPacket::handle
        );
    }

    public static void sendToClient(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(SetTemplatePacket packet) {
        CHANNEL.sendToServer(packet);
    }
}
