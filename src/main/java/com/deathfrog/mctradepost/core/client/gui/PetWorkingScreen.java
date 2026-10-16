package com.deathfrog.mctradepost.core.client.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.api.util.NullnessBridge;
import com.deathfrog.mctradepost.core.inventory.PetWorkingMenu;
import com.deathfrog.mctradepost.core.client.render.ForageAreaOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client screen for a pet working inventory with a side-mounted focus slot.
 */
public class PetWorkingScreen extends AbstractContainerScreen<PetWorkingMenu>
{
    private static final int TOOLTIP_MAX_WIDTH = 220;
    private static final Identifier TEXTURE =
        Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final Identifier FOCUS_SLOT_TEXTURE =
        Identifier.fromNamespaceAndPath("mctradepost", "textures/gui/sniff.png");
    private static final Identifier HIGHLIGHT_OFF_TEXTURE =
        Identifier.fromNamespaceAndPath("mctradepost", "textures/gui/highlight_off.png");
    private static final Identifier HIGHLIGHT_ON_TEXTURE =
        Identifier.fromNamespaceAndPath("mctradepost", "textures/gui/highlight_on.png");
    private ForageAreaCheckbox forageAreaCheckbox;

    /**
     * Creates a working-block screen.
     *
     * @param menu synchronized working-block menu
     * @param inventory viewing player's inventory
     * @param title working-block display title
     */
    public PetWorkingScreen(PetWorkingMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title, 212, 168);
        inventoryLabelY = 74;
    }

    @Override
    protected void init()
    {
        super.init();
        final BlockPos pos = menu.workingPosition();

        Minecraft localMc = minecraft;

        if (pos != null && localMc != null)
        {
            forageAreaCheckbox = addRenderableWidget(new ForageAreaCheckbox(leftPos + 184, topPos + 43,
                ForageAreaOverlay.isEnabled(localMc.level, pos)));
        }
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();

        Minecraft localMinecraft = minecraft;

        if (localMinecraft == null) return;

        if (forageAreaCheckbox != null && forageAreaCheckbox.selected
            && !ForageAreaOverlay.validateFocus(localMinecraft.level, menu.workingPosition(), menu.focusStack()))
        {
            forageAreaCheckbox.selected = false;
        }
    }

    /**
     * Draws the three-row inventory background and the research-sensitive focus
     * slot extension.
     *
     * @param graphics GUI drawing context
     * @param mouseX current mouse X position
     * @param mouseY current mouse Y position
     * @param partialTick partial client tick
     */
    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, NullnessBridge.assumeNonnull(TEXTURE), leftPos, topPos, 0, 0, 176, 71, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, NullnessBridge.assumeNonnull(TEXTURE), leftPos, topPos + 71, 0, 126, 176, 96, 256, 256);
        graphics.fill(leftPos + 176, topPos, leftPos + imageWidth, topPos + 71, 0xffc6c6c6);

        // Match the recessed bevel used by vanilla inventory slots: shadow on
        // the top/left, highlight on the bottom/right, and a gray well.
        graphics.fill(leftPos + 183, topPos + 17, leftPos + 202, topPos + 18, 0xff373737);
        graphics.fill(leftPos + 183, topPos + 17, leftPos + 184, topPos + 36, 0xff373737);
        graphics.fill(leftPos + 183, topPos + 35, leftPos + 202, topPos + 36, 0xffffffff);
        graphics.fill(leftPos + 201, topPos + 17, leftPos + 202, topPos + 36, 0xffffffff);
        graphics.fill(leftPos + 184, topPos + 18, leftPos + 201, topPos + 35,
            menu.isFocusEnabled() ? 0xff8b8b8b : 0xff6b6b6b);
        // RenderPipelines.GUI_TEXTURED already blends (BlendFunction.TRANSLUCENT), so no explicit blend toggle is needed.
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            NullnessBridge.assumeNonnull(FOCUS_SLOT_TEXTURE),
            leftPos + 184,
            topPos + 18,
            0,
            0,
            16,
            16,
            16,
            16,
            ARGB.white(0.75F));

        if (!menu.isFocusEnabled())
        {
            graphics.fill(leftPos + 184, topPos + 18, leftPos + 200, topPos + 34, 0x55000000);
        }
    }

    /**
     * Draws the standard container labels and a centered heading for the
     * dedicated focused-foraging slot.
     *
     * @param graphics GUI drawing context
     * @param mouseX current mouse X position
     * @param mouseY current mouse Y position
     */
    @SuppressWarnings("null")
    @Override
    protected void extractLabels(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        super.extractLabels(graphics, mouseX, mouseY);

        final Component focusTitle = Component.translatable("gui.mctradepost.focused_foraging.title");
        final int sidePanelStart = 176;
        final int sidePanelWidth = imageWidth - sidePanelStart;
        final int titleX = sidePanelStart + (sidePanelWidth - font.width(focusTitle)) / 2;
        graphics.text(font, focusTitle, titleX, 6, 0x404040, false);
    }

    /**
     * Suppresses the default hovered-item tooltip for the focus slot: {@link #extractRenderState}
     * always shows a custom tooltip for that slot instead (including when it is empty, which the
     * default vanilla tooltip never renders for).
     */
    @Override
    protected void extractTooltip(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        final boolean focusSlotHovered = hoveredSlot == menu.slots.get(PetWorkingMenu.FOCUS_SLOT);
        if (!focusSlotHovered)
        {
            super.extractTooltip(graphics, mouseX, mouseY);
        }
    }

    /**
     * Renders the screen and the contextual focus-slot tooltip.
     *
     * @param graphics GUI drawing context
     * @param mouseX current mouse X position
     * @param mouseY current mouse Y position
     * @param partialTick partial client tick
     */
    @SuppressWarnings("null")
    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        final boolean focusSlotHovered = hoveredSlot == menu.slots.get(PetWorkingMenu.FOCUS_SLOT);
        if (focusSlotHovered)
        {
            final List<Component> tooltip;
            if (menu.focusStack().isEmpty())
            {
                tooltip = menu.isFocusEnabled()
                    ? List.of(
                        Component.translatable("gui.mctradepost.focused_foraging.insert.1"),
                        Component.translatable("gui.mctradepost.focused_foraging.insert.2"))
                    : List.of(
                        Component.translatable("gui.mctradepost.focused_foraging.insert.1"),
                        Component.translatable("gui.mctradepost.focused_foraging.insert.2"),
                        Component.translatable("gui.mctradepost.focused_foraging.research_required"));
            }
            else if (menu.isFocusEnabled())
            {
                tooltip = List.of(Component.translatable(
                    "gui.mctradepost.focused_foraging.prefer", menu.focusStack().getHoverName()));
            }
            else
            {
                tooltip = List.of(Component.translatable("gui.mctradepost.focused_foraging.research_required"));
            }
            graphics.setTooltipForNextFrame(font, wrapTooltip(tooltip), mouseX, mouseY);
        }
    }

    /** Wraps tooltip components without discarding formatting or explicit line breaks. */
    @SuppressWarnings("null")
    private List<FormattedCharSequence> wrapTooltip(final List<Component> tooltip)
    {
        final List<FormattedCharSequence> wrapped = new ArrayList<>();
        tooltip.forEach(line -> wrapped.addAll(font.split(line, TOOLTIP_MAX_WIDTH)));
        return wrapped;
    }

    /** Compact image toggle sized for the working block's narrow focus side panel. */
    private final class ForageAreaCheckbox extends AbstractButton
    {
        private boolean selected;

        private ForageAreaCheckbox(int x, int y, boolean selected)
        {
            super(x, y, 18, 18, Component.translatable("gui.mctradepost.forage_area"));
            this.selected = selected;
        }

        @Override
        public void onPress(final InputWithModifiers input)
        {
            Minecraft localMc = minecraft;

            if (localMc != null)
            {
                selected = !selected;
                ForageAreaOverlay.setEnabled(localMc.level, menu.workingPosition(), menu.focusStack(), selected);
            }
        }

        @SuppressWarnings("null")
        @Override
        protected void extractContents(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            graphics.blit(RenderPipelines.GUI_TEXTURED, selected ? HIGHLIGHT_ON_TEXTURE : HIGHLIGHT_OFF_TEXTURE,
                getX(), getY(), 0.0F, 0.0F, width, height, 18, 18);
            if (isHovered())
            {
                graphics.setTooltipForNextFrame(font,
                    wrapTooltip(List.of(Component.translatable("gui.mctradepost.forage_area.tooltip"))),
                    mouseX, mouseY);
            }
        }

        @SuppressWarnings("null")
        @Override
        protected void updateWidgetNarration(@Nonnull NarrationElementOutput output)
        {
            output.add(NarratedElementType.TITLE, createNarrationMessage());
            output.add(NarratedElementType.USAGE,
                Component.translatable("gui.mctradepost.forage_area.tooltip"));
        }
    }
}
