package com.abysslasea.anvilinnovate.mixin;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.dries007.tfc.common.recipes.KnappingRecipe;
import net.dries007.tfc.compat.jei.JEIIntegration;
import net.dries007.tfc.util.KnappingType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = JEIIntegration.class, remap = false)
public abstract class JEIIntegrationMixin {
    @Shadow
    private static Map<KnappingType, RecipeType<KnappingRecipe>> KNAPPING_TYPES;

    @Inject(
            method = "registerCategories(Lmezz/jei/api/registration/IRecipeCategoryRegistration;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void onRegisterCategories(IRecipeCategoryRegistration registry, CallbackInfo ci) {
        KNAPPING_TYPES.clear();
    }

    @Inject(
            method = "registerRecipes(Lmezz/jei/api/registration/IRecipeRegistration;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void onRegisterRecipes(IRecipeRegistration registry, CallbackInfo ci) {
        KNAPPING_TYPES.clear();
    }
}