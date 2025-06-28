package com.abysslasea.anvilinnovate.block;

import com.abysslasea.anvilinnovate.NetworkHandler;
import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.block.flint.ChiseledFlintSlabBlockEntity;
import com.abysslasea.anvilinnovate.template.OpenTemplateScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "anvilinnovate", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandlers {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        BlockPos clickedPos = event.getPos();
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() != Items.FLINT) return;

        BlockState clickedBlockState = level.getBlockState(clickedPos);

        if (clickedBlockState.getBlock() == ModBlocks.CARVING_SLAB.get()) {
            BlockEntity be = level.getBlockEntity(clickedPos);
            if (be instanceof ChiseledFlintSlabBlockEntity slabBE) {
                Vec3 hitVec = event.getHitVec().getLocation();
                Vec3 relativePos = hitVec.subtract(clickedPos.getX(), clickedPos.getY(), clickedPos.getZ());

                int gridX = (int) (relativePos.x * 12);
                int gridY = (int) (relativePos.z * 12);

                gridX = Math.min(Math.max(gridX, 0), 11);
                gridY = Math.min(Math.max(gridY, 0), 11);

                boolean carved = slabBE.tryCarve(gridX, gridY);
                if (carved) {
                    level.playSound(null, clickedPos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.5f, 1.0f);
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToClient(serverPlayer, new OpenTemplateScreenPacket(clickedPos));
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
