package com.deathfrog.mctradepost.core.event;

import com.deathfrog.mctradepost.MCTradePostMod;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Specific texture reload listener.
 */
@EventBusSubscriber(value= Dist.CLIENT, modid = MCTradePostMod.MODID)
public class TextureReloadListener extends SimplePreparableReloadListener<TextureReloadListener.TexturePacks>
{
    /**
     * List of all texture packs available.
     */
    public static final List<String> TEXTURE_PACKS = new ArrayList<>();

    @NotNull
    @Override
    protected TexturePacks prepare(@Nonnull final ResourceManager manager, @Nonnull final ProfilerFiller profiler)
    {
        MCTradePostMod.LOGGER.info("Preparing texture packs");
        
        final Set<String> set = new HashSet<>();
        final List<Identifier> resLocs = new ArrayList<>(manager.listResources("textures/entity/citizen", f -> true).keySet());
        for (final Identifier res : resLocs)
        {
            if (res.getPath().contains("png") && res.getPath().contains("textures/entity/citizen"))
            {
                final String folder = res.getPath().split("/")[3];
                if (!folder.isEmpty())
                {
                    set.add(folder);
                }
            }
        }

        final TexturePacks packs = new TexturePacks();
        packs.packs = new ArrayList<>(set);
        return packs;
    }

    @Override
    protected void apply(@Nonnull final TexturePacks packs, @Nonnull final ResourceManager manager, @Nonnull final ProfilerFiller profiler)
    {
       TextureReloadListener.TEXTURE_PACKS.clear();
       TextureReloadListener.TEXTURE_PACKS.addAll(packs.packs);
    }

    /**
     * Storage class to hand the texture packs from off-thread to the main thread.
     */
    public static class TexturePacks
    {
        public List<String> packs = new ArrayList<>();
    }

    @SubscribeEvent
    public static void modInitClient(final AddClientReloadListenersEvent event)
    {
        event.addListener(Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "citizen_textures"), new TextureReloadListener());

        // 26.1.2 port: the old com.ldtteam.blockui.AtlasManager.INSTANCE.addAtlas(...) registration API is gone.
        // Registering a dedicated mctradepost GUI atlas (net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent
        // + BlockUI.NAMESPACE_TO_ATLAS_MAP, see MineColonies' own TextureReloadListener for the pattern) is an optional
        // batching optimization only: BlockUI's Image.resolveBlit falls through to a direct per-texture blit for any
        // namespace with no registered atlas, which is exactly what our "mctradepost:textures/gui/*.png"-style XML
        // `source` references already use. No atlas is required for our GUI icons to render correctly.
    }
}

