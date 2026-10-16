package com.deathfrog.mctradepost.core.event.wishingwell.resurrection;

import net.minecraft.core.UUIDUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.MCTradePostMod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Persistent, authoritative store of burials made available by the MineColonies burial event. */
public final class BuriedCitizenRegistry extends SavedData
{
    @SuppressWarnings("null")
    public static final @Nonnull SavedDataType<BuriedCitizenRegistry> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "buried_citizens"),
        BuriedCitizenRegistry::new,
        CompoundTag.CODEC.xmap(BuriedCitizenRegistry::load, BuriedCitizenRegistry::save));

    public record Burial(UUID id, int colonyId, BlockPos gravePos, CompoundTag citizenNbt,
        String citizenName, String jobName, int burialDay) {}

    private final Map<UUID, Burial> burials = new HashMap<>();

    public static BuriedCitizenRegistry get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Burial add(int colonyId, BlockPos gravePos, CompoundTag citizenNbt, String citizenName,
        String jobName, int burialDay)
    {
        burials.values().removeIf(entry -> entry.colonyId() == colonyId && entry.gravePos().equals(gravePos));
        Burial burial = new Burial(UUID.randomUUID(), colonyId, gravePos.immutable(), citizenNbt.copy(),
            citizenName, jobName == null ? "" : jobName, burialDay);
        burials.put(burial.id(), burial);
        setDirty();
        return burial;
    }

    public Optional<Burial> find(BlockPos gravePos)
    {
        return burials.values().stream().filter(entry -> entry.gravePos().equals(gravePos)).findFirst();
    }

    public Optional<Burial> get(UUID id)
    {
        return Optional.ofNullable(burials.get(id));
    }

    public Optional<Burial> remove(UUID id)
    {
        Burial removed = burials.remove(id);
        if (removed != null) setDirty();
        return Optional.ofNullable(removed);
    }

    public void restore(Burial burial)
    {
        burials.put(burial.id(), burial);
        setDirty();
    }

    private static BuriedCitizenRegistry load(CompoundTag tag)
    {
        BuriedCitizenRegistry registry = new BuriedCitizenRegistry();
        ListTag list = tag.getListOrEmpty("burials");
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            Burial burial = new Burial(entry.read("id", UUIDUtil.CODEC).orElseThrow(), entry.getIntOr("colony", 0),
                BlockPos.of(entry.getLongOr("pos", 0L)), entry.getCompoundOrEmpty("citizen").copy(),
                entry.getStringOr("name", ""), entry.getStringOr("job", ""), entry.getIntOr("day", 0));
            registry.burials.put(burial.id(), burial);
        }
        return registry;
    }

    @SuppressWarnings("null")
    private @Nonnull CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Burial burial : burials.values())
        {
            CompoundTag entry = new CompoundTag();
            entry.store("id", UUIDUtil.CODEC, burial.id());
            entry.putInt("colony", burial.colonyId());
            entry.putLong("pos", burial.gravePos().asLong());
            entry.put("citizen", burial.citizenNbt().copy());
            entry.putString("name", burial.citizenName());
            entry.putString("job", burial.jobName());
            entry.putInt("day", burial.burialDay());
            list.add(entry);
        }
        tag.put("burials", list);
        return tag;
    }
}
