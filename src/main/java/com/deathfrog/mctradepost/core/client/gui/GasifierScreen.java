package com.deathfrog.mctradepost.core.client.gui;

import java.util.List;
import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.core.inventory.GasifierMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import java.util.Optional;
import com.deathfrog.mctradepost.core.fluids.MCTPFluids;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;
import org.lwjgl.glfw.GLFW;

/** Client screen for Gasifier fuel, burn progress, and Lifting Gas storage. */
public class GasifierScreen extends AbstractContainerScreen<GasifierMenu>
{
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier BURN_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final Identifier GAS_TEXTURE = Identifier.fromNamespaceAndPath("mctradepost", "block/gas_bubbles");
    private static final int GAS_TEXTURE_SIZE = 16;
    private static final int METER_X = 115;
    private static final int METER_Y = 16;
    private static final int METER_WIDTH = 20;
    private static final int METER_HEIGHT = 54;
    private static final int METER_INNER_HEIGHT = METER_HEIGHT - 4;
    public static final int RECIPE_ARROW_X = 79;
    public static final int RECIPE_ARROW_Y = 34;
    public static final int RECIPE_ARROW_WIDTH = 24;
    public static final int RECIPE_ARROW_HEIGHT = 16;

    private long handCursor;
    private boolean handCursorActive;

    public GasifierScreen(GasifierMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
    }

    @Override
    protected void init()
    {
        super.init();
        if (handCursor == 0L && ModList.get().isLoaded("jei"))
            handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
    }

