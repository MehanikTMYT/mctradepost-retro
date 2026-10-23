package com.deathfrog.mctradepost.core.entity.pets.companion;

import net.minecraft.core.UUIDUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import com.deathfrog.mctradepost.MCTradePostMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.resources.Identifier;
import com.deathfrog.mctradepost.MCTradePostMod;

/** Authoritative persistent store for Companion Staff bindings and companion state. */
public final class CompanionRegistry extends SavedData
{
    private static final String DATA_NAME = MCTradePostMod.MODID + "_companions";
    // PORT(26.1): SavedData.Factory -> SavedDataType (codec over the old CompoundTag layout, as MooringBayRegistry).
    @SuppressWarnings("null")
    private static final @Nonnull SavedDataType<CompanionRegistry> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "companions"),
        CompanionRegistry::new,
        CompoundTag.CODEC.xmap(tag -> load(tag, null), data -> data.save(new CompoundTag(), null)));
    private final Map<UUID, CompanionRecord> records = new HashMap<>();

    /** Mutable persisted state for one bound staff. */
    public static final class CompanionRecord
    {
        public UUID bindingId;
        public UUID companionId;
        public int colonyId;
        public String shopDimension;
        public BlockPos shopPosition;
        public boolean active;
        public String activeDimension = "";
        public BlockPos activePosition = BlockPos.ZERO;
        public long cooldownUntil;
        public CompoundTag inventory = new CompoundTag();

        private CompanionRecord(UUID bindingId, UUID companionId, int colonyId, String shopDimension, BlockPos shopPosition)
        {
            this.bindingId = bindingId;
            this.companionId = companionId;
            this.colonyId = colonyId;
            this.shopDimension = shopDimension;
            this.shopPosition = shopPosition.immutable();
        }
    }

    /**
     * Returns the global companion registry stored with the server's Overworld.
     *
     * @param server active server
     * @return persistent companion registry
     */
    public static CompanionRegistry get(@Nonnull MinecraftServer server)
    {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Creates and stores a new staff binding.
     *
     * @param colonyId bound colony ID
     * @param dimension bound shop dimension ID
     * @param shopPosition bound shop building ID
     * @return newly created companion record
     */
    public CompanionRecord create(int colonyId, @Nonnull String dimension, @Nonnull BlockPos shopPosition)
    {
        UUID bindingId = UUID.randomUUID();
        CompanionRecord record = new CompanionRecord(bindingId, UUID.randomUUID(), colonyId, dimension, shopPosition);
        records.put(bindingId, record);
        setDirty();
        return record;
    }

    /**
     * Looks up a companion binding.
     *
     * @param bindingId staff binding ID
     * @return matching record, when present
     */
    public Optional<CompanionRecord> find(@Nonnull UUID bindingId)
    {
        return Optional.ofNullable(records.get(bindingId));
    }

    /** Marks a mutated companion record for persistence. */
    public void changed()
    {
        setDirty();
    }

    /**
     * Permanently removes a companion record.
     *
     * @param bindingId staff binding ID
     * @return removed record, when present
     */
    public Optional<CompanionRecord> remove(@Nonnull UUID bindingId)
    {
        CompanionRecord removed = records.remove(bindingId);
        if (removed != null)
        {
            setDirty();
        }
        return Optional.ofNullable(removed);
    }

    private static CompanionRegistry load(CompoundTag tag, HolderLookup.Provider provider)
    {
        CompanionRegistry registry = new CompanionRegistry();
        ListTag list = tag.getListOrEmpty("companions");
        for (int index = 0; index < list.size(); index++)
        {
            CompoundTag entry = list.getCompoundOrEmpty(index);
            CompanionRecord record = new CompanionRecord(entry.read("binding", UUIDUtil.CODEC).orElseThrow(), entry.read("companion", UUIDUtil.CODEC).orElseThrow(),
                entry.getIntOr("colony", 0), entry.getStringOr("shop_dimension", ""), BlockPos.of(entry.getLongOr("shop_position", 0L)));
            record.active = entry.getBooleanOr("active", false);
            record.activeDimension = entry.getStringOr("active_dimension", "");
            record.activePosition = BlockPos.of(entry.getLongOr("active_position", 0L));
            record.cooldownUntil = entry.getLongOr("cooldown_until", 0L);
            record.inventory = entry.getCompoundOrEmpty("inventory").copy();
            registry.records.put(record.bindingId, record);
        }
        return registry;
    }

    /**
     * Serializes every companion record.
     *
     * @param tag destination tag
     * @param provider registry lookup provider
     * @return populated destination tag
     */
    public @Nonnull CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider)
    {
        ListTag list = new ListTag();
        for (CompanionRecord record : records.values())
        {
            CompoundTag entry = new CompoundTag();
            entry.store("binding", UUIDUtil.CODEC, record.bindingId);
            entry.store("companion", UUIDUtil.CODEC, record.companionId);
            entry.putInt("colony", record.colonyId);
            entry.putString("shop_dimension", record.shopDimension);
            entry.putLong("shop_position", record.shopPosition.asLong());
            entry.putBoolean("active", record.active);
            entry.putString("active_dimension", record.activeDimension);
            entry.putLong("active_position", record.activePosition.asLong());
            entry.putLong("cooldown_until", record.cooldownUntil);
            entry.put("inventory", record.inventory.copy());
            list.add(entry);
        }
        tag.put("companions", list);
        return tag;
    }
}
