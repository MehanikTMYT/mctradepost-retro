package com.deathfrog.mctradepost.compat.emi;

import com.deathfrog.mctradepost.recipe.GasifierRecipe;
import com.deathfrog.mctradepost.compat.emi.recipe.MCTPGasifyingEmiRecipe;
import com.deathfrog.mctradepost.compat.emi.category.MCTPGasifyingEmiCategory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.compat.emi.category.MCTPDeconstructionEmiCategory;
import com.deathfrog.mctradepost.compat.emi.category.MCTPPetForagingEmiCategory;
import com.deathfrog.mctradepost.compat.emi.category.MCTPRitualEmiCategory;
import com.deathfrog.mctradepost.compat.emi.recipe.MCTPDeconstructionEmiRecipe;
import com.deathfrog.mctradepost.compat.emi.recipe.MCTPPetForagingEmiRecipe;
import com.deathfrog.mctradepost.compat.emi.recipe.MCTPRitualEmiRecipe;
import com.deathfrog.mctradepost.core.entity.pets.scavenge.PetForagingJeiCache;
import com.deathfrog.mctradepost.core.entity.pets.scavenge.PetForagingJeiEntry;
import com.deathfrog.mctradepost.core.event.wishingwell.ritual.RitualDefinitionHelper;
import com.deathfrog.mctradepost.core.event.wishingwell.ritual.RitualManager;
import com.deathfrog.mctradepost.recipe.DeconstructionRecipe;
import com.deathfrog.mctradepost.recipe.PotionShapelessRecipe;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

/**
 * Native EMI plugin entrypoint for Trade Post.
 * <p>
 * Mirrors MineColonies' {@code MineColoniesEmiPlugin} lifecycle exactly: categories and workstations
 * are registered synchronously in {@link #register}, while everything that reads current game/colony
 * state (rituals, pet foraging entries) goes through {@link EmiRegistry#addDeferredRecipes}, run only
 * after every EMI plugin has finished registering -- late/eager {@code registry.addRecipe(...)} calls
 * for this kind of data would race EMI's own bake/index step.
 * <p>
 * {@link com.deathfrog.mctradepost.recipe.UniqueTagShapelessRecipe} and
 * {@link com.deathfrog.mctradepost.recipe.DurableToolShapelessRecipe} are deliberately NOT handled
 * here: both are plain {@code CraftingRecipe} implementations registered under the standard
 * {@code RecipeType.CRAFTING}, so EMI's own {@code VanillaPlugin} already discovers and displays them
 * generically (via {@code CraftingRecipe#placementInfo()}/{@code #getRemainingItems(CraftingInput)}),
 * including correct tag-alternative indexing and crafting-remainder rendering. Adding a duplicate
 * hand-written entry for either would only risk fighting EMI's own recipe-id de-duplication.
 * {@link PotionShapelessRecipe} cannot rely on that generic path (see {@link #replacePotionRecipes}),
 * and {@link DeconstructionRecipe} isn't reached by it at all (see {@link #registerDeconstructionRecipes}).
 */
@EmiEntrypoint
public class MCTPEmiPlugin implements EmiPlugin
{
    private MCTPRitualEmiCategory ritualCategory;
    private MCTPPetForagingEmiCategory petForagingCategory;
    private MCTPDeconstructionEmiCategory deconstructionCategory;
    private MCTPGasifyingEmiCategory gasifyingCategory;

    @Override
    public void register(final EmiRegistry registry)
    {
        final Block mixedStone = MCTradePostMod.MIXED_STONE.get();
        this.ritualCategory = new MCTPRitualEmiCategory(mixedStone == null ? EmiStack.EMPTY : EmiStack.of(mixedStone));
        this.petForagingCategory =
            new MCTPPetForagingEmiCategory(EmiStack.of(MCTradePostMod.blockHutPetShop.get()));
        this.deconstructionCategory =
            new MCTPDeconstructionEmiCategory(EmiStack.of(MCTradePostMod.blockHutRecycling.get()));

        registry.addCategory(ritualCategory);
        registry.addCategory(petForagingCategory);
        registry.addCategory(deconstructionCategory);
        this.gasifyingCategory = new MCTPGasifyingEmiCategory(EmiStack.of(MCTradePostMod.GASIFIER.get()));
        registry.addCategory(gasifyingCategory);
        registry.addWorkstation(gasifyingCategory, EmiStack.of(MCTradePostMod.GASIFIER.get()));

        if (mixedStone != null)
        {
            registry.addWorkstation(ritualCategory, EmiStack.of(mixedStone));
        }
        registry.addWorkstation(petForagingCategory, EmiStack.of(MCTradePostMod.blockHutPetShop.get()));
        registry.addWorkstation(petForagingCategory, EmiStack.of(MCTradePostMod.FEEDER.get()));
        registry.addWorkstation(petForagingCategory, EmiStack.of(MCTradePostMod.DREDGER.get()));
        registry.addWorkstation(petForagingCategory, EmiStack.of(MCTradePostMod.SCAVENGE.get()));
        registry.addWorkstation(deconstructionCategory, EmiStack.of(MCTradePostMod.blockHutRecycling.get()));

        replacePotionRecipes(registry);
        registerDeconstructionRecipes(registry);
        registerGasifyingRecipes(registry);

        registry.addDeferredRecipes(recipeConsumer -> populateRecipes(recipeConsumer::accept));
    }

