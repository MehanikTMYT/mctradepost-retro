package com.deathfrog.mctradepost.compat.emi.category;

import com.deathfrog.mctradepost.MCTradePostMod;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * EMI category for {@link com.deathfrog.mctradepost.recipe.DeconstructionRecipe}.
 * <p>
 * Unlike {@link MCTPRitualEmiCategory}/{@link MCTPPetForagingEmiCategory}, there is no legacy JEI
 * category to port here -- deconstruction recipes were never registered with JEI. This is new,
 * because {@code DeconstructionRecipe} implements plain {@code Recipe<SingleRecipeInput>} under its
 * own custom {@code RecipeType} (not {@code CraftingRecipe}/{@code RecipeType.CRAFTING}), so EMI's own
 * {@code VanillaPlugin} has no generic fallback that would ever discover it (see the migration report,
 * section F, for the source-level verification of that).
 */
public class MCTPDeconstructionEmiCategory extends EmiRecipeCategory
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "deconstruction");

    public MCTPDeconstructionEmiCategory(final EmiRenderable icon)
    {
        super(ID, icon);
    }

    @Override
    public Component getName()
    {
        return Component.translatable("block.mctradepost.blockhutrecycling");
    }
}
