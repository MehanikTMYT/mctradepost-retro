package com.deathfrog.mctradepost.api.client.render.modeltype;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.minecolonies.api.client.render.modeltype.IModelType;
import net.minecraft.resources.Identifier;

public final class ModModelTypes
{

    public static final Identifier SHOPKEEPER_MODEL_ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "shopkeeper");
    public static final Identifier GUESTSERVICES_MODEL_ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "guestservices");
    public static final Identifier BARTENDER_MODEL_ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "bartender");
    public static final Identifier ANIMALTRAINER_MODEL_ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "animaltrainer");
    public static final Identifier RECYCLINGENGINEER_MODEL_ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "recyclingengineer");
    public static final Identifier DAIRYWORKER_MODEL_ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "dairyworker");
    public static final Identifier STEWMELIER_MODEL_ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "stewmelier");
    public static final Identifier STATIONMASTER_MODEL_ID = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "stationmaster");

    public static IModelType SHOPKEEPER;
    public static IModelType GUESTSERVICES;
    public static IModelType BARTENDER;
    public static IModelType ANIMALTRAINER;
    public static IModelType RECYCLINGENGINEER;
    public static IModelType DAIRYWORKER;
    public static IModelType STEWMELIER;
    public static IModelType STATIONMASTER;

    private ModModelTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModModelTypes but this is a Utility class.");
    }
}
