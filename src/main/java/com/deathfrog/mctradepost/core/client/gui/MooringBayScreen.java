package com.deathfrog.mctradepost.core.client.gui;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.core.inventory.MooringBayMenu;
import com.deathfrog.mctradepost.core.blocks.blockentity.LiftingGasStorage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Client screen for installing Route Surveys and viewing Mooring Bay gas storage. */
public class MooringBayScreen extends AbstractContainerScreen<MooringBayMenu>
{
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/hopper.png");
    private static final int EXTRA_LINE_HEIGHT = 18;
    private static final int ORIGINAL_IMAGE_HEIGHT = 133;

    public MooringBayScreen(MooringBayMenu menu, Inventory inventory, Component title)
    {
        // 26.1.2: imageWidth/imageHeight are final and passed to the super constructor.
        super(menu, inventory, title, 176, ORIGINAL_IMAGE_HEIGHT + EXTRA_LINE_HEIGHT);
        inventoryLabelY = 39 + EXTRA_LINE_HEIGHT;
    }

    @SuppressWarnings("null")
    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor g, int x, int y, float p)
    {
        super.extractBackground(g, x, y, p);
        // Insert a text-height strip before the hopper slots while retaining the vanilla texture's borders.
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, 18, 256, 256);
        for (int row = 0; row < EXTRA_LINE_HEIGHT; row++)
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos + 18 + row, 0.0F, 17.0F, imageWidth, 1, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos + 18 + EXTRA_LINE_HEIGHT, 0.0F, 18.0F, imageWidth, ORIGINAL_IMAGE_HEIGHT - 18, 256, 256);
    }

    @SuppressWarnings("null")
    @Override
    protected void extractLabels(@Nonnull GuiGraphicsExtractor g, int x, int y)
    {
        super.extractLabels(g, x, y);
        Component gas = Component.translatable(LiftingGasStorage.LIFTING_GAS_DISPLAY_KEY, menu.gas(), menu.capacity());
        g.text(font, gas, 8, 20, 0xFF404040, false);
    }
}
