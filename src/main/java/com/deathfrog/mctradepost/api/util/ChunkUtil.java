package com.deathfrog.mctradepost.api.util;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.deathfrog.mctradepost.MCTradePostMod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkUtil
{
    // TicketType instances themselves now live in MCTradePostMod.TICKET_TYPES (see there for why:
    // TicketType lost its per-entity comparator/lifespan constructor in 26.1.2 and instances must be
    // registered through a DeferredRegister rather than constructed as bare static fields here).
    @Nonnull public static TicketType railTicket()
    {
        return MCTradePostMod.RAIL_TICKET.get();
    }

    @Nonnull public static TicketType outpostTicket()
    {
        return MCTradePostMod.OUTPOST_TICKET.get();
    }

    public static final @Nonnull Set<ChunkPos> emptySet = NullnessBridge.assumeNonnull(Set.of());

    /**
     * Ensure the given chunk is loaded, and if not, load it synchronously. This method is used during pathfinding to ensure that the
     * chunk containing the rail block is loaded. If the chunk is already loaded, nothing happens.
     * 
     * @param level The level to check.
     * @param cp    the chunk to check for.
     * @return true if the chunk is loaded, false if not.
     */
    public static boolean ensureChunkLoadedByTicket(ServerLevel level, ChunkPos cp, int radius, @Nonnull TicketType ticketType)
    {
        return ensureChunkLoadedByTicket(level, cp, radius, ticketType, null);
    }

    /**
     * Ensure the given chunk is loaded, and optionally records tickets this call added so callers can release them after a bounded
     * operation completes.
     *
     * @param level The level to check.
     * @param cp the chunk to check for.
     * @param radius the region ticket radius.
     * @param ticketType the ticket type to add if the chunk is not already loaded.
     * @param ticketsAdded optional set that receives chunks for which this call added a ticket.
     * @return true if the chunk is loaded, false if not.
     */
    public static boolean ensureChunkLoadedByTicket(ServerLevel level,
        ChunkPos cp,
        int radius,
        @Nonnull TicketType ticketType,
        @Nullable Set<ChunkPos> ticketsAdded)
    {
        // already resident?
        if (level.hasChunk(cp.x(), cp.z())) return true;

        // 1. add a short-lived ticket
        level.getChunkSource().addTicketWithRadius(ticketType, cp, radius);
        if (ticketsAdded != null)
        {
            ticketsAdded.add(cp);
        }

        // 2. pull the chunk synchronously so the search can proceed *this* tick
        level.getChunk(cp.x(), cp.z(), NullnessBridge.assumeNonnull(ChunkStatus.FULL), true);   // may generate

        return level.hasChunk(cp.x(), cp.z());
    }

    /**
     * Ensure the given chunk is loaded, and if not, load it synchronously. This method is used during pathfinding to ensure that the
     * chunk containing the rail block is loaded. If the chunk is already loaded, nothing happens.
     * 
     * @param level The level to check.
     * @param pos   the position of the block to check for.
     * @return true if the chunk is loaded, false if not.
     */
    public static boolean ensureChunkLoadedByTicket(@Nonnull ServerLevel level, @Nonnull BlockPos pos, int radius, @Nonnull TicketType ticketType)
    {
        return ensureChunkLoadedByTicket(level, ChunkPos.containing(pos), radius, ticketType);
    }

    /**
     * Ensure the chunk containing a block position is loaded, recording newly added tickets when requested.
     *
     * @param level The level to check.
     * @param pos the position of the block to check for.
     * @param radius the region ticket radius.
     * @param ticketType the ticket type to add if the chunk is not already loaded.
     * @param ticketsAdded optional set that receives chunks for which this call added a ticket.
     * @return true if the chunk is loaded, false if not.
     */
    public static boolean ensureChunkLoadedByTicket(@Nonnull ServerLevel level,
        @Nonnull BlockPos pos,
        int radius,
        @Nonnull TicketType ticketType,
        @Nullable Set<ChunkPos> ticketsAdded)
    {
        return ensureChunkLoadedByTicket(level, ChunkPos.containing(pos), radius, ticketType, ticketsAdded);
    }

    /**
     * Release the chunk ticket for the given BlockPos and radius. This method should be called after pathfinding has completed to
     * release the ticket and free up resources.
     * 
     * @param level  the level to release the ticket in
     * @param pos    the BlockPos of the chunk to release
     * @param radius the radius of the ticket to release
     */
    public static void releaseChunkTicket(@Nonnull ServerLevel level, @Nonnull BlockPos pos, int radius, @Nonnull TicketType ticketType)
    {
        ChunkPos cp = ChunkPos.containing(pos);
        level.getChunkSource().removeTicketWithRadius(ticketType, cp, radius);
    }

    /**
     * Releases chunk tickets for exact chunk positions that were previously recorded by a ticket-adding operation.
     *
     * @param level the level to release the tickets in
     * @param chunks chunk positions that were ticketed
     * @param radius the radius of the tickets to release
     * @param ticketType the ticket type to release
     */
    public static void releaseChunkTickets(@Nonnull ServerLevel level,
        @Nonnull Set<ChunkPos> chunks,
        int radius,
        @Nonnull TicketType ticketType)
    {
        for (ChunkPos cp : chunks)
        {
            if (cp == null)
            {
                continue;
            }
            level.getChunkSource().removeTicketWithRadius(ticketType, cp, radius);
        }
    }


    /**
     * Forces chunks around a position.
     *
     * @param level server level (must be ServerLevel)
     * @param center block position
     * @param radiusChunks 0 = only the chunk containing center; 1 = 3x3 chunks; etc.
     * @param alreadyForced previously-forced chunk set; If provided, this method will not re-force chunks already in the set.
     * @return the updated set of forced chunks
     */
    @Nonnull
    public static Set<ChunkPos> forceChunksAround(
        @Nonnull final Level level,
        @Nonnull final BlockPos center,
        final int radiusChunks,
        @Nullable final Set<ChunkPos> alreadyForced)
    {
        if (!(level instanceof final ServerLevel serverLevel))
        {
            return alreadyForced == null ? emptySet : alreadyForced;
        }

        final Set<ChunkPos> forced = alreadyForced == null ? new HashSet<>() : new HashSet<>(alreadyForced);

        final ChunkPos centerChunk = ChunkPos.containing(center);
        final int r = Math.max(0, radiusChunks);

        for (int dx = -r; dx <= r; dx++)
        {
            for (int dz = -r; dz <= r; dz++)
            {
                final ChunkPos cp = new ChunkPos(centerChunk.x() + dx, centerChunk.z() + dz);
                if (forced.contains(cp))
                {
                    continue;
                }

                // Force it
                serverLevel.setChunkForced(cp.x(), cp.z(), true);
                forced.add(cp);
            }
        }

        return forced;
    }

    /**
     * Releases previously forced chunks.
     *
     * @param level server level
     * @param forcedChunks the exact set you got back from forceChunksAround (persisted in NBT)
     * @return empty set (so your caller can just assign the field)
     */
    @Nonnull
    public static Set<ChunkPos> unforceChunks(
        @Nonnull final Level level,
        @Nullable final Set<ChunkPos> forcedChunks)
    {
        if (forcedChunks == null || forcedChunks.isEmpty())
        {
            return emptySet;
        }

        if (!(level instanceof final ServerLevel serverLevel))
        {
            // Can't unforce on client; keep state so it can be unforced on server later
            return forcedChunks;
        }

        for (final ChunkPos cp : forcedChunks)
        {
            if (cp == null) continue;
            serverLevel.setChunkForced(cp.x(), cp.z(), false);
        }

        return emptySet;
    }

}
