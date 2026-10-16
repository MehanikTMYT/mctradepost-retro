package com.deathfrog.mctradepost.compat.emi.category;

import com.deathfrog.mctradepost.MCTradePostMod;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * EMI category for wishing-well rituals. Uses the {@code emi.mctradepost.rituals} translation (a
 * neutral rename of the old {@code jei.mctradepost.rituals} key, kept for the legacy
 * {@code compat/jei} category) instead of minting a new {@code emi.category.*} key, matching how
 * {@code MineColoniesEmiRecipeCategory} reuses job translation keys.
 */
public class MCTPRitualEmiCategory extends EmiRecipeCategory
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "rituals");

    public MCTPRitualEmiCategory(final EmiRenderable icon)
    {
        super(ID, icon);
    }

    @Override
    public Component getName()
    {
        return Component.translatable("emi.mctradepost.rituals");
    }
}
