package com.deathfrog.mctradepost.core.recycling.blacklist;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.core.blacklist.ItemBlacklistRules;
import com.google.gson.JsonElement;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/** Reloadable recycler blacklist backed by the shared item-rule parser. */
public final class RecyclingBlacklistManager extends SimpleJsonResourceReloadListener<ItemBlacklistRules.RuleFile>
{
    public static final String RECYCLING_BLACKLIST_FOLDER = "recycling_blacklist";
    private static final ItemBlacklistRules RULES = new ItemBlacklistRules("recycling");

    /** Creates the server resource reload listener for recycler rules. */
    public RecyclingBlacklistManager()
    {
        super(ItemBlacklistRules.RuleFile.CODEC, FileToIdConverter.json(RECYCLING_BLACKLIST_FOLDER));
    }

    /**
     * Rebuilds recycler rules after datapack resources are prepared.
     *
     * @param dataMap blacklist rule files, already decoded
     * @param resourceManager active resource manager
     * @param profiler reload profiler
     */
    @Override
    protected void apply(@Nonnull final Map<Identifier, ItemBlacklistRules.RuleFile> dataMap,
        @Nonnull final ResourceManager resourceManager, @Nonnull final ProfilerFiller profiler)
    {
        RULES.load(dataMap);
    }

    /**
     * Registers the recycler listener with the server resource reload.
     *
     * @param event reload-listener registration event
     */
    @SubscribeEvent
    public static void listenForRecyclingBlacklistRecords(@Nonnull final AddServerReloadListenersEvent event)
    {
        event.addListener(Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, RECYCLING_BLACKLIST_FOLDER), new RecyclingBlacklistManager());
    }

    /**
     * Determines whether a stack is denied by the recycler rules.
     *
     * @param stack item stack to evaluate
     * @param level active level, when required by a matcher
     * @return whether the stack is blacklisted
     */
    public static boolean isBlacklisted(@Nonnull final ItemStack stack, @Nullable final Level level)
    {
        return RULES.isBlacklisted(stack, level);
    }

    /**
     * Finds the effective recycler deny rule.
     *
     * @param stack item stack to evaluate
     * @param level active level, when required by a matcher
     * @return matching deny rule, or {@code null} when allowed
     */
    @Nullable
    public static ItemBlacklistRules.CompiledRule findMatch(@Nonnull final ItemStack stack, @Nullable final Level level)
    {
        return RULES.findMatch(stack, level);
    }
}
