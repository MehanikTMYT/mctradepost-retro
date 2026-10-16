package com.deathfrog.mctradepost.core.colony.buildings.workerbuildings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.deathfrog.mctradepost.core.colony.buildings.workerbuildings.DisplayCase.SaleState;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/**
 * Regression coverage for the display-shelf state-preservation contract that
 * {@code BuildingMarketplace.lostShelfAtDisplayPos(...)} depends on.
 *
 * <p>Upstream mctradepost-retro commit {@code 3d6e07a} ("AI improvements") changed that method from
 * <em>replacing</em> the persisted {@link DisplayCase} with a freshly constructed one to <em>mutating
 * only the frame identity</em> via {@link DisplayCase#setFrameId(UUID)}. The reason is that an item
 * frame entity can be swapped out (re-placed by a player, replaced by Fast Item Frames, reloaded with
 * a new entity UUID) while the shelf itself, its merchandise and any in-flight customer order are
 * unchanged. Rebuilding the record threw away the sale state and reset the tick counter, so a
 * visitor's placed order silently reverted and stocked merchandise stopped being advertised.
 *
 * <p>These tests are deliberately plain JUnit and touch no {@code Level}: constructing a
 * {@link DisplayCase} only needs {@link BlockPos} and {@link ItemStack#EMPTY}.
 */
class DisplayCaseFrameReplacementTest
{
    private static final BlockPos SHELF = new BlockPos(12, 65, -7);

    /**
     * The behaviour {@code 3d6e07a} introduced: swapping the frame entity identity must leave
     * merchandise, tick counter and order state completely untouched.
     */
    @Test
    void changingFrameIdentityPreservesMerchandiseAndOrderState()
    {
        final UUID oldFrame = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
        final UUID newFrame = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

        final ItemStack merchandise = ItemStack.EMPTY;
        final DisplayCase display = new DisplayCase(SHELF, oldFrame, merchandise, 17);
        display.setSaleState(SaleState.ORDER_PLACED);

        display.setFrameId(newFrame);

        assertEquals(newFrame, display.getFrameId(), "frame identity should be the only thing that changed");
        assertEquals(SHELF, display.getPos());
        assertSame(merchandise, display.getStack(), "merchandise must survive a frame entity swap");
        assertEquals(17, display.getTickcount(), "tick counter must not be reset by a frame entity swap");
        assertEquals(SaleState.ORDER_PLACED, display.getSaleState(),
            "an order already placed by a visitor must not be reverted behind their back");
    }

    /**
     * The Fast Item Frames branch: the frame-like block exists but there is no entity, so the UUID is
     * cleared. Clearing it must likewise not disturb the shelf's contents or its order state.
     */
    @Test
    void clearingFrameIdentityPreservesMerchandiseAndOrderState()
    {
        final DisplayCase display =
            new DisplayCase(SHELF, UUID.fromString("00000000-0000-0000-0000-0000000000c3"), ItemStack.EMPTY, 5);
        display.setSaleState(SaleState.FOR_SALE);

        display.setFrameId(null);

        assertNull(display.getFrameId());
        assertEquals(5, display.getTickcount());
        assertEquals(SaleState.FOR_SALE, display.getSaleState());
    }

    /**
     * Pins the failure mode the fix removed, so a future refactor cannot quietly reintroduce it:
     * rebuilding the record instead of mutating it loses both the tick counter and the sale state,
     * because {@link DisplayCase}'s constructors always start at {@link SaleState#NONE}.
     */
    @Test
    void rebuildingTheRecordInsteadOfMutatingItLosesOrderState()
    {
        final UUID newFrame = UUID.fromString("00000000-0000-0000-0000-0000000000d4");

        final DisplayCase original = new DisplayCase(SHELF, null, ItemStack.EMPTY, 17);
        original.setSaleState(SaleState.ORDER_PLACED);

        final DisplayCase rebuilt = new DisplayCase(SHELF, newFrame, original.getStack(), 0);

        assertNotEquals(original.getSaleState(), rebuilt.getSaleState());
        assertEquals(SaleState.NONE, rebuilt.getSaleState());
        assertEquals(0, rebuilt.getTickcount());
    }
}