    @SuppressWarnings("null")
    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor g, int x, int y, float p)
    {
        super.extractRenderState(g, x, y, p);
        updateRecipeArrowCursor(x, y);
        if (x >= leftPos + METER_X && x < leftPos + METER_X + METER_WIDTH &&
            y >= topPos + METER_Y && y < topPos + METER_Y + METER_HEIGHT)
            g.setTooltipForNextFrame(font, List.of(
                Component.translatable("fluid_type.mctradepost.lifting_gas"),
                Component.translatable("container.mctradepost.lifting_gas_meter_amount", menu.gas(), menu.capacity())
                    .withStyle(ChatFormatting.GRAY)), Optional.empty(), x, y);
    }

    @SuppressWarnings("null")
    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor g, int x, int y, float p)
    {
        super.extractBackground(g, x, y, p);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        // The Gasifier uses the furnace input and fuel positions; erase only the unused output slot.
        g.fill(leftPos + 110, topPos + 29, leftPos + 142, topPos + 59, 0xFFC6C6C6);

        if (menu.isLit())
        {
            int height = Mth.ceil(menu.litProgress() * 13.0F) + 1;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - height,
                leftPos + 56, topPos + 50 - height, 14, height);
        }

        int width = Mth.ceil(menu.burnProgress() * 24.0F);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_SPRITE, RECIPE_ARROW_WIDTH, RECIPE_ARROW_HEIGHT, 0, 0,
            leftPos + RECIPE_ARROW_X, topPos + RECIPE_ARROW_Y, width, RECIPE_ARROW_HEIGHT);

        renderGasMeter(g);
    }

    private void updateRecipeArrowCursor(double mouseX, double mouseY)
    {
        boolean shouldUseHand = handCursor != 0L &&
            mouseX >= leftPos + RECIPE_ARROW_X && mouseX < leftPos + RECIPE_ARROW_X + RECIPE_ARROW_WIDTH &&
            mouseY >= topPos + RECIPE_ARROW_Y && mouseY < topPos + RECIPE_ARROW_Y + RECIPE_ARROW_HEIGHT;
        if (shouldUseHand != handCursorActive)
        {
            GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().handle(), shouldUseHand ? handCursor : 0L);
            handCursorActive = shouldUseHand;
        }
    }

    @Override
    public void removed()
    {
        if (handCursorActive)
            GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().handle(), 0L);
        if (handCursor != 0L)
            GLFW.glfwDestroyCursor(handCursor);
        handCursor = 0L;
        handCursorActive = false;
        super.removed();
    }

    private void renderGasMeter(GuiGraphicsExtractor g)
    {
        // Convert the meter's GUI-relative horizontal offset into an absolute screen coordinate.
        int x = leftPos + METER_X;

        // Convert the meter's GUI-relative vertical offset into an absolute screen coordinate.
        int y = topPos + METER_Y;

        // Paint the complete meter rectangle light gray; later calls draw its frame and interior over it.
        g.fill(x, y, x + METER_WIDTH, y + METER_HEIGHT, 0xFFC6C6C6);

        // Draw the two-pixel dark top edge. GuiGraphics.fill uses exclusive right and bottom coordinates.
        g.fill(x, y, x + METER_WIDTH, y + 2, 0xFF555555);

        // Draw the two-pixel dark left edge for the recessed-frame effect.
        g.fill(x, y, x + 2, y + METER_HEIGHT, 0xFF555555);

        // Draw the two-pixel white bottom edge for the raised highlight side of the frame.
        g.fill(x, y + METER_HEIGHT - 1, x + METER_WIDTH, y + METER_HEIGHT, 0xFFFFFFFF);

        // Draw the two-pixel white right edge for the raised highlight side of the frame.
        g.fill(x + METER_WIDTH - 2, y, x + METER_WIDTH, y + METER_HEIGHT, 0xFFFFFFFF);

        // Paint the dark empty-meter cavity; adjust these four insets to hand-tune its bounds.
        g.fill(x + 2, y + 1, x + METER_WIDTH - 2, y + METER_HEIGHT - 1, 0xFF555555);

        // Convert gas/capacity into a pixel height, using zero when the capacity is invalid.
        int fill = menu.capacity() <= 0 ? 0 : Mth.ceil((double) menu.gas() * METER_INNER_HEIGHT / menu.capacity());

        // Skip all gas rendering when the calculated level occupies no pixels.
        if (fill > 0)
        {
            // Locate the fill's top edge; subtracting fill makes the gas rise upward from the bottom.
            int fillTop = y + 1 + METER_INNER_HEIGHT - fill;

            // Apply an optional extra horizontal inset to both the texture and its highlight strip.
            int fillInset = 0;

            // Set the texture's inclusive left edge; increasing this value narrows it from the left.
            int fillLeft = x + fillInset + 1;

            // Set the texture's exclusive right edge; increasing the trailing 2 narrows it from the right.
            int fillRight = x + METER_WIDTH - 2;

            // Set the texture's exclusive bottom edge; decreasing the trailing 1 raises that edge.
            int fillBottom = y + METER_HEIGHT - 1;

            // Tile and crop the gas-bubble image within the calculated fill rectangle.
            renderTiledGas(g, fillLeft, fillTop, fillRight, fillBottom);

            // Draw the lighter highlight over the texture; its first and third arguments set its X bounds.
            g.fill(x + fillInset + 2, fillTop, x + 4, y + METER_HEIGHT - 1, 0xFF8BAFF6);
        }
    }

    /** Draws animated fluid-sprite tiles, clipping edge tiles and anchoring the pattern to the bottom. */
    @SuppressWarnings("null")
    private void renderTiledGas(GuiGraphicsExtractor g, int left, int top, int right, int bottom)
    {
        // 26.1.2: the sprite comes from the lifting gas fluid model (block/gas_bubbles, registered in MCTradePostMod).
        TextureAtlasSprite gasSprite = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
            .get(MCTPFluids.LIFTING_GAS.get().defaultFluidState()).stillMaterial().sprite();

        g.enableScissor(left, top, right, bottom);
        try
        {
            for (int tileBottom = bottom; tileBottom > top; tileBottom -= GAS_TEXTURE_SIZE)
            {
                int tileY = tileBottom - GAS_TEXTURE_SIZE;
                for (int tileX = left; tileX < right; tileX += GAS_TEXTURE_SIZE)
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, gasSprite, tileX, tileY, GAS_TEXTURE_SIZE, GAS_TEXTURE_SIZE);
            }
        }
        finally
        {
            g.disableScissor();
        }
    }
}
