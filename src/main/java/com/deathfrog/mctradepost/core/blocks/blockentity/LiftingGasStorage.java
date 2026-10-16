package com.deathfrog.mctradepost.core.blocks.blockentity;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.core.fluids.MCTPFluids;
import net.minecraft.world.level.material.Fluid;

/** Shared one-tank Lifting Gas storage and NeoForge automation contract. */
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Lifting Gas tank shared by the Gasifier and the Mooring Bay.
 * 26.1.2 port: the IFluidHandler capability is gone; {@link #createFluidHandler(LiftingGasStorage)} exposes the tank
 * through the transfer API (Capabilities.Fluid.BLOCK) with transaction rollback.
 */
public interface LiftingGasStorage
{
    /** Block-entity data key used to persist stored Lifting Gas. */
    String LIFTING_GAS_NBT_KEY = "LiftingGas";

    /** Translation key shared by Lifting Gas storage screens. */
    String LIFTING_GAS_DISPLAY_KEY = "container.mctradepost.lifting_gas";

    int gasAmount();

    int gasCapacity();

    void setGasAmount(int amount);

    default int fillGas(int amount, boolean simulate)
    {
        int accepted = Math.min(Math.max(0, amount), gasCapacity() - gasAmount());
        if (!simulate && accepted > 0) setGasAmount(gasAmount() + accepted);
        return accepted;
    }

    default int drainGas(int amount, boolean simulate)
    {
        int drained = Math.min(Math.max(0, amount), gasAmount());
        if (!simulate && drained > 0) setGasAmount(gasAmount() - drained);
        return drained;
    }

    default Fluid liftingGas()
    {
        return MCTPFluids.LIFTING_GAS.get();
    }

    static ResourceHandler<FluidResource> createFluidHandler(LiftingGasStorage storage)
    {
        return new FluidHandler(storage);
    }

    /** Single-tank transfer-API view of a {@link LiftingGasStorage}; only Lifting Gas is accepted. */
    final class FluidHandler extends SnapshotJournal<Integer> implements ResourceHandler<FluidResource>
    {
        private final LiftingGasStorage storage;

        private FluidHandler(LiftingGasStorage storage)
        {
            this.storage = storage;
        }

        @Override
        public int size()
        {
            return 1;
        }

        @SuppressWarnings("null")
        @Override
        public FluidResource getResource(int index)
        {
            return index == 0 && storage.gasAmount() > 0 ? FluidResource.of(storage.liftingGas()) : FluidResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index)
        {
            return index == 0 ? storage.gasAmount() : 0;
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource)
        {
            return index == 0 && (resource.isEmpty() || isValid(index, resource)) ? storage.gasCapacity() : 0;
        }

        @SuppressWarnings("null")
        @Override
        public boolean isValid(int index, FluidResource resource)
        {
            return index == 0 && resource.is(storage.liftingGas());
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction)
        {
            if (amount <= 0 || !isValid(index, resource)) return 0;
            int accepted = storage.fillGas(amount, true);
            if (accepted > 0)
            {
                updateSnapshots(transaction);
                storage.setGasAmount(storage.gasAmount() + accepted);
            }
            return accepted;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction)
        {
            if (amount <= 0 || !isValid(index, resource)) return 0;
            int drained = storage.drainGas(amount, true);
            if (drained > 0)
            {
                updateSnapshots(transaction);
                storage.setGasAmount(storage.gasAmount() - drained);
            }
            return drained;
        }

        @Override
        protected Integer createSnapshot()
        {
            return storage.gasAmount();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot)
        {
            storage.setGasAmount(snapshot);
        }
    }
}
