package com.deathfrog.mctradepost.item;

import net.minecraft.world.item.ItemStack;

/**
 * Marks an {@link net.minecraft.world.item.Item} that acts as a durable crafting-grid tool: when used as an
 * ingredient it is not consumed outright, but instead comes back damaged by one use (or disappears once its
 * durability is exhausted).
 * <p>
 * {@code Item.hasCraftingRemainingItem()}/{@code getCraftingRemainingItem(ItemStack)} were removed in 26.1.2;
 * crafting-grid remainders are now produced by {@link net.minecraft.world.item.crafting.CraftingRecipe#getRemainingItems}
 * on the recipe itself, which is why this lives as a per-item strategy that a custom {@code CraftingRecipe}
 * (see {@link com.deathfrog.mctradepost.recipe.DurableToolShapelessRecipe}) consults per slot, rather than as an
 * {@code Item} override.
 */
public interface IDamageableCraftingTool
{
    /**
     * Computes this tool's crafting-grid remainder for one use, without mutating {@code toolStack}.
     * <p>
     * Preserves every component on the original stack (custom name, enchantments, custom data, etc.) since the
     * remainder starts from a full {@link ItemStack#copy()}, only the damage value changes.
     *
     * @param toolStack the tool stack as it appeared in the crafting grid; not mutated
     * @return a damaged copy of {@code toolStack}, or {@link ItemStack#EMPTY} once durability is exhausted
     */
    default ItemStack computeCraftingRemainder(ItemStack toolStack)
    {
        final ItemStack remainder = toolStack.copy();
        final int currentDamage = remainder.getDamageValue();
        final int maxDamage = remainder.getMaxDamage();

        if (currentDamage < maxDamage)
        {
            remainder.setDamageValue(currentDamage + 1);
        }

        return remainder.getDamageValue() >= maxDamage ? ItemStack.EMPTY : remainder;
    }
}
