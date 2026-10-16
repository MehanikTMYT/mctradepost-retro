package com.deathfrog.mctradepost.core.colony.buildings.modules.thriftshop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.core.colony.buildings.modules.thriftshop.MarketDailyRoller.MarketTier;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.VillagerTradeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class WanderingTraderSource
{
    public static ItemStack rollTradeOutput(@Nonnull ServerLevel level, RandomSource rand, MarketTier tier)
    {
        // Villager/wandering-trader trades became data-driven registry entries in Minecraft 26.1: there
        // is no more static VillagerTrades.WANDERING_TRADER_TRADES map of ItemListing[] to pull from.
        // Instead, pick a random entry from the wandering_trader/common or wandering_trader/uncommon
        // VillagerTradeTags (mirrors the old tier 1 / tier 2 split) and resolve it via VillagerTrade#getOffer,
        // following the same LootContext construction AbstractVillager itself uses.
        final TagKey<VillagerTrade> tag = switch (tier)
        {
            case TIER1_COMMON, TIER2_UNCOMMON -> VillagerTradeTags.WANDERING_TRADER_COMMON;
            case TIER3_RARE, TIER4_EPIC -> VillagerTradeTags.WANDERING_TRADER_UNCOMMON;
        };

        final List<Holder<VillagerTrade>> candidates = new ArrayList<>();
        level.registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE).getTagOrEmpty(tag).forEach(candidates::add);

        if (candidates.isEmpty())
            return ItemStack.EMPTY;

        final WanderingTrader trader = EntityType.WANDERING_TRADER.create(level, EntitySpawnReason.TRIGGERED);
        if (trader == null)
            return ItemStack.EMPTY;

        final LootContext lootContext = new LootContext.Builder(
            new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, trader.position())
                .withParameter(LootContextParams.THIS_ENTITY, trader)
                .withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
                .create(LootContextParamSets.VILLAGER_TRADE)
        ).create(Optional.empty());

        final Holder<VillagerTrade> chosen = candidates.get(rand.nextInt(candidates.size()));
        final MerchantOffer offer = chosen.value().getOffer(lootContext);

        if (offer == null)
            return ItemStack.EMPTY;

        final ItemStack result = offer.getResult();
        return result == null ? ItemStack.EMPTY : result.copy();
    }
}
