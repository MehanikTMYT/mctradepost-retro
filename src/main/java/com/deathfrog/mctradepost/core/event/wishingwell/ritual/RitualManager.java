package com.deathfrog.mctradepost.core.event.wishingwell.ritual;

import com.deathfrog.mctradepost.MCTradePostMod;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class RitualManager extends SimpleJsonResourceReloadListener<RitualDefinition>
{
    private static final String RITUAL_FOLDER = "rituals";
    private static final Map<Identifier, RitualDefinitionHelper> rituals = new HashMap<>();

    public static final String RITUAL_EFFECT_SLAY = "slay";
    public static final String RITUAL_EFFECT_WEATHER = "weather";
    public static final String RITUAL_EFFECT_SUMMON = "summon";
    public static final String RITUAL_EFFECT_TRANSFORM = "transform";
    public static final String RITUAL_EFFECT_COMMUNITY = "community";
    public static final String RITUAL_EFFECT_OUTPOST    = "outpost";
    public static final String RITUAL_EFFECT_KNOWLEDGE  = "knowledge";
    public static final String RITUAL_EFFECT_RESURRECTION = "resurrection";

    public RitualManager()
    {
        super(RitualDefinition.CODEC, FileToIdConverter.json(RITUAL_FOLDER));
    }

    /**
     * Called when the resource manager is reloading resources. This method is responsible for registering all rituals decoded from the
     * given data map (the base class already parses and validates each JSON file against {@link RitualDefinition#CODEC} before this is
     * called, logging and skipping any entry that fails to parse).
     *
     * @param objectMap       a map from resource location to the already-decoded ritual definition.
     * @param resourceManager the resource manager that is reloading resources.
     * @param profiler        the profiler to use when loading the rituals.
     */
    @Override
    protected void apply(@Nonnull Map<Identifier, RitualDefinition> objectMap,
        @Nonnull ResourceManager resourceManager,
        @Nonnull ProfilerFiller profiler)
    {
        rituals.clear();

        MCTradePostMod.LOGGER.info("Ritual map size: {}", objectMap.size());

        objectMap.forEach((id, def) -> {
            MCTradePostMod.LOGGER.info("Decoding ritual: {}", id);
            rituals.put(id, new RitualDefinitionHelper(id, def));
        });

        MCTradePostMod.LOGGER.info("Loaded {} ritual definitions", rituals.size());
    }

    /**
     * Retrieves a ritual definition helper for the specified resource location.
     *
     * @param id the resource location of the ritual.
     * @return the ritual definition helper associated with the given resource location, or null if no such ritual exists.
     */
    public static RitualDefinitionHelper getRitual(Identifier id)
    {
        return rituals.get(id);
    }

    /**
     * Puts a ritual definition helper into the map of rituals.
     *
     * @param id     the resource location of the ritual.
     * @param helper the ritual definition helper to store in the map.
     */
    public static void putRitual(Identifier id, RitualDefinitionHelper helper)
    {
        rituals.put(id, helper);
    }

    /**
     * Replaces the complete ritual set with an authoritative synchronized copy.
     *
     * @param definitions ritual definitions keyed by their resource location
     */
    public static void replaceRituals(Map<Identifier, RitualDefinition> definitions)
    {
        rituals.clear();
        definitions.forEach((id, definition) -> {
            if (definition != null)
            {
                rituals.put(id, new RitualDefinitionHelper(id, definition));
            }
        });
    }

    /**
     * Retrieves all the ritual definitions that have been loaded.
     *
     * @return a map of resource locations to their corresponding ritual definition helpers.
     */
    public static Map<Identifier, RitualDefinitionHelper> getAllRituals()
    {
        return rituals;
    }

    /**
     * Retrieves all the ritual definitions that have been loaded, but without the helper objects that wrap them. This is useful for
     * serializing the rituals to the client, for example.
     *
     * @return a map of resource locations to their corresponding ritual definitions.
     */
    public static Map<Identifier, RitualDefinition> getUnwrappedRituals()
    {
        Map<Identifier, RitualDefinition> unwrappedRituals = new HashMap<>();

        rituals.forEach((id, helper) -> unwrappedRituals.put(id, helper.getDefinition()));

        return unwrappedRituals;
    }
}
