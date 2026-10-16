package com.deathfrog.mctradepost.compat.emi.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.compat.emi.category.MCTPPetForagingEmiCategory;
import com.deathfrog.mctradepost.core.entity.pets.scavenge.PetForagingJeiEntry;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/**
 * EMI recipe for a single pet-foraging display entry. Ports the layout of the legacy
 * {@code PetForagingCategory} (JEI): work-location catalyst, source block input, up to 10 possible
 * outputs, and short role/note text.
 * <p>
 * These are not real crafting recipes -- they are server-derived display entries synced from
 * {@link com.deathfrog.mctradepost.core.entity.pets.scavenge.PetForagingJeiCache}, exactly as they
 * were for JEI. The source block is shown as its plain item icon rather than the legacy JEI
 * {@code BlockStateIngredientRenderer}'s rotated 3D "mature" block-state render: the block's
 * identity, tooltip and R/U indexing are unaffected, and this mod-agnostic (non-Trade-Post-specific)
 * a block-state-aware EMI ingredient renderer does not yet exist anywhere in this modpack's EMI
 * stack -- see the migration report for the full rationale.
 * </p>
 */
public class MCTPPetForagingEmiRecipe implements EmiRecipe
{
    private static final int WIDTH = 170;
    private static final int HEIGHT = 64;
    private static final int MAX_OUTPUT_SLOTS = 10;

    private final MCTPPetForagingEmiCategory category;
    private final PetForagingJeiEntry entry;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiIngredient> catalysts;

    public MCTPPetForagingEmiRecipe(final MCTPPetForagingEmiCategory category, final PetForagingJeiEntry entry)
    {
        this.category = category;
        this.entry = entry;

        final Block sourceBlock = BuiltInRegistries.BLOCK.getValue(entry.sourceBlock());
        this.inputs = List.of(EmiStack.of(sourceBlock));

        final List<EmiStack> outputList = new ArrayList<>();
        final List<net.minecraft.world.item.ItemStack> rawOutputs = entry.outputs();
        for (int i = 0; i < Math.min(rawOutputs.size(), MAX_OUTPUT_SLOTS); i++)
        {
            outputList.add(EmiStack.of(rawOutputs.get(i)));
        }
        this.outputs = List.copyOf(outputList);

        final Block workLocationBlock = BuiltInRegistries.BLOCK.getValue(entry.workLocationBlock());
        this.catalysts = List.of(EmiStack.of(workLocationBlock));
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return category;
    }

    @Override
    public Identifier getId()
    {
        return entry.id();
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

    @Override
    public void addWidgets(final WidgetHolder widgets)
    {
        if (!catalysts.isEmpty())
        {
            widgets.addSlot(catalysts.get(0), 4, 18).catalyst(true);
        }

        widgets.addSlot(inputs.get(0), 28, 18)
            .appendTooltip(Component.translatable("emi.mctradepost.pet_foraging.tooltip.source", sourceBlockName()))
            .appendTooltip(Component.literal(entry.sourceBlock().toString()).withStyle(ChatFormatting.DARK_GRAY));

        widgets.addTexture(dev.emi.emi.api.render.EmiTexture.EMPTY_ARROW, 52, 18);

        for (int i = 0; i < outputs.size(); i++)
        {
            widgets.addSlot(outputs.get(i), 74 + (i % 5) * 18, 9 + (i / 5) * 18).recipeContext(this);
        }

        widgets.addText(Component.translatable(roleKey()), 4, 0, ChatFormatting.DARK_GRAY.getColor(), false);
        widgets.addText(Component.translatable(entry.noteKey()), 4, 50, ChatFormatting.GRAY.getColor(), false);
    }

    private Component sourceBlockName()
    {
        final Block sourceBlock = BuiltInRegistries.BLOCK.getValue(entry.sourceBlock());
        return sourceBlock.getName();
    }

    private String roleKey()
    {
        return "emi.mctradepost.pet_foraging.role." + entry.role().name().toLowerCase(Locale.ROOT);
    }
}
