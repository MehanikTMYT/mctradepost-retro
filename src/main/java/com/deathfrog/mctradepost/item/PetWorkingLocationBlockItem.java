package com.deathfrog.mctradepost.item;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.core.blocks.AbstractBlockPetWorkingLocation;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/**
 * BlockItem for {@link AbstractBlockPetWorkingLocation} blocks (trough, feeder, hauler, scavenge, dredger). Block has no
 * appendHoverText hook of its own, so this item delegates to the paired block's tooltip logic.
 */
public class PetWorkingLocationBlockItem extends BlockItem
{
    public PetWorkingLocationBlockItem(@Nonnull Block block, @Nonnull Properties properties)
    {
        super(block, properties);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nonnull Item.TooltipContext context,
                                @Nonnull TooltipDisplay display,
                                @Nonnull Consumer<Component> tooltip,
                                @Nonnull TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);

        if (this.getBlock() instanceof AbstractBlockPetWorkingLocation petBlock)
        {
            petBlock.appendHoverText(stack, context, tooltip, flag);
        }
    }
}
