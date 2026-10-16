package com.deathfrog.mctradepost.compat.emi.category;

import com.deathfrog.mctradepost.MCTradePostMod;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * EMI category for pet foraging display entries. Uses the {@code emi.mctradepost.pet_foraging}
 * translation (a neutral rename of the old {@code jei.mctradepost.pet_foraging} key, kept for the
 * legacy {@code compat/jei} category), matching how {@code MineColoniesEmiRecipeCategory} reuses job
 * translation keys instead of minting new {@code emi.category.*} ones.
 */
public class MCTPPetForagingEmiCategory extends EmiRecipeCategory
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "pet_foraging");

    public MCTPPetForagingEmiCategory(final EmiRenderable icon)
    {
        super(ID, icon);
    }

    @Override
    public Component getName()
    {
        return Component.translatable("emi.mctradepost.pet_foraging");
    }
}
