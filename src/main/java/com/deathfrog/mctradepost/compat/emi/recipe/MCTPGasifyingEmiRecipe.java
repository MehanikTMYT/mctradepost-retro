package com.deathfrog.mctradepost.compat.emi.recipe;

import java.util.List;

import com.deathfrog.mctradepost.compat.emi.category.MCTPGasifyingEmiCategory;
import com.deathfrog.mctradepost.core.fluids.MCTPFluids;
import com.deathfrog.mctradepost.recipe.GasifierRecipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * One Gasifier recipe: input item -> lifting gas, with the burn time as the arrow animation
 * (layout follows upstream's JEI GasifierCategory, 170x44).
 */
public class MCTPGasifyingEmiRecipe implements EmiRecipe
{
    private static final int WIDTH = 170;
    private static final int HEIGHT = 44;

    private final MCTPGasifyingEmiCategory category;
    private final Identifier id;
    private final GasifierRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public MCTPGasifyingEmiRecipe(final MCTPGasifyingEmiCategory category, final RecipeHolder<GasifierRecipe> holder)
    {
        this.category = category;
        this.id = holder.id().identifier();
        this.recipe = holder.value();
        this.inputs = List.of(EmiIngredient.of(recipe.input()));
        this.outputs = List.of(EmiStack.of(MCTPFluids.LIFTING_GAS.get(), recipe.gasYield()));
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
        return WIDTH;
    }

    @Override
    public int getDisplayHeight()
    {
        return HEIGHT;
    }

    @Override
    public void addWidgets(final WidgetHolder widgets)
    {
        widgets.addSlot(inputs.get(0), 11, 9);
        widgets.addFillingArrow(73, 10, Math.max(1, recipe.burnTime()) * 50);
        widgets.addSlot(outputs.get(0), 139, 9).recipeContext(this);
        widgets.addText(Component.translatable("jei.mctradepost.gasifying.output", recipe.gasYield()), 0, 33,
            ChatFormatting.DARK_GRAY.getColor(), false);
    }
}
