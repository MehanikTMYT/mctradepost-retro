package com.deathfrog.mctradepost.compat;

// com.deathfrog.mctradepost.compat.CompatHooks
public final class CompatHooks
{
    private CompatHooks()
    {
    }

    /**
     * Triggers an EMI recipe reload after server-synced ritual data has changed.
     * <p>
     * Unlike {@link com.deathfrog.mctradepost.compat.emi.MCTPEmiPlugin}'s deferred recipe population
     * (which reads {@link com.deathfrog.mctradepost.core.event.wishingwell.ritual.RitualManager} fresh
     * on every EMI bake), EMI's own bake only runs once per EMI-triggered reload cycle. It is not aware
     * that this mod just received a custom sync packet, so a manual reload is requested here -- the
     * same role the removed JEI bridge used to play.
     * <p>
     * {@code EmiReloadManager} is an internal EMI class not present in the {@code :api} compile-time
     * jar this mod depends on (see build.gradle), so it is invoked via reflection -- this also keeps the mod loadable when EMI (an optional dependency) isn't
     * present at all.
     */
    public static void refreshRitualsEmi()
    {
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() != net.neoforged.api.distmarker.Dist.CLIENT)
        {
            return;
        }

        if (!net.neoforged.fml.ModList.get().isLoaded("emi"))
        {
            return;
        }

        try
        {
            Class<?> c = Class.forName("dev.emi.emi.runtime.EmiReloadManager");
            c.getMethod("reload").invoke(null);
        }
        catch (Throwable t)
        {
            com.deathfrog.mctradepost.MCTradePostMod.LOGGER.warn("Failed to refresh EMI rituals", t);
        }
    }

    /**
     * Triggers an EMI recipe reload after server-synced pet foraging entries have changed.
     * See {@link #refreshRitualsEmi()} for why this manual push is still needed alongside EMI's own
     * deferred recipe population.
     */
    public static void refreshPetForagingEmi()
    {
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() != net.neoforged.api.distmarker.Dist.CLIENT)
        {
            return;
        }

        if (!net.neoforged.fml.ModList.get().isLoaded("emi"))
        {
            return;
        }

        try
        {
            Class<?> c = Class.forName("dev.emi.emi.runtime.EmiReloadManager");
            c.getMethod("reload").invoke(null);
        }
        catch (Throwable t)
        {
            com.deathfrog.mctradepost.MCTradePostMod.LOGGER.warn("Failed to refresh EMI pet foraging entries", t);
        }
    }
}
