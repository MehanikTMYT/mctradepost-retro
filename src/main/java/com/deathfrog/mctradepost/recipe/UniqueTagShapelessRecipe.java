package com.deathfrog.mctradepost.recipe;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.api.util.NullnessBridge;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Crafting-table recipe:
 *  - requires exactly 1 stack matching "base"
 *  - requires exactly N stacks matching "tag"
 *  - the N tag stacks must be unique by item id (no duplicates)
 *  - no other items allowed in the grid
 */
public class UniqueTagShapelessRecipe implements CraftingRecipe
{
    public static final String UNIQUE_TAG_SHAPELESS_RECIPE_KEY = "unique_tag_shapeless";

    private final Ingredient base;
    private final TagKey<Item> tag;
    private final int count;
    // ItemStackTemplate, not a raw ItemStack: ItemStack.CODEC's "id" field goes through
    // Item.CODEC_WITH_BOUND_COMPONENTS, which requires the item's default component map to already be
    // bound -- not true yet when this recipe's JSON is parsed during a datapack reload. ItemStackTemplate
    // only needs the registry id at parse time and materializes lazily via create().
    private final ItemStackTemplate result;

    public UniqueTagShapelessRecipe(final Ingredient base, final TagKey<Item> tag, final int count, final ItemStackTemplate result)
    {
        this.base = base;
        this.tag = tag;
        this.count = count;
        this.result = result;
    }

    public Ingredient getBase() { return base; }
    public TagKey<Item> getTag() { return tag; }
    public int getCount() { return count; }
    public ItemStackTemplate getResult() { return result; }
    public ItemStack getResultStack() { return result.create(); }

    @Override
    public boolean matches(@Nonnull final CraftingInput in, @Nonnull final Level level)
    {
        TagKey<Item> localTag = tag;

        if (localTag == null) return false;

        if (level.isClientSide())
        {
            // Optional: can still validate client-side if you want previews exact.
            // Leaving it enabled is fine; returning true here can cause ghost matches.
        }

        int baseFound = 0;
        int tagFound = 0;

        // Track uniqueness by item ID
        final ObjectOpenHashSet<Identifier> uniqueItemIds = new ObjectOpenHashSet<>();

        for (int i = 0; i < in.size(); i++)
        {
            final ItemStack stack = in.getItem(i);
            if (stack.isEmpty())
                continue;

            if (base.test(stack))
            {
                baseFound++;
                if (baseFound > 1)
                    return false; // exactly one bowl (or base ingredient)
                continue;
            }

            if (stack.is(localTag))
            {
                tagFound++;

                Item item = stack.getItem();

                if (item == null) continue;

                // uniqueness rule: same exact item cannot appear twice
                final Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (!uniqueItemIds.add(id))
                    return false;

                // early-out if too many
                if (tagFound > count)
                    return false;

                continue;
            }

            // Any other item makes this not match
            return false;
        }

        return baseFound == 1 && tagFound == count;
    }

    @Override
    public ItemStack assemble(@Nonnull final CraftingInput in)
    {
        return result.create();
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
        return PlacementInfo.create(getIngredients());
    }

    @Override
    public RecipeSerializer<? extends CraftingRecipe> getSerializer()
    {
        return MCTradePostMod.UNIQUE_TAG_SHAPELESS_SERIALIZER.get();
    }

    // getType() is no longer declared here: CraftingRecipe#getType() now has a default
    // implementation returning RecipeType.CRAFTING ("keep this as vanilla crafting" still holds).

    /** RecipeSerializer is a record (MapCodec + StreamCodec) as of Minecraft 26.1.2; this holds the shared instance. */
    public static final class Serializer
    {
        // JSON format:
        // {
        //   "type": "mctradepost:unique_tag_shapeless",
        //   "base": { ... Ingredient ... },
        //   "tag": "mctradepost:bar_nut_seeds",
        //   "count": 4,
        //   "result": { "id": "mctradepost:bar_nuts", "count": 8 }
        // }
    @SuppressWarnings("null")
    public static final MapCodec<UniqueTagShapelessRecipe> CODEC =
        RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.fieldOf("base")
                    .forGetter(UniqueTagShapelessRecipe::getBase),

                TagKey.codec(NullnessBridge.assumeNonnull(net.minecraft.core.registries.Registries.ITEM))
                    .fieldOf("tag")
                    .forGetter(UniqueTagShapelessRecipe::getTag),

                com.mojang.serialization.Codec.INT.fieldOf("count")
                    .forGetter(r -> r.getCount()),

                ItemStackTemplate.CODEC.fieldOf("result")
                    .forGetter(UniqueTagShapelessRecipe::getResult)
            ).apply(i, (base, tag, count, result) ->
                new UniqueTagShapelessRecipe(base, tag, count.intValue(), result)
            )
        );

        @SuppressWarnings("null")
        public static final StreamCodec<RegistryFriendlyByteBuf, UniqueTagShapelessRecipe> STREAM_CODEC =
            StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, UniqueTagShapelessRecipe::getBase,
                ByteBufCodecs.fromCodec(TagKey.codec(net.minecraft.core.registries.Registries.ITEM)), UniqueTagShapelessRecipe::getTag,
                ByteBufCodecs.VAR_INT, UniqueTagShapelessRecipe::getCount,
                ItemStackTemplate.STREAM_CODEC, UniqueTagShapelessRecipe::getResult,
                UniqueTagShapelessRecipe::new
            );

        public static final RecipeSerializer<UniqueTagShapelessRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {}
    }

    /**
     * Get the ingredient list.
     * This list will contain the base ingredient, and if the recipe has a tag, it will contain the tag ingredient
     * repeated the number of times specified in the count field.
     * The tag ingredient is built using the ingredient mappings available in the game.
     * @return the ingredient list
     */
    public NonNullList<Ingredient> getIngredients()
    {
        final NonNullList<Ingredient> list = NonNullList.create();
        list.add(base);

        if (tag == null) return list;

        // build an Ingredient that represents the tag for display in JEI/recipe book
        // Ingredient.of(TagKey) no longer exists as of 26.1.2; resolve the tag to a HolderSet first
        // (matches com.minecolonies.api.crafting.ZeroWasteRecipe's BuiltInRegistries...getOrThrow(tag) pattern).
        final Ingredient tagIng = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag));

        for (int i = 0; i < count; i++)
        {
            list.add(tagIng);
        }

        return list;
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
}