package com.abysslasea.anvilinnovate.item.stonethrower;

import com.abysslasea.anvilinnovate.Anvilinnovate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Anvilinnovate.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StoneSkipHandler {

    private static boolean isLooseRock(ItemStack stack) {
        ResourceLocation id = stack.getItem().builtInRegistryHolder().key().location();
        String path = id.getPath();
        return path.startsWith("rock/loose/") || path.startsWith("rock/mossy_loose/");
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem evt) {
        Player player = (Player) evt.getEntity();
        Level level = evt.getLevel();
        InteractionHand hand = evt.getHand();
        ItemStack stack = evt.getItemStack();

        if (!isLooseRock(stack)) return;

        evt.setCanceled(true);
        evt.setCancellationResult(InteractionResult.SUCCESS);

        if (!level.isClientSide) {
            StoneSkipEntity stone = new StoneSkipEntity(
                    ThrowingStones.STONE_SKIP.get(),
                    level,
                    player,
                    stack.copy()
            );

            stone.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, 0.8f, 1f);
            level.addFreshEntity(stone);

            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        player.swing(hand);
        evt.setCancellationResult(InteractionResult.SUCCESS);
    }
}