package com.abysslasea.anvilinnovate.mixin;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.dries007.tfc.client.screen.KnappingScreen;
import net.dries007.tfc.compat.jei.JEIIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = JEIIntegration.class, remap = false)
public abstract class JEIIntegrationGuiMixin {

    @Redirect(
            method = "registerGuiHandlers(Lmezz/jei/api/registration/IGuiHandlerRegistration;)V",
            at = @At(
                    value = "INVOKE",
                    target = "mezz.jei.api.registration.IGuiHandlerRegistration" +
                            ".addRecipeClickArea" +
                            "(Ljava/lang/Class;IIII[Lmezz/jei/api/recipe/RecipeType;)V"
            ),
            remap = false
    )
    private <T> void skipKnappingClickArea(
            IGuiHandlerRegistration registry,
            Class<? extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>> screenClass,
            int x, int y, int width, int height,
            RecipeType<?>... recipeTypes
    ) {
        if (screenClass == KnappingScreen.class) {
            return;
        }
        registry.addRecipeClickArea(screenClass, x, y, width, height, recipeTypes);
    }
}
