package com.abysslasea.anvilinnovate.block;

import com.abysslasea.anvilinnovate.NetworkHandler;
import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.block.flint.ChiseledFlintSlabBlockEntity;
import com.abysslasea.anvilinnovate.template.CarvingTemplate;
import com.abysslasea.anvilinnovate.template.CarvingTemplateManager;
import com.abysslasea.anvilinnovate.template.OpenTemplateScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();
        BlockPos clickedPos = event.getPos();
        BlockState clickedBlockState = level.getBlockState(clickedPos);
        Block clickedBlock = clickedBlockState.getBlock();

        boolean holdingFlint = heldItem.getItem() == Items.FLINT;
        boolean emptyHand = heldItem.isEmpty();

        if (holdingFlint) {
            if (clickedBlock == ModBlocks.CARVING_SLAB.get()) {
                BlockEntity be = level.getBlockEntity(clickedPos);
                if (!(be instanceof ChiseledFlintSlabBlockEntity slabBE)) return;

                ResourceLocation templateId = slabBE.getTemplateId();
                if (templateId == null) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        NetworkHandler.sendToClient(serverPlayer, new OpenTemplateScreenPacket(clickedPos));
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }

                Vec3 hitVec = event.getHitVec().getLocation();
                Vec3 relativePos = hitVec.subtract(clickedPos.getX(), clickedPos.getY(), clickedPos.getZ());

                float offset = (1f - 13f / 16f) / 2f;
                float cellSize = (13f / 16f) / 10f;

                float localX = (float) relativePos.x - offset;
                float localZ = (float) relativePos.z - offset;

                int gridX = (int) (localX / cellSize);
                int gridY = (int) (localZ / cellSize);

                if (gridX < 0 || gridX >= 10 || gridY < 0 || gridY >= 10) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.FAIL);
                    return;
                }

                CarvingTemplate template = CarvingTemplateManager.getTemplate(templateId);
                if (template == null) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.FAIL);
                    return;
                }

                boolean shouldCarve = template.shouldCarve(gridX, gridY);

                if (shouldCarve) {
                    boolean carvedNow = slabBE.tryCarve(gridX, gridY);
                    if (carvedNow) {
                        level.playSound(null, clickedPos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.5f, 1.0f);
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        return;
                    }
                } else {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.FAIL);
                    return;
                }
            } else {
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.sendToClient(serverPlayer, new OpenTemplateScreenPacket(clickedPos));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
            }
        } else {
            if (!emptyHand) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.PASS);
                return;
            }

            if (clickedBlock == ModBlocks.CARVING_SLAB.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }
        }
    }
}
