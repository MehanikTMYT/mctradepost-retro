package com.deathfrog.mctradepost.item;

import javax.annotation.Nonnull;
import net.minecraft.world.item.Item;

/**
 * Durable tool used by the "mctradepost:to_tradecoin" crafting recipe: takes one point of damage per craft and
 * disappears once its durability is exhausted, instead of being consumed outright. See
 * {@link com.deathfrog.mctradepost.recipe.DurableToolShapelessRecipe}, which is the recipe type that actually
 * produces this remainder (crafting-grid remainders are computed by the recipe since 26.1.2, not the Item).
 */
public class CurrencyExchangeItem extends Item implements IDamageableCraftingTool
{
    public CurrencyExchangeItem(@Nonnull Properties properties)
    {
        super(properties);
    }
}
