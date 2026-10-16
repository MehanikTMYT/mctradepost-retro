package com.deathfrog.mctradepost.compat.emi.category;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.recipe.GasifierRecipe;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** EMI category for Gasifier recipes (replaces upstream's JEI GasifierCategory). */
public class MCTPGasifyingEmiCategory extends EmiRecipeCategory
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, GasifierRecipe.ID);

    public MCTPGasifyingEmiCategory(final EmiRenderable icon)
    {
        super(ID, icon);
    }

    @Override
    public Component getName()
    {
        return Component.translatable("jei.mctradepost.gasifying");
    }
}
