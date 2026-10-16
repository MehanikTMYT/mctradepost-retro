package com.deathfrog.mctradepost.api.items;

import com.deathfrog.mctradepost.MCTradePostMod;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Equipment assets for the armour added by Trade Post.
 * <p>
 * Since 26.1 the armour layer textures are no longer declared on the {@code ArmorMaterial} itself but referenced
 * through an {@link EquipmentAsset} key, which resolves to {@code assets/mctradepost/equipment/<path>.json}
 * (mirrors com.minecolonies.api.items.ModEquipmentAssets).
 */
public final class ModEquipmentAssets
{
    public static final ResourceKey<EquipmentAsset> REINFORCED_LEATHER = create("reinforced_leather");

    private ModEquipmentAssets()
    {
        throw new IllegalStateException("Tried to initialize: ModEquipmentAssets but this is a Utility class.");
    }

    private static ResourceKey<EquipmentAsset> create(final String name)
    {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, name));
    }
}
