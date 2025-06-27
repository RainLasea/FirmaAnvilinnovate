package com.abysslasea.anvilinnovate.block;

import com.abysslasea.anvilinnovate.network.NetworkHandler;
import com.abysslasea.anvilinnovate.network.OpenTemplateScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "anvilinnovate", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandlers {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return; // 只在服务端处理

        Player player = event.getEntity();
        ItemStack mainHandStack = player.getMainHandItem();
        BlockPos clickedPos = event.getPos();
        BlockPos slabPos = clickedPos.above();

        if (mainHandStack.getItem() == Items.FLINT) {
            if (level.getBlockState(slabPos).isAir()) {
                level.setBlock(slabPos, ModBlocks.CARVING_SLAB.get().defaultBlockState(), 3);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);

                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.sendToClient(serverPlayer, new OpenTemplateScreenPacket(slabPos));
                }
            } else if (level.getBlockState(slabPos).getBlock() == ModBlocks.CARVING_SLAB.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);

                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.sendToClient(serverPlayer, new OpenTemplateScreenPacket(slabPos));
                }
            }
        }
    }
}
