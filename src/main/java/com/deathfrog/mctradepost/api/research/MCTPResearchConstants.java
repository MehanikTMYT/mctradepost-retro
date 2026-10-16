package com.deathfrog.mctradepost.api.research;

import com.deathfrog.mctradepost.MCTradePostMod;

import net.minecraft.resources.Identifier;

public class MCTPResearchConstants {

    public static final Identifier RESEARCH_DISENCHANTING     = getResearchEffectID("disenchanting");
    public static final Identifier TOURISTS                   = getResearchEffectID("tourists");
    public static final Identifier TRADESPEED                 = getResearchEffectID("tradespeed");
    public static final Identifier TRADECAPACITY              = getResearchEffectID("tradecapacity");
    public static final Identifier ADVERTISING                = getResearchEffectID("advertising");
    public static final Identifier FIVE_STAR_SERVICE          = getResearchEffectID("fivestarservice");
    public static final Identifier THRIFTSHOP_TIER            = getResearchEffectID("thriftshop_tier");
    public static final Identifier THRIFTSHOP_MORE            = getResearchEffectID("thriftshop_more");
    public static final Identifier THRIFTSHOP_REROLL          = getResearchEffectID("thriftshop_reroll");
    public static final Identifier THRIFTSHOP_BOTTOMLESS      = getResearchEffectID("thriftshop_bottomless");
    public static final Identifier RETAINED_SEARCH            = getResearchEffectID("retained_search");
    public static final Identifier MARKETPLACE_SUBSCRIPTIONS  = getResearchEffectID("marketplace_subscriptions");
    public static final Identifier HUSBANDRY                  = getResearchEffectID("husbandry");
    public static final Identifier EXOTIC                     = getResearchEffectID("exotic");
    public static final Identifier VET                        = getResearchEffectID("vet");
    public static final Identifier FOCUSED_FORAGING           = getResearchEffectID("focused_foraging");
    public static final Identifier ENHANCED_FOCUSED_FORAGING  = getResearchEffectID("enhanced_focused_foraging");
    public static final Identifier MARITIME_TRADE             = getResearchEffectID("maritime_trade");
    public static final Identifier AIR_TRADE                  = getResearchEffectID("air_trade");
    public static final Identifier INCENTIVE_PLANS            = getResearchEffectID("incentive_plans");

    public static Identifier getResearchEffectID(String researchName) {
        return Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "effects/" + researchName);
    }

}
