package com.deathfrog.mctradepost.item;

import com.deathfrog.mctradepost.api.util.NullnessBridge;

import net.minecraft.world.item.Item;

/**
 * Base for durable tools used by bartender shapeless recipes (energy_shake, protien_shake, fruit_juice, mystic_tea,
 * daiquiri, veggie_juice, vanilla_milkshake, smetana, slime_ball): takes one point of damage per craft and
 * disappears once its durability is exhausted, instead of being consumed outright. See
 * {@link com.deathfrog.mctradepost.recipe.DurableToolShapelessRecipe}, which is the recipe type that actually
 * produces this remainder (crafting-grid remainders are computed by the recipe since 26.1.2, not the Item).
 */
public abstract class AbstractCraftCountItem extends Item implements IDamageableCraftingTool
{

    public AbstractCraftCountItem(Properties properties)
    {
        super(NullnessBridge.assumeNonnull(properties));
    }
}
