package com.deathfrog.mctradepost.core.event.wishingwell.ritual;

import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import com.deathfrog.mctradepost.MCTradePostMod;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = MCTradePostMod.MODID)
public class RitualReloadListener
{
    @SubscribeEvent
    public static void onReloadListenerRegistration(AddServerReloadListenersEvent event)
    {
        event.addListener(Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "rituals"), new RitualManager());
        MCTradePostMod.LOGGER.info("Registered RitualManager as a datapack reload listener.");
    }
}
