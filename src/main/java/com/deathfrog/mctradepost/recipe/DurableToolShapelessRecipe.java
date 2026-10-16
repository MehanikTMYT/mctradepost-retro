package com.deathfrog.mctradepost.recipe;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.item.IDamageableCraftingTool;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A shapeless crafting recipe that is otherwise identical to vanilla's {@code minecraft:crafting_shapeless}, except
 * that any ingredient slot whose item implements {@link IDamageableCraftingTool} produces that tool's damaged
 * remainder (or nothing, once broken) instead of being consumed outright.
 * <p>
 * {@code Item.hasCraftingRemainingItem()}/{@code getCraftingRemainingItem(ItemStack)} were removed in 26.1.2; the
 * game now asks the *recipe* for remainders via {@link CraftingRecipe#getRemainingItems(CraftingInput)}, which
 * defaults to each item's static {@code Item.getCraftingRemainder()} template. That default can't express "the
 * same stack, damaged by one, preserving its components" -- only "swap in a fixed different item" -- so tools
 * needing dynamic wear use this recipe type instead. Standard recipe contract otherwise: {@link #getRemainingItems}
 * is consulted by every crafting-grid/menu/automation path that goes through {@link CraftingRecipe}, not just the
 * player crafting table, so no separate automation-specific handling is needed.
 */
public class DurableToolShapelessRecipe implements CraftingRecipe
{
    public static final String DURABLE_TOOL_SHAPELESS_RECIPE_KEY = "durable_tool_shapeless";

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;

    public DurableToolShapelessRecipe(final ItemStackTemplate result, final List<Ingredient> ingredients)
    {
        this.result = result;
        this.ingredients = List.copyOf(ingredients);
    }

    public ItemStackTemplate getResult()
    {
        return result;
    }

    public List<Ingredient> getIngredients()
    {
        return ingredients;
    }

    @Override
    public boolean matches(@Nonnull final CraftingInput input, @Nonnull final Level level)
    {
        if (input.ingredientCount() != ingredients.size())
        {
            return false;
        }

        return input.size() == 1 && ingredients.size() == 1
            ? ingredients.getFirst().test(input.getItem(0))
            : input.stackedContents().canCraft(this, null);
    }

    @Override
    public ItemStack assemble(@Nonnull final CraftingInput input)
    {
        return result.create();
    }

    /**
     * Per-slot remainders: {@link IDamageableCraftingTool} items wear down instead of being consumed; every other
     * slot falls back to the same static-template lookup {@link CraftingRecipe#defaultCraftingReminder} uses.
     */
    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@Nonnull final CraftingInput input)
    {
        final NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int slot = 0; slot < remainingItems.size(); slot++)
        {
            final ItemStack stack = input.getItem(slot);
            if (stack.isEmpty())
            {
                continue;
            }

            final Item item = stack.getItem();
            if (item instanceof IDamageableCraftingTool tool)
            {
                remainingItems.set(slot, tool.computeCraftingRemainder(stack));
                continue;
            }

            final ItemStackTemplate staticRemainder = item.getCraftingRemainder();
            if (staticRemainder != null)
            {
                remainingItems.set(slot, staticRemainder.create());
            }
        }

        return remainingItems;
    }

    @Override
    public boolean showNotification()
    {
        return true;
    }

    @Nonnull
    @Override
    public String group()
    {
        return "";
    }

    @Override
    public PlacementInfo placementInfo()
    {
        return PlacementInfo.create(ingredients);
    }

    @Override
    public RecipeSerializer<? extends CraftingRecipe> getSerializer()
    {
        return MCTradePostMod.DURABLE_TOOL_SHAPELESS_SERIALIZER.get();
    }

    @Override
    public boolean isSpecial()
    {
        return false;
    }

    @Override
    public CraftingBookCategory category()
    {
        return CraftingBookCategory.MISC;
    }

    /** RecipeSerializer is a record (MapCodec + StreamCodec) as of Minecraft 26.1.2; this holds the shared instance. */
    public static final class Serializer
    {
        @SuppressWarnings("null")
        public static final MapCodec<DurableToolShapelessRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(DurableToolShapelessRecipe::getResult),
                    Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(DurableToolShapelessRecipe::getIngredients)
                ).apply(instance, DurableToolShapelessRecipe::new)
            );

        @SuppressWarnings("null")
        public static final StreamCodec<RegistryFriendlyByteBuf, DurableToolShapelessRecipe> STREAM_CODEC =
            StreamCodec.composite(
                ItemStackTemplate.STREAM_CODEC, DurableToolShapelessRecipe::getResult,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), DurableToolShapelessRecipe::getIngredients,
                DurableToolShapelessRecipe::new
            );

        public static final RecipeSerializer<DurableToolShapelessRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {}
    }
}
