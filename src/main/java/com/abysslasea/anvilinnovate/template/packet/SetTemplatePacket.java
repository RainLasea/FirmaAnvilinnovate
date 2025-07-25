package com.abysslasea.anvilinnovate.template.packet;

import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.block.flint.ChiseledFlintSlabBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetTemplatePacket {

    private final BlockPos pos;
    private final ResourceLocation templateId;

    private static final TagKey<Item> CARVING_FLINTS = ItemTags.create(new ResourceLocation("anvilinnovate", "carving_flints"));

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
            BlockPos placePos = pkt.pos.above();

            if (!world.isEmptyBlock(placePos)) return;

            if (!player.isCreative()) {
                boolean consumed = false;

                ItemStack mainHand = player.getMainHandItem();
                if (mainHand.is(CARVING_FLINTS)) {
                    mainHand.shrink(1);
                    consumed = true;
                }

                if (!consumed) {
                    ItemStack offHand = player.getOffhandItem();
                    if (offHand.is(CARVING_FLINTS)) {
                        offHand.shrink(1);
                        consumed = true;
                    }
                }

                if (!consumed) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack.is(CARVING_FLINTS)) {
                            stack.shrink(1);
                            consumed = true;
                            break;
                        }
                    }
                }

                if (!consumed) {
                    return;
                }
            }

            boolean placed = world.setBlock(placePos, ModBlocks.CARVING_SLAB.get().defaultBlockState(), 3);
            if (!placed) return;

            BlockEntity be = world.getBlockEntity(placePos);
            if (be instanceof ChiseledFlintSlabBlockEntity slabBE) {
                slabBE.setTemplateId(pkt.templateId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}