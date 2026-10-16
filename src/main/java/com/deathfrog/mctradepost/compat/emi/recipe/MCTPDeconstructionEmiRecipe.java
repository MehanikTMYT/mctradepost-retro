package com.deathfrog.mctradepost.compat.emi.recipe;

import java.util.ArrayList;
import java.util.List;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.compat.emi.category.MCTPDeconstructionEmiCategory;
import com.deathfrog.mctradepost.recipe.DeconstructionRecipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * EMI recipe for a single {@link DeconstructionRecipe}: one input, one or more chance-weighted
 * outputs. Outputs keep their {@code ItemStackTemplate}-derived components (materialized once, at
 * construction time, via {@link DeconstructionRecipe.Output#stack()}) instead of being flattened to
 * bare items.
 */
public class MCTPDeconstructionEmiRecipe implements EmiRecipe
{
    private static final int MAX_OUTPUT_SLOTS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;
    private static final int ARROW_GAP = 22;
    private static final int MAX_PER_ROW = 3;

    private final MCTPDeconstructionEmiCategory category;
    private final Identifier id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public MCTPDeconstructionEmiRecipe(final MCTPDeconstructionEmiCategory category, final RecipeHolder<DeconstructionRecipe> holder)
    {
        this.category = category;
        this.id = holder.id().identifier();

        final DeconstructionRecipe recipe = holder.value();
        this.inputs = List.of(EmiIngredient.of(recipe.getInput()));

        final List<EmiStack> outputList = new ArrayList<>();
        for (final DeconstructionRecipe.Output output : recipe.getOutputs())
        {
            if (outputList.size() >= MAX_OUTPUT_SLOTS)
            {
                break;
            }
            outputList.add(EmiStack.of(output.stack()).setChance(output.chance()));
        }
        this.outputs = List.copyOf(outputList);
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
    public List<EmiStack> getOutputs()
    {
        return outputs;
    }

    @Override
    public int getDisplayWidth()
    {
        final int outCols = Math.min(MAX_PER_ROW, Math.max(1, outputs.size()));
        return SLOT_SIZE + ARROW_GAP + outCols * (SLOT_SIZE + SLOT_GAP);
    }

    @Override
    public int getDisplayHeight()
    {
        final int outRows = Math.max(1, (outputs.size() + MAX_PER_ROW - 1) / MAX_PER_ROW);
        return outRows * (SLOT_SIZE + SLOT_GAP);
    }

    @Override
    public void addWidgets(final WidgetHolder widgets)
    {
        widgets.addSlot(inputs.get(0), 0, 0);

        final int outX = SLOT_SIZE + ARROW_GAP;
        for (int i = 0; i < outputs.size(); i++)
        {
            final int col = i % MAX_PER_ROW;
            final int row = i / MAX_PER_ROW;
            widgets.addSlot(outputs.get(i), outX + col * (SLOT_SIZE + SLOT_GAP), row * (SLOT_SIZE + SLOT_GAP))
                .recipeContext(this);
        }
    }
}
