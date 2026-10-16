package com.deathfrog.mctradepost.core.client.render;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.api.util.NullnessBridge;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.items.component.ColonyId;
import com.minecolonies.api.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
public class AdvancedClipBoardDecorator implements IItemDecorator {
    private static IColonyView colonyView;
    private static boolean     render = false;
    private        long        lastChange;

    @Override
    public boolean render(@Nonnull GuiGraphicsExtractor graphics, @Nonnull Font font,@Nonnull ItemStack stack, int xOffset, int yOffset)
    {
        @SuppressWarnings("null")
        final long gametime = Minecraft.getInstance().level.getGameTime();

        if (lastChange != gametime && gametime % 40 == 0)
        {
            lastChange = gametime;
            render = !render;
        }

        if (render)
        {
            colonyView = ColonyId.readColonyViewFromItemStack(stack);
            if (colonyView != null)
            {
                try
                {
                    int count = 0;
                    for (final ICitizenDataView view : colonyView.getCitizens().values())
                    {
                        if (view.hasBlockingInteractions())
                        {
                            count++;
                        }
                    }

                    if (count > 0)
                    {
                        // nextStratum() replaces the old PoseStack z-translate trick for guaranteeing
                        // this draws above the item/stack-count layers already rendered by this point.
                        graphics.nextStratum();
                        graphics.centeredText(font,
                            NullnessBridge.assumeNonnull(Component.literal(count + "")),
                            xOffset + 15,
                            yOffset - 2,
                            0xFF4500 | (255 << 24));
                        return true;
                    }
                }
                catch (Exception e)
                {
                    Log.getLogger().error("Something went wrong with the Advanced ClipBoard item decorator", e);
                }
            }
        }
        return false;
    }    
}