    /**
     * Gathers ritual and pet-foraging display entries from current server-synced client state, during
     * EMI's deferred-recipe phase (after all plugins have run, before EMI's bake/index step).
     */
    private void populateRecipes(final Consumer<EmiRecipe> consumer)
    {
        for (final RitualDefinitionHelper ritual : RitualManager.getAllRituals().values())
        {
            consumer.accept(new MCTPRitualEmiRecipe(ritualCategory, ritual));
        }

        for (final PetForagingJeiEntry entry : PetForagingJeiCache.getEntries())
        {
            consumer.accept(new MCTPPetForagingEmiRecipe(petForagingCategory, entry));
        }
    }

    /**
     * {@link PotionShapelessRecipe} is a plain {@code CraftingRecipe}, so EMI's own
     * {@code VanillaPlugin} already generates a generic "minecraft:crafting" entry for every instance
     * it finds in the recipe manager -- but that generic path converts each {@code PotionInput} into a
     * vanilla item-only {@code Ingredient} (see {@code PotionShapelessRecipe#getIngredients()}), which
     * cannot express "this potion item, but only the water variant". The generated entry would
     * therefore accept/search-match ANY potion (water, healing, poison, ...), not just the one this
     * recipe actually requires -- silently wrong R/U. This removes those generic entries and replaces
     * them with correctly potion-component-aware ones built straight from
     * {@link PotionShapelessRecipe#getPotionInputs()}.
     */
    private void replacePotionRecipes(final EmiRegistry registry)
    {
        final RecipeMap recipeMap = registry.getRecipeMap();
        if (recipeMap == null)
        {
            return;
        }

        final Set<Identifier> potionRecipeIds = new HashSet<>();
        final List<EmiRecipe> replacements = new ArrayList<>();

        for (final RecipeHolder<?> holder : recipeMap.byType(RecipeType.CRAFTING))
        {
            if (!(holder.value() instanceof PotionShapelessRecipe recipe))
            {
                continue;
            }

            final Identifier id = holder.id().identifier();
            potionRecipeIds.add(id);

            final List<EmiIngredient> inputs = new ArrayList<>();
            for (final PotionShapelessRecipe.PotionInput input : recipe.getPotionInputs())
            {
                final EmiStack stack = EmiStack.of(input.displayStack());
                for (int i = 0; i < input.count(); i++)
                {
                    inputs.add(stack);
                }
            }

            replacements.add(new EmiCraftingRecipe(inputs, EmiStack.of(recipe.getResultStack()), id));
        }

        if (potionRecipeIds.isEmpty())
        {
            return;
        }

        registry.removeRecipes(r -> potionRecipeIds.contains(r.getId()));
        replacements.forEach(registry::addRecipe);
    }

    /**
     * {@link DeconstructionRecipe} implements plain {@code Recipe<SingleRecipeInput>} under its own
     * custom {@code MCTradePostMod.DECON_RECIPE_TYPE}, not {@code CraftingRecipe}/
     * {@code RecipeType.CRAFTING}. EMI's {@code VanillaPlugin} only walks a fixed list of known
     * vanilla {@code RecipeType}s (crafting, smelting, smithing, stonecutting, ...) and has no generic
     * fallback for arbitrary custom ones, so these recipes are never discovered automatically -- they
     * need an explicit native EMI recipe, unlike {@link PotionShapelessRecipe} (needs replacing) or
     * {@code UniqueTagShapelessRecipe}/{@code DurableToolShapelessRecipe} (need nothing at all).
     */
    private void registerDeconstructionRecipes(final EmiRegistry registry)
    {
        final RecipeMap recipeMap = registry.getRecipeMap();
        final RecipeType<DeconstructionRecipe> deconType = MCTradePostMod.DECON_RECIPE_TYPE.get();
        if (recipeMap == null || deconType == null)
        {
            return;
        }

        for (final RecipeHolder<DeconstructionRecipe> holder : recipeMap.byType(deconType))
        {
            registry.addRecipe(new MCTPDeconstructionEmiRecipe(deconstructionCategory, holder));
        }
    }

    /** Gasifier recipes (upstream shows them through JEI; this pack uses EMI). */
    private void registerGasifyingRecipes(final EmiRegistry registry)
    {
        final RecipeMap recipeMap = registry.getRecipeMap();
        if (recipeMap == null)
        {
            return;
        }

        for (final RecipeHolder<GasifierRecipe> holder : recipeMap.byType(MCTradePostMod.GASIFIER_RECIPE_TYPE.get()))
        {
            registry.addRecipe(new MCTPGasifyingEmiRecipe(gasifyingCategory, holder));
        }
    }
}
