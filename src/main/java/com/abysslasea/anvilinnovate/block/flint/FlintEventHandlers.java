package com.abysslasea.anvilinnovate.block.flint;

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
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
public class FlintEventHandlers {

    private static final TagKey<Item> CARVING_FLINTS = ItemTags.create(new ResourceLocation("anvilinnovate", "carving_flints"));

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        Direction face = event.getFace();
        ItemStack held = player.getMainHandItem();
        BlockState state = level.getBlockState(pos);

        boolean isFlint = held.is(CARVING_FLINTS);
        boolean isSlab = state.getBlock() == ModBlocks.CARVING_SLAB.get();

        if (isSlab) {
            if (!isFlint) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof ChiseledFlintSlabBlockEntity slab)) return;

            ResourceLocation templateId = slab.getTemplateId();
            if (templateId == null) {
                if (player instanceof ServerPlayer sp) {
                    NetworkHandler.sendToClient(sp, new OpenTemplateScreenPacket(pos, "carving_template"));
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            Vec3 hit = event.getHitVec().getLocation();
            Vec3 rel = hit.subtract(pos.getX(), pos.getY(), pos.getZ());
            final float OFFSET = (1f - 13f / 16f) / 2f;
            final float SIZE = (13f / 16f) / 10f;

            int x = (int) ((rel.x - OFFSET) / SIZE);
            int y = (int) ((rel.z - OFFSET) / SIZE);

            if (x < 0 || x >= 10 || y < 0 || y >= 10) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }

            CarvingTemplate template = CarvingTemplateManager.getTemplate(templateId);
            if (template != null && template.shouldCarve(x, y, 0)) {
                if (slab.tryCarve(x, y)) {
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

        if (isFlint && face == Direction.UP) {
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.canBeReplaced()
                    && !(aboveState.getBlock() instanceof DoorBlock)
                    && !(aboveState.getBlock() instanceof DoublePlantBlock)) {
                if (player instanceof ServerPlayer sp) {
                    NetworkHandler.sendToClient(sp, new OpenTemplateScreenPacket(pos, "carving_template"));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
    }
}