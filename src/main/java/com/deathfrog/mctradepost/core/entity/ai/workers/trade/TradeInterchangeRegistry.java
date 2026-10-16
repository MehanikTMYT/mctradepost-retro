package com.deathfrog.mctradepost.core.entity.ai.workers.trade;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Persistent per-dimension index of Trade Interchange blocks. */
public final class TradeInterchangeRegistry extends SavedData
{
    private static final String TAG_INTERCHANGES = "interchanges";
    private final Set<BlockPos> interchanges = new HashSet<>();

    @SuppressWarnings("null")
    public static final @Nonnull SavedDataType<TradeInterchangeRegistry> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "trade_interchanges"),
        TradeInterchangeRegistry::new,
        codec());

    @SuppressWarnings("null")
    public static TradeInterchangeRegistry get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void add(BlockPos pos)
    {
        if (interchanges.add(pos.immutable())) setDirty();
    }

    public void remove(BlockPos pos)
    {
        if (interchanges.remove(pos)) setDirty();
    }

    public Set<BlockPos> interchanges()
    {
        return Collections.unmodifiableSet(interchanges);
    }

    @SuppressWarnings("null")
    private static Codec<TradeInterchangeRegistry> codec()
    {
        return CompoundTag.CODEC.xmap(TradeInterchangeRegistry::load, TradeInterchangeRegistry::save);
    }

    private static TradeInterchangeRegistry load(CompoundTag tag)
    {
        TradeInterchangeRegistry data = new TradeInterchangeRegistry();
        ListTag list = tag.getListOrEmpty(TAG_INTERCHANGES);
        for (int i = 0; i < list.size(); i++) data.interchanges.add(BlockPos.of(((LongTag) list.get(i)).value()));
        return data;
    }

    private CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (BlockPos pos : interchanges) list.add(LongTag.valueOf(pos.asLong()));
        tag.put(TAG_INTERCHANGES, list);
        return tag;
    }
}
