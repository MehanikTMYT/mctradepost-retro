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
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Persistent per-dimension index of placed trade docks. */
public final class TradeDockRegistry extends SavedData
{
    private static final String TAG_DOCKS = "docks";
    private final Set<BlockPos> docks = new HashSet<>();

    @SuppressWarnings("null")
    public static final @Nonnull SavedDataType<TradeDockRegistry> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "trade_docks"),
        TradeDockRegistry::new,
        codec());

    @SuppressWarnings("null")
    public static TradeDockRegistry get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void add(BlockPos pos)
    {
        if (docks.add(pos.immutable())) setDirty();
    }

    public void remove(BlockPos pos)
    {
        if (docks.remove(pos)) setDirty();
    }

    public Set<BlockPos> docks()
    {
        return Collections.unmodifiableSet(docks);
    }

    @SuppressWarnings("null")
    private static Codec<TradeDockRegistry> codec()
    {
        return CompoundTag.CODEC.xmap(TradeDockRegistry::load, TradeDockRegistry::save);
    }

    private static TradeDockRegistry load(CompoundTag tag)
    {
        TradeDockRegistry data = new TradeDockRegistry();
        ListTag list = tag.getListOrEmpty(TAG_DOCKS);
        for (int i = 0; i < list.size(); i++) data.docks.add(BlockPos.of(((LongTag) list.get(i)).value()));
        return data;
    }

    private CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (BlockPos pos : docks) list.add(LongTag.valueOf(pos.asLong()));
        tag.put(TAG_DOCKS, list);
        return tag;
    }
}
