package com.deathfrog.mctradepost.recipe;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class DeconstructionRecipe implements Recipe<SingleRecipeInput>
{
    public static final String DECON_RECIPE_KEY = "deconstruction";

    /**
     * Uses {@link ItemStackTemplate} rather than a raw {@link ItemStack} for the output: as of 26.1.2,
     * {@code ItemStack.CODEC}'s "id" field goes through {@code Item.CODEC_WITH_BOUND_COMPONENTS}, which
     * validates that the item's default component map is already bound -- that isn't true yet at the
     * point recipe JSON gets parsed during a datapack reload, so any recipe using it (even for plain
     * vanilla items) fails with "Item X does not have components yet". {@code ItemStackTemplate} (the
     * same type this mod's other recipe results already use) only needs the item's registry id at parse
     * time and materializes lazily via {@link ItemStackTemplate#create()}, sidestepping the problem.
     */
    public record Output(ItemStackTemplate template, float chance)
    {
        public ItemStack stack()
        {
            return template.create();
        }

        @SuppressWarnings("null")
        public static final MapCodec<Output> CODEC =
            RecordCodecBuilder.mapCodec(i -> i
                .group(ItemStackTemplate.CODEC.fieldOf("item").forGetter(Output::template),
                    com.mojang.serialization.Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(Output::chance))
                .apply(i, Output::new));

        @SuppressWarnings("null")
        public static final StreamCodec<RegistryFriendlyByteBuf, Output> STREAM_CODEC =
            StreamCodec.composite(
                ItemStackTemplate.STREAM_CODEC, Output::template,
                ByteBufCodecs.FLOAT, Output::chance,
                Output::new
            );
    }

    private final Ingredient input;
    private final ObjectArrayList<Output> outputs;

    public DeconstructionRecipe(Ingredient input, ObjectArrayList<Output> outputs)
    {
        this.input = input;
        this.outputs = outputs;
    }

    @Override
    public boolean matches(@Nonnull SingleRecipeInput in, @Nonnull Level level)
    {
        // SingleRecipeInput is a record SingleRecipeInput(ItemStack item)
        return !in.item().isEmpty() && input.test(in.item());
    }

    @Override
    public ItemStack assemble(@Nonnull SingleRecipeInput in)
    {
        return ItemStack.EMPTY; // your machine reads outputs directly
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
        return PlacementInfo.create(input);
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer()
    {
        return MCTradePostMod.DECON_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType()
    {
        return MCTradePostMod.DECON_RECIPE_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory()
    {
        // Not a player-facing crafting-table recipe (a custom machine reads the outputs directly);
        // mirrors com.minecolonies.api.crafting.CompostRecipe's use of CRAFTING_MISC as a generic fallback.
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public Ingredient getInput()
    {
        return input;
    }

    public ObjectArrayList<Output> getOutputs()
    {
        return outputs;
    }

    /** RecipeSerializer is a record (MapCodec + StreamCodec) as of Minecraft 26.1.2; this holds the shared instance. */
    public static final class Serializer
    {
        @SuppressWarnings("null")
        public static final MapCodec<DeconstructionRecipe> CODEC = RecordCodecBuilder.mapCodec(
            i -> i
                .group(Ingredient.CODEC.fieldOf("input").forGetter(DeconstructionRecipe::getInput),
                    Output.CODEC.codec()
                        .listOf()
                        .fieldOf("results")
                        .xmap(list -> new ObjectArrayList<>(list), list -> list)
                        .forGetter(DeconstructionRecipe::getOutputs))
                .apply(i, DeconstructionRecipe::new));

        @SuppressWarnings("null")
        public static final StreamCodec<RegistryFriendlyByteBuf, DeconstructionRecipe> STREAM_CODEC =
            StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, DeconstructionRecipe::getInput,
                ByteBufCodecs.collection(ObjectArrayList::new, Output.STREAM_CODEC), DeconstructionRecipe::getOutputs,
                DeconstructionRecipe::new
            );

        public static final RecipeSerializer<DeconstructionRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {}
    }
}
