package com.abysslasea.anvilinnovate.block;

import com.abysslasea.anvilinnovate.NetworkHandler;
import com.abysslasea.anvilinnovate.block.flint.ChiseledFlintSlabBlockEntity;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "anvilinnovate", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandlers {

    private static final TagKey<Item> CARVING_FLINTS = ItemTags.create(new ResourceLocation("anvilinnovate", "carving_flints"));

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            BlockState state = event.getLevel().getBlockState(event.getPos());
            if (state.getBlock() == ModBlocks.CARVING_SLAB.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
            return;
        }

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        Direction face = event.getFace();
        ItemStack held = player.getMainHandItem();
        BlockState state = level.getBlockState(pos);
        boolean isSlab = state.getBlock() == ModBlocks.CARVING_SLAB.get();
        boolean flint = held.is(CARVING_FLINTS);

        if (face == Direction.UP) {
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (!aboveState.canBeReplaced() || aboveState.getBlock() instanceof DoorBlock || aboveState.getBlock() instanceof DoublePlantBlock) {
                return;
            }
        }

        if (isSlab) {
            if (flint) {
                handleCarvingSlabInteraction(level, player, pos, event);
            } else {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        } else if (flint && face == Direction.UP) {
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.canBeReplaced() && !(aboveState.getBlock() instanceof DoorBlock) && !(aboveState.getBlock() instanceof DoublePlantBlock)) {
                if (player instanceof ServerPlayer sp) {
                    NetworkHandler.sendToClient(sp, new OpenTemplateScreenPacket(pos));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
    }

    private static void handleCarvingSlabInteraction(Level level, Player player, BlockPos pos, PlayerInteractEvent.RightClickBlock event) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ChiseledFlintSlabBlockEntity slab)) return;

        ResourceLocation id = slab.getTemplateId();
        if (id == null) {
            if (player instanceof ServerPlayer sp) {
                NetworkHandler.sendToClient(sp, new OpenTemplateScreenPacket(pos));
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        Vec3 hit = event.getHitVec().getLocation();
        Vec3 rel = hit.subtract(pos.getX(), pos.getY(), pos.getZ());
        float offset = (1f - 13f/16f)/2f;
        float size = (13f/16f)/10f;
        int x = (int)((rel.x - offset)/size);
        int y = (int)((rel.z - offset)/size);
        if (x<0||x>=10||y<0||y>=10) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        CarvingTemplate t = CarvingTemplateManager.getTemplate(id);
        if (t==null) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        if (t.shouldCarve(x,y)) {
            if (slab.tryCarve(x,y)) {
                level.playSound(null,pos,SoundEvents.STONE_HIT,SoundSource.BLOCKS,0.5f,1f);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        } else {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
}