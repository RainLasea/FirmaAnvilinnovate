package com.abysslasea.anvilinnovate.block.clay;

import com.abysslasea.anvilinnovate.NetworkHandler;
import com.abysslasea.anvilinnovate.block.ModBlocks;
import com.abysslasea.anvilinnovate.template.CarvingTemplate;
import com.abysslasea.anvilinnovate.template.CarvingTemplateManager;
import com.abysslasea.anvilinnovate.template.packet.OpenTemplateScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "anvilinnovate", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClayEventHandlers {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        Direction face = event.getFace();
        BlockState state = level.getBlockState(pos);
        ItemStack held = player.getMainHandItem();

        boolean isClay = state.getBlock() == ModBlocks.CLAY_FORMING.get();
        boolean clayBall = held.getItem() == Items.CLAY_BALL;

        if (isClay) {
            if (!clayBall) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof ClayFormingBlockEntity clayBE)) return;

            ResourceLocation templateId = clayBE.getTemplateId();
            if (templateId == null) {
                if (player instanceof ServerPlayer sp) {
                    NetworkHandler.sendToClient(sp, new OpenTemplateScreenPacket(pos, "clay_template"));
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            Vec3 hit = event.getHitVec().getLocation();
            Vec3 rel = hit.subtract(pos.getX(), pos.getY(), pos.getZ());

            final float OFFSET = (1f - 14f / 16f) / 2f;
            final float SIZE = (14f / 16f) / 14f;

            int x = (int) ((rel.x - OFFSET) / SIZE);
            int y = (int) ((rel.y - OFFSET) / SIZE);
            int z = (int) ((rel.z - OFFSET) / SIZE);

            if (x < 0 || x >= 14 || y < 0 || y >= 14 || z < 0 || z >= 14) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }

            CarvingTemplate template = CarvingTemplateManager.getTemplate(templateId);
            if (template != null && template.shouldCarve(x, y, z)) {
                if (clayBE.tryForm(x, y, z)) {
                    level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.5f, 1f);
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            } else {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
            return;
        }

        if (clayBall && face == Direction.UP) {
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.canBeReplaced()
                    && !(aboveState.getBlock() instanceof DoorBlock)
                    && !(aboveState.getBlock() instanceof DoublePlantBlock)) {
                if (player instanceof ServerPlayer sp) {
                    NetworkHandler.sendToClient(sp, new OpenTemplateScreenPacket(pos, "clay_template"));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();

        if (held.getItem() == Items.CLAY_BALL) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}