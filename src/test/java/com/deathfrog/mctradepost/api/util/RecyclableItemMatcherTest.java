package com.deathfrog.mctradepost.api.util;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;

/**
 * Round 15 regression coverage for {@link RecyclableItemMatcher}, ported semantically from upstream
 * 4cbf063e ("Stew plus", release 1.06.011).
 *
 * <p><b>Coverage limitation, stated explicitly.</b> This project's {@code test} source set is a plain
 * JUnit run with no FML loader, so any Minecraft class whose static initializer reaches
 * {@code SharedConstants} or {@code Bootstrap} cannot be touched here. Constructing a populated
 * {@link ItemStack} fails with either {@code IllegalStateException: There is no current FML Loader}
 * (via {@code SharedConstants.<clinit>} -> {@code FMLEnvironment.isProduction}) or
 * {@code IllegalArgumentException: Not bootstrapped (called from registry minecraft:game_event)}
 * (via {@code Items.<clinit>} -> {@code Blocks.<clinit>}). Consequently only the null/empty guard
 * contract - which short-circuits before any registry access - is machine-verified here.</p>
 *
 * <p>The component-sensitive behaviour (durability ignored, exact enchantment payload ignored,
 * enchanted vs unenchanted distinct, all other components significant) therefore remains on the
 * human validation backlog for the Recycling Center, together with the rest of the live recycler
 * checks. Machine-verifying it would require standing up an FML-bootstrapped test source set
 * (NeoGradle's {@code testJunit} task is present but NO-SOURCE), which is a build-infrastructure
 * change outside this round's scope.</p>
 */
class RecyclableItemMatcherTest
{
    /**
     * The recycler compares definitions that may deserialize to an empty stack (removed mod item,
     * stale saved selection). Those must never match anything, or a stale definition would claim
     * every warehouse stack in {@code RecyclingItemListModule.matches}.
     */
    @Test
    void emptyStacksNeverMatch()
    {
        assertFalse(RecyclableItemMatcher.matches(ItemStack.EMPTY, ItemStack.EMPTY));
    }

    @Test
    void nullStacksNeverMatch()
    {
        assertFalse(RecyclableItemMatcher.matches(null, ItemStack.EMPTY));
        assertFalse(RecyclableItemMatcher.matches(ItemStack.EMPTY, null));
        assertFalse(RecyclableItemMatcher.matches(null, null));
    }

    @Test
    void emptyStackIsNotEnchanted()
    {
        assertFalse(RecyclableItemMatcher.isEnchanted(ItemStack.EMPTY));
        assertFalse(RecyclableItemMatcher.isEnchanted(null));
    }
}
