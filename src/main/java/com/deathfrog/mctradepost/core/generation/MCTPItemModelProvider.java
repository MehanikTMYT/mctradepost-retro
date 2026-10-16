package com.deathfrog.mctradepost.core.generation;

import com.deathfrog.mctradepost.MCTradePostMod;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Datagen for the client-item descriptors ({@code assets/mctradepost/items/<id>.json}) that Minecraft
 * 26.1's split item-model pipeline requires for every registered item. Block models/blockstates and
 * every item's inventory model ({@code models/item/<id>.json}) are shipped as static resources under
 * {@code assets/mctradepost}; this provider only has to point each plain (non-block) item at its
 * already-existing static model. Most BlockItems are left unregistered here so the base ModelProvider
 * auto-fills them pointing at {@code block/<id>} (mirrors MineColonies' DefaultItemModelProvider); the
 * timber-frame family (checkerboard/distressed/glazed/side_slab/side_slab_interleaved/stacked_slab)
 * ship their block model under {@code block/timber_frame/<id>} instead (their blockstate is a
 * multipart selecting that model per facing), so the auto-fill's flat {@code block/<id>} guess would
 * leave their inventory icon missing - those get an explicit override below. Souvenir and outpost_claim
 * ship their own hand-authored client-item descriptor (special renderer / conditional model
 * respectively) and must never be touched by this provider.
 */
@EventBusSubscriber(modid = MCTradePostMod.MODID, value = Dist.CLIENT)
public class MCTPItemModelProvider extends ModelProvider
{
    private static final Set<String> STATIC_ONLY_ITEMS = Set.of("outpost_claim", "souvenir");

    private static final Set<String> TIMBER_FRAME_ITEMS =
        Set.of("checkerboard", "distressed", "glazed", "side_slab", "side_slab_interleaved", "stacked_slab");

    public MCTPItemModelProvider(final PackOutput packOutput)
    {
        super(packOutput, MCTradePostMod.MODID);
    }

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent.Client event)
    {
        event.addProvider(new MCTPItemModelProvider(event.getGenerator().getPackOutput()));
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks()
    {
        // Block models and blockstates are shipped as static resources; nothing to generate here.
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems()
    {
        // Excluded here (not just skipped in registerModels below) so the base class's own
        // finalizeAndValidate() - which re-derives its "did every known item get an entry"
        // check from this same method - doesn't flag their static descriptor as missing.
        return super.getKnownItems().filter(holder -> !STATIC_ONLY_ITEMS.contains(holder.unwrapKey().orElseThrow().identifier().getPath()));
    }

    @Override
    protected void registerModels(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels)
    {
        getKnownItems().forEach(holder -> {
            final Item item = holder.value();
            final String path = holder.unwrapKey().orElseThrow().identifier().getPath();

            if (item instanceof BlockItem)
            {
                if (TIMBER_FRAME_ITEMS.contains(path))
                {
                    itemModels.itemModelOutput.accept(item,
                        ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "block/timber_frame/" + path)));
                }
                return;
            }

            itemModels.itemModelOutput.accept(item,
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "item/" + path)));
        });
    }
}
