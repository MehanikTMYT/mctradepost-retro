package com.deathfrog.mctradepost.api.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Defines the recycler's item matching semantics. Durability and the exact
 * enchantments are ignored, but enchanted and unenchanted stacks remain
 * distinct and all other components must match.
 *
 * <p>This is deliberately NOT the same identity model as recipe matching (which is
 * component-exact for potions/unique tags) or AE2 key identity. The recycler needs
 * "same recyclable category" semantics so that a worn tool and a pristine tool are
 * ordered and consumed as one category, while an enchanted variant stays separate
 * because stripping enchantments is a distinct recycling outcome.</p>
 */
public final class RecyclableItemMatcher
{
    private RecyclableItemMatcher()
    {
    }

    /**
     * Compares a recyclable definition against a candidate stack using recycler category rules.
     *
     * @param definition the stored recyclable definition.
     * @param candidate  the concrete stack to test.
     * @return true if the candidate belongs to the same recyclable category as the definition.
     */
    public static boolean matches(final ItemStack definition, final ItemStack candidate)
    {
        if (definition == null || candidate == null || definition.isEmpty() || candidate.isEmpty())
        {
            return false;
        }

        if (isEnchanted(definition) != isEnchanted(candidate))
        {
            return false;
        }

        final ItemStack normalizedDefinition = normalize(definition);
        final ItemStack normalizedCandidate = normalize(candidate);

        if (normalizedCandidate == null || normalizedDefinition == null)
        {
            return false;
        }

        return ItemStack.isSameItemSameComponents(normalizedDefinition, normalizedCandidate);
    }

    /**
     * Determines whether a stack carries either applied or stored enchantments.
     *
     * @param stack the stack to test.
     * @return true if the stack is enchanted or is an enchantment carrier such as an enchanted book.
     */
    @SuppressWarnings("null")
    public static boolean isEnchanted(final ItemStack stack)
    {
        return stack != null && !stack.isEmpty()
            && (stack.isEnchanted()
                || !stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty());
    }

    /**
     * Produces a single-count copy with durability and enchantment payloads stripped, so that only the
     * remaining components participate in the comparison.
     *
     * @param stack the stack to normalize.
     * @return the normalized copy.
     */
    @SuppressWarnings("null")
    private static ItemStack normalize(final ItemStack stack)
    {
        final ItemStack normalized = stack.copy();
        normalized.setCount(1);
        normalized.remove(DataComponents.DAMAGE);
        normalized.remove(DataComponents.ENCHANTMENTS);
        normalized.remove(DataComponents.STORED_ENCHANTMENTS);
        return normalized;
    }
}
