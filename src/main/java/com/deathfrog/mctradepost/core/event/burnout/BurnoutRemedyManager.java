package com.deathfrog.mctradepost.core.event.burnout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import com.deathfrog.mctradepost.MCTradePostMod;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.crafting.ItemStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public class BurnoutRemedyManager extends SimpleJsonResourceReloadListener<BurnoutRemedyManager.BurnoutRemedy>
{
    public static final String BURNOUT_FOLDER = "burnout";
    private static final Map<Identifier, BurnoutRemedy> burnoutMap = new HashMap<>();

    public BurnoutRemedyManager()
    {
        super(BurnoutRemedy.CODEC, FileToIdConverter.json(BURNOUT_FOLDER));
    }

    public record BurnoutRemedy(List<ItemEntry> remedy, String message)
    {
        public static final Codec<BurnoutRemedy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemEntry.CODEC.listOf().fieldOf("remedy").forGetter(BurnoutRemedy::remedy),
                Codec.STRING.fieldOf("message").forGetter(BurnoutRemedy::message)
            ).apply(instance, BurnoutRemedy::new));

        public record ItemEntry(Identifier id, int count)
        {
            public static final Codec<ItemEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(ItemEntry::id),
                    Codec.INT.fieldOf("count").forGetter(ItemEntry::count)
                ).apply(instance, ItemEntry::new));

            public ItemStorage toStack()
            {
                Item item = BuiltInRegistries.ITEM.getValue(id);
                return new ItemStorage(new ItemStack(item, count));
            }
        }

        @SuppressWarnings("null")
        public List<ItemStorage> toItemStorage()
        {
            return remedy.stream().map(ItemEntry::toStack).toList();
        }
    }

    /**
     * Called when the resource manager is reloading resources. This method is responsible for registering all burnout remedies decoded
     * from the given data map (the base class already parses and validates each JSON file against {@link BurnoutRemedy#CODEC} before
     * this is called, logging and skipping any entry that fails to parse).
     *
     * @param dataMap         a map from resource location to the already-decoded burnout remedy.
     * @param resourceManager the resource manager that is reloading resources.
     * @param profiler        the profiler to use when loading the remedies.
     */
    @Override
    protected void apply(@Nonnull Map<Identifier, BurnoutRemedy> dataMap,
        @Nonnull ResourceManager resourceManager,
        @Nonnull ProfilerFiller profiler)
    {
        burnoutMap.clear();
        burnoutMap.putAll(dataMap);
        MCTradePostMod.LOGGER.info("Loaded {} burnout remedies.", burnoutMap.size());
    }

    /**
     * Listens for the AddServerReloadListenersEvent to add the BurnoutRemedyManager as a reload listener. This ensures that burnout remedies
     * are reloaded whenever the server resources are reloaded.
     *
     * @param event the event that triggers the addition of the reload listener.
     */
    @SubscribeEvent
    public static void listenForBurnoutRecords(@Nonnull final AddServerReloadListenersEvent event)
    {
        event.addListener(Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "burnout_remedies"), new BurnoutRemedyManager());
    }

    /**
     * Get the burnout remedy for the given skill.
     *
     * @param skill the skill for which we want the burnout remedy.
     * @return the burnout remedy for the given skill, or null if there is no such remedy.
     */
    public static List<ItemStorage> getRemedy(@Nonnull Skill skill)
    {
        Identifier id = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, skill.toString().toLowerCase() + "");
        
        BurnoutRemedy remedy = burnoutMap.get(id);
        
        if (remedy == null)
        {
            MCTradePostMod.LOGGER.warn("Cannot find a remedy list for skill: {}", skill.toString().toLowerCase());
            return null;
        }

        return remedy.toItemStorage();
    }
}
