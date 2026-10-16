package com.deathfrog.mctradepost.recipe;

import javax.annotation.Nonnull;
import com.deathfrog.mctradepost.MCTradePostMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** Defines a feedstock that the Gasifier converts into Lifting Gas. */
public record GasifierRecipe(Ingredient input, int burnTime, int gasYield) implements Recipe<SingleRecipeInput>
{
    public static final String ID = "gasifying";

    @Override
    public boolean matches(@Nonnull SingleRecipeInput input, @Nonnull Level level)
    {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(@Nonnull SingleRecipeInput input)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification()
    {
        return false;
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
        return PlacementInfo.create(input);
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer()
    {
        return MCTradePostMod.GASIFIER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType()
    {
        return MCTradePostMod.GASIFIER_RECIPE_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory()
    {
        // Machine recipe, not shown in the recipe book (same fallback as DeconstructionRecipe).
        return RecipeBookCategories.CRAFTING_MISC;
    }

    /** Serializes datapack and network forms of Gasifier recipes. RecipeSerializer is a record as of 26.1.2. */
    public static final class Serializer
    {
        @SuppressWarnings("null")
        private static final MapCodec<GasifierRecipe> CODEC = RecordCodecBuilder.mapCodec(
            i -> i
                .group(Ingredient.CODEC.fieldOf("input").forGetter(GasifierRecipe::input),
                    Codec.intRange(1, 1_000_000).fieldOf("burn_time").forGetter(GasifierRecipe::burnTime),
                    Codec.intRange(1, 10_000_000).fieldOf("gas").forGetter(GasifierRecipe::gasYield))
                .apply(i, GasifierRecipe::new));

        @SuppressWarnings("null")
        private static final StreamCodec<RegistryFriendlyByteBuf, GasifierRecipe> STREAM_CODEC =
            StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC,
                GasifierRecipe::input,
                ByteBufCodecs.VAR_INT,
                GasifierRecipe::burnTime,
                ByteBufCodecs.VAR_INT,
                GasifierRecipe::gasYield,
                GasifierRecipe::new);

        public static final RecipeSerializer<GasifierRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {}
    }
}
