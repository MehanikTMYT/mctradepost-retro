package com.deathfrog.mctradepost.compat.emi.recipe;

import java.util.List;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.compat.emi.category.MCTPRitualEmiCategory;
import com.deathfrog.mctradepost.core.event.wishingwell.ritual.RitualDefinitionHelper;
import com.deathfrog.mctradepost.core.event.wishingwell.ritual.RitualManager;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * EMI recipe for a single wishing-well ritual definition. Ports the layout of the legacy
 * {@code RitualCategory} (JEI): coin + companion item inputs, the Mixed Stone wishing well as a
 * non-consumed catalyst, an optional transform-result output, and a short description.
 */
public class MCTPRitualEmiRecipe implements EmiRecipe
{
    private static final int WIDTH = 170;
    private static final int HEIGHT = 64;
    private static final int TEXT_X = 55;
    private static final int WISHING_WELL_SIZE = 50;

    private final MCTPRitualEmiCategory category;
    private final Identifier id;
    private final RitualDefinitionHelper recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiIngredient> catalysts;

    public MCTPRitualEmiRecipe(final MCTPRitualEmiCategory category, final RitualDefinitionHelper recipe)
    {
        this.category = category;
        this.recipe = recipe;
        this.id = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "rituals/" + recipe.getFileName());

        final Item coinItem = recipe.getCoinAsItem();
        final EmiStack coin = coinItem == null ? EmiStack.EMPTY : EmiStack.of(coinItem, recipe.requiredCoins());

        final Item companionItem = BuiltInRegistries.ITEM.getValue(recipe.companionItem());
        final EmiStack companion = companionItem == null ? EmiStack.EMPTY : EmiStack.of(companionItem, recipe.companionItemCount());

        this.inputs = List.of(coin, companion);

        if (RitualManager.RITUAL_EFFECT_TRANSFORM.equals(recipe.effect()))
        {
            final Item targetItem = recipe.getTargetAsItem();
            this.outputs = targetItem == null ? List.of() : List.of(EmiStack.of(targetItem, recipe.companionItemCount()));
        }
        else
        {
            this.outputs = List.of();
        }

        final Block mixedStone = MCTradePostMod.MIXED_STONE.get();
        this.catalysts = mixedStone == null ? List.of() : List.of(EmiStack.of(mixedStone));
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return category;
    }

    @Override
    public Identifier getId()
    {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs()
    {
        return inputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts()
    {
        return catalysts;
    }

    @Override
    public List<EmiStack> getOutputs()
    {
        return outputs;
    }

    @Override
    public int getDisplayWidth()
    {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight()
    {
        return HEIGHT;
    }

    /**
     * A ritual with no transform output (slay/summon/weather/community/outpost effects) is not a
     * set of inputs producing outputs in the recipe-tree sense.
     */
    @Override
    public boolean supportsRecipeTree()
    {
        return !outputs.isEmpty();
    }

    @Override
    public void addWidgets(final WidgetHolder widgets)
    {
        if (!catalysts.isEmpty())
        {
            widgets.addSlot(catalysts.get(0), 4, 20)
                .drawBack(false)
                .catalyst(true)
                .appendTooltip(Component.translatable("emi.mctradepost.tooltip.wishingwell"));
        }

        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addSlot(inputs.get(1), 34, 0);

        if (!outputs.isEmpty())
        {
            widgets.addSlot(outputs.get(0), 34, 34).large(true).recipeContext(this);
        }

        final Identifier wishingWellTexture = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "textures/rituals/wishingwell.png");
        widgets.addTexture(wishingWellTexture, 0, 0, WISHING_WELL_SIZE, WISHING_WELL_SIZE, 0, 0, WISHING_WELL_SIZE, WISHING_WELL_SIZE,
            WISHING_WELL_SIZE, WISHING_WELL_SIZE);

        addWrappedText(widgets, recipe.describeComponent(), TEXT_X, 0, WIDTH - TEXT_X, 0xFF000000);
    }

    /**
     * EMI's {@code TextWidget} draws a single line; wrap the ritual's description into as many
     * lines as fit the available width ourselves, one widget per line.
     */
    private static void addWrappedText(final WidgetHolder widgets, final Component text, final int x, final int y, final int maxWidth,
        final int color)
    {
        final var font = Minecraft.getInstance().font;
        final List<FormattedCharSequence> lines = font.split(text, maxWidth);
        for (int i = 0; i < lines.size(); i++)
        {
            widgets.add(new dev.emi.emi.api.widget.TextWidget(lines.get(i), x, y + i * font.lineHeight, color, false));
        }
    }
}
