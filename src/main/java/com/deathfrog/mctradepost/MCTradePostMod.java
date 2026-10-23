package com.deathfrog.mctradepost;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.slf4j.Logger;

import com.deathfrog.mctradepost.api.advancements.MCTPAdvancementTriggers;
import com.deathfrog.mctradepost.api.entity.AirshipEntity;
import com.deathfrog.mctradepost.api.entity.GhostBoatEntity;
import com.deathfrog.mctradepost.api.entity.GhostCartEntity;
import com.deathfrog.mctradepost.api.entity.WagonEntity;
import com.deathfrog.mctradepost.api.entity.pets.ITradePostPet;
import com.deathfrog.mctradepost.api.entity.pets.PetAxolotl;
import com.deathfrog.mctradepost.api.entity.pets.PetCat;
import com.deathfrog.mctradepost.api.entity.pets.PetDragon;
import com.deathfrog.mctradepost.api.entity.pets.PetFox;
import com.deathfrog.mctradepost.api.entity.pets.PetParrot;
import com.deathfrog.mctradepost.api.entity.pets.PetPanda;
import com.deathfrog.mctradepost.api.entity.pets.PetTypes;
import com.deathfrog.mctradepost.api.entity.pets.PetWolf;
import com.deathfrog.mctradepost.api.items.MCTPModDataComponents;
import com.deathfrog.mctradepost.api.items.datacomponent.DimensionalLinkageRecord;
import com.deathfrog.mctradepost.api.items.datacomponent.NavigationChartRecord;
import com.deathfrog.mctradepost.api.sounds.MCTPModSoundEvents;
import com.deathfrog.mctradepost.api.tileentities.MCTradePostTileEntities;
import com.deathfrog.mctradepost.api.util.ItemValueManager;
import com.deathfrog.mctradepost.api.util.NullnessBridge;
import com.deathfrog.mctradepost.api.util.PetRegistryUtil;
import com.deathfrog.mctradepost.apiimp.initializer.MCTPCraftingSetup;
import com.deathfrog.mctradepost.apiimp.initializer.MCTPInteractionInitializer;
import com.deathfrog.mctradepost.apiimp.initializer.ModBlocksInitializer;
import com.deathfrog.mctradepost.apiimp.initializer.ModBuildingsInitializer;
import com.deathfrog.mctradepost.apiimp.initializer.ModJobsInitializer;
import com.deathfrog.mctradepost.apiimp.initializer.ModModelTypeInitializer;
import com.deathfrog.mctradepost.apiimp.initializer.TileEntityInitializer;
import com.deathfrog.mctradepost.api.items.ModEquipmentAssets;
import com.deathfrog.mctradepost.core.ModTags;
import com.deathfrog.mctradepost.core.blocks.AbstractBlockPetWorkingLocation;
import com.deathfrog.mctradepost.core.blocks.BlockCheckerboard;
import com.deathfrog.mctradepost.core.blocks.BlockDistressed;
import com.deathfrog.mctradepost.core.blocks.BlockDredger;
import com.deathfrog.mctradepost.core.blocks.BlockFeeder;
import com.deathfrog.mctradepost.core.blocks.BlockGlazed;
import com.deathfrog.mctradepost.core.blocks.BlockHauler;
import com.deathfrog.mctradepost.core.blocks.BlockLamp;
import com.deathfrog.mctradepost.core.blocks.BlockMixedStone;
import com.deathfrog.mctradepost.core.blocks.BlockMooringBay;
import com.deathfrog.mctradepost.core.blocks.BlockGasifier;
import com.deathfrog.mctradepost.core.fluids.MCTPFluids;
import com.deathfrog.mctradepost.core.blocks.BlockOutpostMarker;
import com.deathfrog.mctradepost.core.blocks.BlockSideSlab;
import com.deathfrog.mctradepost.core.blocks.BlockSideSlabInterleaved;
import com.deathfrog.mctradepost.core.blocks.BlockStackedSlab;
import com.deathfrog.mctradepost.core.blocks.BlockTradeDock;
import com.deathfrog.mctradepost.core.blocks.BlockTradeInterchange;
import com.deathfrog.mctradepost.core.blocks.BlockTrough;
import com.deathfrog.mctradepost.core.blocks.StewpotBlock;
import com.deathfrog.mctradepost.core.blocks.BlockScavenge;
import com.deathfrog.mctradepost.core.blocks.huts.BlockHutMarketplace;
import com.deathfrog.mctradepost.core.blocks.huts.BlockHutPetShop;
import com.deathfrog.mctradepost.core.blocks.huts.BlockHutRecycling;
import com.deathfrog.mctradepost.core.blocks.huts.BlockHutResort;
import com.deathfrog.mctradepost.core.blocks.huts.BlockHutStation;
import com.deathfrog.mctradepost.core.blocks.huts.BlockHutOutpost;
import com.deathfrog.mctradepost.core.blocks.huts.MCTPBaseBlockHut;
import com.deathfrog.mctradepost.core.client.render.AirshipRenderer;
import com.deathfrog.mctradepost.core.client.render.AdvancedClipBoardDecorator;
import com.deathfrog.mctradepost.core.client.render.GhostBoatRenderer;
import com.deathfrog.mctradepost.core.client.render.GhostCartRenderer;
import com.deathfrog.mctradepost.core.client.render.PetDragonRenderer;
import com.deathfrog.mctradepost.core.client.render.WagonRenderer;
import com.deathfrog.mctradepost.core.client.render.souvenir.SouvenirSpecialRenderer;
import com.deathfrog.mctradepost.core.colony.buildings.modules.BuildingStationConnectionModule;
import com.deathfrog.mctradepost.core.colony.buildings.modules.CancelRecyclingMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.MCTPBuildingModules;
import com.deathfrog.mctradepost.core.colony.buildings.modules.PetMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.ResortGuestMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.StationLinkageMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.StationConnectionAttemptMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.StationRouteRefreshMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.StewIngredientMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.ThriftShopMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.MarketplaceSourcingMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.TradeMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.WithdrawMessage;
import com.deathfrog.mctradepost.core.colony.buildings.modules.settings.MCTPSettingsFactory;
import com.deathfrog.mctradepost.core.colony.requestsystem.resolvers.OutpostRequestResolverFactory;
import com.deathfrog.mctradepost.core.colony.requestsystem.resolvers.TrainDeliveryResolverFactory;
import com.deathfrog.mctradepost.core.colony.buildings.workerbuildings.BuildingOutpost;
import com.deathfrog.mctradepost.core.colony.buildings.workerbuildings.BuildingStation;
import com.deathfrog.mctradepost.core.event.ModelRegistryHandler;
import com.deathfrog.mctradepost.core.event.burnout.BurnoutRemedyManager;
import com.deathfrog.mctradepost.core.event.wishingwell.resurrection.BurialEventHandler;
import com.deathfrog.mctradepost.core.recycling.blacklist.RecyclingBlacklistManager;
import com.deathfrog.mctradepost.core.rarefinds.blacklist.RareFindBlacklistManager;
import com.deathfrog.mctradepost.network.RareFindBlacklistSyncPacket;
import com.deathfrog.mctradepost.core.event.wishingwell.ritual.RitualManager;
import com.deathfrog.mctradepost.core.event.wishingwell.ritual.RitualPacket;
import com.deathfrog.mctradepost.core.entity.pets.scavenge.PetForagingJeiSyncPacket;
import com.deathfrog.mctradepost.core.entity.pets.scavenge.FocusedForagingIndex;
import com.deathfrog.mctradepost.core.entity.ai.workers.trade.DimPos;
import com.deathfrog.mctradepost.core.entity.ai.workers.trade.StationData;
import com.deathfrog.mctradepost.core.entity.ai.workers.trade.TrackPathConnection.TrackConnectionResult;
import com.deathfrog.mctradepost.core.entity.ai.workers.trade.TrackRoute;
import com.deathfrog.mctradepost.core.loot.ModLootModifiers;
import com.deathfrog.mctradepost.core.network.messages.OutpostAssignMessage;
import com.deathfrog.mctradepost.core.placementhandlers.OutpostPlacementHandler;
import com.deathfrog.mctradepost.item.AdvancedClipboardItem;
import com.deathfrog.mctradepost.item.BlockCheckerboardItem;
import com.deathfrog.mctradepost.item.BlockDistressedItem;
import com.deathfrog.mctradepost.item.BlockGlazedItem;
import com.deathfrog.mctradepost.item.BlockSideSlabInterleavedItem;
import com.deathfrog.mctradepost.item.BlockSideSlabItem;
import com.deathfrog.mctradepost.item.BlockStackedSlabItem;
import com.deathfrog.mctradepost.item.CoinItem;
import com.deathfrog.mctradepost.item.CompanionStaffItem;
import com.deathfrog.mctradepost.item.CurrencyExchangeItem;
import com.deathfrog.mctradepost.item.DimensionalLinkageItem;
import com.deathfrog.mctradepost.item.RouteSurveyItem;
import com.deathfrog.mctradepost.item.NavigationChartItem;
import com.deathfrog.mctradepost.item.LiftingGasBucketItem;
import com.deathfrog.mctradepost.item.ImmersionBlenderItem;
import com.deathfrog.mctradepost.item.OutpostClaimMarkerItem;
import com.deathfrog.mctradepost.item.PetWorkingLocationBlockItem;
import com.deathfrog.mctradepost.item.SouvenirItem;
import com.deathfrog.mctradepost.item.ScrapPileItem;
import com.deathfrog.mctradepost.item.SouvenirItem.SouvenirRecord;
import com.deathfrog.mctradepost.item.WishGatheringItem;
import com.deathfrog.mctradepost.item.WishResurrectionItem;
import com.deathfrog.mctradepost.network.ConfigurationPacket;
import com.deathfrog.mctradepost.network.ItemValueSyncPacket;
import com.deathfrog.mctradepost.recipe.DeconstructionRecipe;
import com.deathfrog.mctradepost.recipe.DurableToolShapelessRecipe;
import com.deathfrog.mctradepost.recipe.PotionShapelessRecipe;
import com.deathfrog.mctradepost.recipe.UniqueTagShapelessRecipe;
import com.deathfrog.mctradepost.recipe.GasifierRecipe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenBuriedModEvent;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.util.IItemHandlerCapProvider;
import com.minecolonies.api.util.LegacyItemHandlerResourceAdapter;
import com.minecolonies.api.util.RegistryConstructionContext;
import com.minecolonies.api.util.constant.EquipmentLevelConstants;
import com.minecolonies.core.items.ItemFood;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.PandaRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.DefaultDataComponentsBoundEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.block.FluidModel;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.RegisterEvent;
import com.deathfrog.mctradepost.core.entity.CoinEntity;
import com.deathfrog.mctradepost.core.entity.CoinRenderer;

@Mod(MCTradePostMod.MODID)
public class MCTradePostMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mctradepost";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final java.util.concurrent.atomic.AtomicBoolean FACTORIES_REGISTERED = new AtomicBoolean(false);

    // Create a Gson instance
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Identifier.class, new TypeAdapter<Identifier>()
        {
            @Override
            public void write(JsonWriter out, Identifier value) throws IOException {
                out.value(value.toString());
            }

            @Override
            public Identifier read(JsonReader in) throws IOException
            {
                String nextString = in.nextString();

                if (nextString == null)
                {
                    return null;
                }

                return Identifier.parse(nextString);
            }
        })
        .setPrettyPrinting()
        .create();

    // Create a Deferred Register to hold Blocks which will all be registered under the "mctradepost" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    // Create a Deferred Register to hold Items which will all be registered under the "mctradepost" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 26.1.2 requires every Item/Block's Properties to carry its registry id (Item/BlockBehaviour's
    // constructors now call properties.effectiveDescriptionId(), which throws "Item id not set" /
    // "Block id not set" if Properties.setId(ResourceKey) was never called). registerItem/registerBlock
    // below capture the id being registered in a ThreadLocal for the duration of the factory call, so
    // that itemProps()/blockProps()/blockPropsFrom() -- drop-in replacements for the plain
    // "new Item.Properties()"/"Properties.of()"/"<block>.get().properties()" this file used everywhere
    // before 26.1.2 -- can stamp it on automatically without threading an id parameter through every
    // one of this file's ~220 registration call sites individually.
    private static final ThreadLocal<Identifier> CURRENT_ITEM_ID = new ThreadLocal<>();
    private static final ThreadLocal<Identifier> CURRENT_BLOCK_ID = new ThreadLocal<>();

    private static <I extends Item> DeferredItem<I> registerItem(final String name, final Supplier<? extends I> factory)
    {
        return ITEMS.register(name, id -> {
            CURRENT_ITEM_ID.set(id);
            try
            {
                return factory.get();
            }
            finally
            {
                CURRENT_ITEM_ID.remove();
            }
        });
    }

    private static <B extends Block> DeferredBlock<B> registerBlock(final String name, final Supplier<? extends B> factory)
    {
        return BLOCKS.register(name, id -> {
            CURRENT_BLOCK_ID.set(id);
            try
            {
                return factory.get();
            }
            finally
            {
                CURRENT_BLOCK_ID.remove();
            }
        });
    }

    private static Item.Properties itemProps()
    {
        final Item.Properties props = new Item.Properties();
        final Identifier id = CURRENT_ITEM_ID.get();
        if (id != null)
        {
            props.setId(ResourceKey.create(Registries.ITEM, id));
        }
        return props;
    }

    // Since 26.1, Item.Properties defaults to an item.<namespace>.<path> description id for every
    // item, including BlockItem - the old automatic fall-through to the paired block's
    // block.<namespace>.<path> name is gone (see Item.Properties#useBlockDescriptionPrefix()). Every
    // BlockItem registration below should go through this instead of the plain itemProps(), so its
    // display name/tooltip resolve against the block's own lang entry rather than requiring a
    // redundant item.<namespace>.<path> duplicate in every lang file.
    private static Item.Properties blockItemProps()
    {
        return itemProps().useBlockDescriptionPrefix();
    }

    // Public: some block base classes (e.g. ExtendedTimberFrameBlock) build their own Properties
    // internally in a no-arg constructor rather than accepting one from this file's registration
    // lambdas, so they need to reach this from outside the com.deathfrog.mctradepost package too.
    public static BlockBehaviour.Properties blockProps()
    {
        return applyCurrentBlockId(BlockBehaviour.Properties.of());
    }

    private static BlockBehaviour.Properties blockPropsFrom(final Block source)
    {
        return applyCurrentBlockId(source.properties());
    }

    private static BlockBehaviour.Properties blockPropsCopyOf(final Block source)
    {
        return applyCurrentBlockId(BlockBehaviour.Properties.ofFullCopy(source));
    }

    private static BlockBehaviour.Properties applyCurrentBlockId(final BlockBehaviour.Properties props)
    {
        final Identifier id = CURRENT_BLOCK_ID.get();
        if (id != null)
        {
            props.setId(ResourceKey.create(Registries.BLOCK, id));
        }
        return props;
    }

    // Since 26.1, ArmorMaterial is a plain record rather than a registered object, so there is no
    // more Registries.ARMOR_MATERIAL to register it into (see REINFORCED_LEATHER_ARMOR_MATERIAL below).

    // Create a Deferred Register to hold Entities which will all be registered under the "mctradepost" namespace
    @SuppressWarnings("null")
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MCTradePostMod.MODID);

    @SuppressWarnings("null")
    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, MCTradePostMod.MODID);

    @SuppressWarnings("null")
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MCTradePostMod.MODID);

    // Create a Deferred Register for chunk-loading ticket types (see GhostCartEntity). Mirrors
    // com.minecolonies.api.util.constant.ModTicketTypes: TicketType instances must be registered
    // through a DeferredRegister, not a bare Registry.register() call at class-static-init time,
    // otherwise the registration can race the registry freeze if the entity class loads late.
    public static final DeferredRegister<TicketType> TICKET_TYPES = DeferredRegister.create(Registries.TICKET_TYPE, MODID);

    // FLAG_LOADING keeps the chunk's blocks loaded; FLAG_SIMULATION additionally keeps entities in
    // it ticking, which the ghost cart needs since it must keep moving even if no player is nearby.
    public static final DeferredHolder<TicketType, TicketType> GHOST_CART_TICKET =
        TICKET_TYPES.register("ghost_cart_follow", () -> new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION));

    // ChunkUtil.RAIL_TICKET: bounded, block-only pull-in used by pathfinding to synchronously inspect
    // track blocks (added then explicitly released by the caller). Matches the old
    // TicketType.create("track_search", comparator, 2) - timeout 2 ticks is just a safety backstop
    // in case the caller fails to release it; FLAG_LOADING only, no entity simulation is needed.
    public static final DeferredHolder<TicketType, TicketType> RAIL_TICKET =
        TICKET_TYPES.register("track_search", () -> new TicketType(2L, TicketType.FLAG_LOADING));

    // ChunkUtil.OUTPOST_TICKET: same bounded add/release pattern as RAIL_TICKET, used by
    // BuildingOutpost to pull in its chunk. Matches the old
    // TicketType.create("mctradepost_outpost", comparator) - no timeout arg, i.e. NO_TIMEOUT.
    public static final DeferredHolder<TicketType, TicketType> OUTPOST_TICKET =
        TICKET_TYPES.register("mctradepost_outpost", () -> new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING));

    // Companion Staff (upstream 64a66a10): TicketType.create("mctradepost_companion", comparator, 20) -> 20-tick
    // loading + simulation ticket, re-added while the companion is active.
    public static final DeferredHolder<TicketType, TicketType> COMPANION_TICKET =
        TICKET_TYPES.register("mctradepost_companion", () -> new TicketType(20L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION));

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(NullnessBridge.assumeNonnull(Registries.MENU), MCTradePostMod.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<com.deathfrog.mctradepost.core.inventory.PetWorkingMenu>> PET_WORKING_MENU =
        MENUS.register("pet_working", () -> IMenuTypeExtension.create(com.deathfrog.mctradepost.core.inventory.PetWorkingMenu::new));

    @SuppressWarnings("null")
    public static final DeferredHolder<MenuType<?>, MenuType<com.deathfrog.mctradepost.core.inventory.MooringBayMenu>> MOORING_BAY_MENU =
        MENUS.register("mooring_bay", () -> IMenuTypeExtension.create((id, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            net.minecraft.world.level.block.entity.BlockEntity be = inv.player.level().getBlockEntity(pos);
            if (be instanceof com.deathfrog.mctradepost.core.blocks.blockentity.MooringBayBlockEntity bay)
                return (com.deathfrog.mctradepost.core.inventory.MooringBayMenu) bay.createMenu(id, inv, inv.player);
            throw new IllegalStateException("Missing Mooring Bay at " + pos);
        }));
    
    @SuppressWarnings("null")
    public static final DeferredHolder<MenuType<?>, MenuType<com.deathfrog.mctradepost.core.inventory.GasifierMenu>> GASIFIER_MENU =
        MENUS.register("gasifier", () -> IMenuTypeExtension.create((id, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            net.minecraft.world.level.block.entity.BlockEntity be = inv.player.level().getBlockEntity(pos);
            if (be instanceof com.deathfrog.mctradepost.core.blocks.blockentity.GasifierBlockEntity gasifier)
                return (com.deathfrog.mctradepost.core.inventory.GasifierMenu) gasifier.createMenu(id, inv, inv.player);
            throw new IllegalStateException("Missing Gasifier at " + pos);
        }));

    public static final DeferredHolder<MenuType<?>, MenuType<com.deathfrog.mctradepost.core.inventory.TradeDockMenu>> TRADE_DOCK_MENU =
        MENUS.register(BlockTradeDock.ID, () -> IMenuTypeExtension.create(com.deathfrog.mctradepost.core.inventory.TradeDockMenu::new));

    public static final String CREATIVE_TRADEPOST_TABNAME = "tradepost";

    /*
     * Items (Non-Block)
     */
    public static final DeferredItem<AdvancedClipboardItem> ADVANCED_CLIPBOARD = registerItem("advanced_clipboard",
        () -> new AdvancedClipboardItem(itemProps().stacksTo(1)));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> ICECREAM = registerItem("icecream",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.6F).build()), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> DAIQUIRI = registerItem("daiquiri",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()).usingConvertsTo(Items.GLASS_BOTTLE), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> VEGGIE_JUICE = registerItem("veggie_juice",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> FRUIT_JUICE = registerItem("fruit_juice",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> PROTIEN_SHAKE = registerItem("protien_shake",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> SMETANA = registerItem("smetana",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> DUMPLINGS_AND_CREAM = registerItem("dumplings_and_cream",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(4).saturationModifier(1.1F).build()), 2));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> BAR_NUTS = registerItem("bar_nuts",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()).usingConvertsTo(Items.BOWL), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> COLD_BREW = registerItem("cold_brew",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()).usingConvertsTo(Items.GLASS_BOTTLE), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> MYSTIC_TEA = registerItem("mystic_tea",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> VANILLA_MILKSHAKE = registerItem("vanilla_milkshake",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> ENERGY_SHAKE = registerItem("energy_shake",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build()), 1));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> PERPETUAL_STEW = registerItem("perpetual_stew",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(6).saturationModifier(3.0F).alwaysEdible().build()).usingConvertsTo(Items.BOWL), 1));

    public static final DeferredItem<WishResurrectionItem> WISH_RESURRECTION = registerItem("wish_resurrection",
        () -> new WishResurrectionItem(itemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> HEARTY_PERPETUAL_STEW = registerItem("hearty_perpetual_stew",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(9).saturationModifier(3.0F).alwaysEdible().build()).usingConvertsTo(Items.BOWL), 2));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> GOURMET_PERPETUAL_STEW = registerItem("gourmet_perpetual_stew",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(13).saturationModifier(3.0F).alwaysEdible().build()).usingConvertsTo(Items.BOWL), 3));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> CHICKPEA_STROGANOFF = registerItem("chickpea_stroganoff",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(7).saturationModifier(2.0F).build()), 2));

    @SuppressWarnings("null")
    public static final DeferredItem<ItemFood> STIR_FRY = registerItem("stir_fry",
        () -> new ItemFood((itemProps()).food(new FoodProperties.Builder().nutrition(7).saturationModifier(2.0F).build()), 2));

    public static final DeferredItem<Item> COPPER_NUGGET = registerItem("copper_nugget",
        () -> new Item(itemProps()));

    public static final DeferredItem<Item> REINFORCED_LEATHER = registerItem("reinforced_leather",
        () -> new Item(itemProps()));

    /**
     * Reinforced leather is intentionally stronger than leather while remaining
     * a level-one MineColonies guard armor. Durability is specified per piece
     * below so the set lasts approximately twice as long as leather.
     */
    // Since 26.1, ArmorMaterial is a plain record rather than a registered object (see
    // com.minecolonies.apiimp.initializer.ModItemsInitializer for the same convention): layer
    // textures moved to an EquipmentAsset key (ModEquipmentAssets) and the repair ingredient is now
    // a TagKey<Item> rather than an Ingredient supplier. Material-level durability is left at 0
    // (unused multiplier) because each piece below sets its own explicit durability, matching how
    // MineColonies' own custom armor materials (e.g. PLATE_ARMOR) are declared.
    @SuppressWarnings("null")
    public static final ArmorMaterial REINFORCED_LEATHER_ARMOR_MATERIAL =
        new ArmorMaterial(
            0,
            Util.make(new EnumMap<>(ArmorType.class), defense -> {
                defense.put(ArmorType.BOOTS, 2);
                defense.put(ArmorType.LEGGINGS, 3);
                defense.put(ArmorType.CHESTPLATE, 4);
                defense.put(ArmorType.HELMET, 2);
            }),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            ModTags.ITEMS.REINFORCED_LEATHER_REPAIR_TAG,
            ModEquipmentAssets.REINFORCED_LEATHER);

    // ArmorItem no longer exists; armor is a plain Item whose Properties carry the Equippable data
    // component via Properties#humanoidArmor(material, type) (see com.minecolonies.core.items.ItemPlateArmor).
    // humanoidArmor() itself sets a placeholder durability from the material (0 here, since our
    // material's own durability field is an unused multiplier - see REINFORCED_LEATHER_ARMOR_MATERIAL
    // above), so the explicit per-piece .durability(N) call must come AFTER it to take effect.
    @SuppressWarnings("null")
    public static final DeferredItem<Item> REINFORCED_LEATHER_HELMET =
        registerItem("reinforced_leather_helmet",
            () -> new Item(itemProps().humanoidArmor(REINFORCED_LEATHER_ARMOR_MATERIAL, ArmorType.HELMET).durability(110)));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> REINFORCED_LEATHER_CHESTPLATE =
        registerItem("reinforced_leather_chestplate",
            () -> new Item(itemProps().humanoidArmor(REINFORCED_LEATHER_ARMOR_MATERIAL, ArmorType.CHESTPLATE).durability(160)));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> REINFORCED_LEATHER_LEGGINGS =
        registerItem("reinforced_leather_leggings",
            () -> new Item(itemProps().humanoidArmor(REINFORCED_LEATHER_ARMOR_MATERIAL, ArmorType.LEGGINGS).durability(150)));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> REINFORCED_LEATHER_BOOTS =
        registerItem("reinforced_leather_boots",
            () -> new Item(itemProps().humanoidArmor(REINFORCED_LEATHER_ARMOR_MATERIAL, ArmorType.BOOTS).durability(130)));

    public static final DeferredItem<Item> NAPKIN = registerItem("napkin",
        () -> new Item(itemProps()));

    public static final DeferredItem<ScrapPileItem> SCRAP_PILE = registerItem("scrap_pile",
        () -> new ScrapPileItem(itemProps()));

    public static final DeferredItem<Item> PET_WASTE = registerItem("pet_waste",
        () -> new Item(itemProps()));

    public static final DeferredItem<Item> END_MORTAR = registerItem("end_mortar",
        () -> new Item(itemProps()));

    public static final DeferredItem<Item> PRISMARINE_MORTAR = registerItem("prismarine_mortar",
        () -> new Item(itemProps()));

    public static final DeferredItem<Item> QUARTZ_MORTAR = registerItem("quartz_mortar",
        () -> new Item(itemProps()));

    public static final DeferredItem<Item> STEW_SEASONING = registerItem("stew_seasoning",
        () -> new Item(itemProps()));

    public static final DeferredItem<Item> WISH_PLENTY = registerItem("wish_plenty",
        () -> new Item(itemProps()));

    public static final DeferredItem<Item> WISH_HEALTH = registerItem("wish_health",
        () -> new Item(itemProps()));

    public static final DeferredItem<WishGatheringItem> WISH_GATHERING_COW = registerItem("wish_gathering_cow",
        () -> new WishGatheringItem(itemProps(), PetTypes.COW));

    public static final DeferredItem<Item> WISH_KNOWLEDGE = registerItem("wish_knowledge",
        () -> new Item(itemProps()));

    public static final DeferredItem<WishGatheringItem> WISH_GATHERING_PIG = registerItem("wish_gathering_pig",
        () -> new WishGatheringItem(itemProps(), PetTypes.PIG));

    public static final DeferredItem<WishGatheringItem> WISH_GATHERING_CHICKEN = registerItem("wish_gathering_chicken",
        () -> new WishGatheringItem(itemProps(), PetTypes.CHICKEN));

    public static final DeferredItem<WishGatheringItem> WISH_GATHERING_SHEEP = registerItem("wish_gathering_sheep",
        () -> new WishGatheringItem(itemProps(), PetTypes.SHEEP));

    public static final DeferredItem<Item> WISH_SHELTER = registerItem("wish_shelter",
        () -> new Item(itemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<ImmersionBlenderItem> IMMERSION_BLENDER = registerItem("immersion_blender",
        () -> new ImmersionBlenderItem(itemProps().durability(100)));

    @SuppressWarnings("null")
    public static final DeferredItem<CurrencyExchangeItem> CURRENCY_EXCHANGE = registerItem("currency_exchange",
        () -> new CurrencyExchangeItem(itemProps().durability(100)));

    @SuppressWarnings("null")
    public static final DeferredItem<SouvenirItem> SOUVENIR = registerItem("souvenir",
        () -> new SouvenirItem(itemProps().component(MCTPModDataComponents.SOUVENIR_COMPONENT.get(), new SouvenirRecord("empty", 0))));

    @SuppressWarnings("null")
    public static final DeferredItem<DimensionalLinkageItem> DIMENSIONAL_LINKAGE = registerItem("dimensional_linkage",
        () -> new DimensionalLinkageItem(itemProps().stacksTo(1)
            .component(MCTPModDataComponents.DIMENSIONAL_LINKAGE.get(), DimensionalLinkageRecord.uninitialized())));

    @SuppressWarnings("null")
    public static final DeferredItem<CompanionStaffItem> COMPANION_STAFF = registerItem(CompanionStaffItem.ID,
        () -> new CompanionStaffItem(itemProps().stacksTo(1).rarity(Rarity.EPIC)));
            
    @SuppressWarnings("null")
    public static final DeferredItem<RouteSurveyItem> ROUTE_SURVEY = registerItem("route_survey",
        () -> new RouteSurveyItem(itemProps().stacksTo(1)));

    @SuppressWarnings("null")
    public static final DeferredItem<NavigationChartItem> NAVIGATION_CHART = registerItem(NavigationChartItem.ID,
        () -> new NavigationChartItem(itemProps().stacksTo(1)
            .component(MCTPModDataComponents.NAVIGATION_CHART.get(), NavigationChartRecord.empty())));

    @SuppressWarnings("null")
    public static final DeferredItem<LiftingGasBucketItem> LIFTING_GAS_BUCKET = registerItem("lifting_gas_bucket",
        () -> new LiftingGasBucketItem(itemProps().stacksTo(1).craftRemainder(Items.BUCKET)));

    @SuppressWarnings("null")
    public static final DeferredItem<CoinItem> MCTP_COIN_ITEM = registerItem("mctp_coin",
        () -> new CoinItem(itemProps().stacksTo(64).rarity(Rarity.UNCOMMON)));

    @SuppressWarnings("null")
    public static final DeferredItem<CoinItem> MCTP_COIN_GOLD = registerItem("mctp_coin_gold",
        () -> new CoinItem(itemProps().stacksTo(64).rarity(Rarity.RARE)));

    @SuppressWarnings("null")
    public static final DeferredItem<CoinItem> MCTP_COIN_DIAMOND = registerItem("mctp_coin_diamond",
        () -> new CoinItem(itemProps().stacksTo(64).rarity(Rarity.EPIC)));

    public static final DeferredItem<OutpostClaimMarkerItem> OUTPOST_CLAIM = registerItem("outpost_claim",
        () -> new OutpostClaimMarkerItem(itemProps()));

    /*
    * ENTITIES
    */
    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<CoinEntity>> COIN_ENTITY_TYPE = ENTITIES.register("coin_entity",
        () -> EntityType.Builder.<CoinEntity>of(CoinEntity::new, MobCategory.MISC)
            .sized(0.20f, 0.20f)    // Size of the entity (like an ItemEntity)
            .clientTrackingRange(8) // Reasonable for item-like entities
            .updateInterval(10)     // Sync rate
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "coin_entity"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<GhostCartEntity>> GHOST_CART = ENTITIES.register("ghost_cart",
        () -> EntityType.Builder.<GhostCartEntity>of(
            GhostCartEntity::new, MobCategory.MISC)
            .sized(0.98f, 0.7f)          // same hitbox as normal cart
            .clientTrackingRange(128)
            .updateInterval(1)            // send pos every tick
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "ghost_cart"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<GhostBoatEntity>> GHOST_BOAT = ENTITIES.register("ghost_boat",
        () -> EntityType.Builder.<GhostBoatEntity>of(GhostBoatEntity::new, MobCategory.MISC)
            .sized(1.375F, 0.7F).clientTrackingRange(128).updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "ghost_boat"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<WagonEntity>> WAGON = ENTITIES.register("wagon",
        () -> EntityType.Builder.<WagonEntity>of(WagonEntity::new, MobCategory.MISC)
            .sized(1.2F, 1.0F).clientTrackingRange(128).updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "wagon"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<AirshipEntity>> AIRSHIP = ENTITIES.register("airship",
        () -> EntityType.Builder.<AirshipEntity>of(AirshipEntity::new, MobCategory.MISC)
            .sized(1.875F, 2.6875F).clientTrackingRange(128).updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "airship"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<PetWolf>> PET_WOLF = ENTITIES.register("pet_wolf",
        () -> EntityType.Builder.of(PetWolf::new, MobCategory.CREATURE)
            .sized(EntityType.WOLF.getDimensions().width(), EntityType.WOLF.getDimensions().height())
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "pet_wolf"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<PetFox>> PET_FOX = ENTITIES.register("pet_fox",
        () -> EntityType.Builder.of(PetFox::new, MobCategory.CREATURE)
            .sized(EntityType.FOX.getDimensions().width(), EntityType.FOX.getDimensions().height())
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "pet_fox"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<PetAxolotl>> PET_AXOLOTL = ENTITIES.register("pet_axolotl",
        () -> EntityType.Builder.of(PetAxolotl::new, MobCategory.CREATURE)
            .sized(EntityType.AXOLOTL.getDimensions().width(), EntityType.AXOLOTL.getDimensions().height())
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "pet_axolotl"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<PetParrot>> PET_PARROT = ENTITIES.register("pet_parrot",
        () -> EntityType.Builder.of(PetParrot::new, MobCategory.CREATURE)
            .sized(EntityType.PARROT.getDimensions().width(), EntityType.PARROT.getDimensions().height())
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "pet_parrot"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<PetCat>> PET_CAT = ENTITIES.register("pet_cat",
        () -> EntityType.Builder.of(PetCat::new, MobCategory.CREATURE)
            .sized(EntityType.CAT.getDimensions().width(), EntityType.CAT.getDimensions().height())
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "pet_cat"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<PetPanda>> PET_PANDA = ENTITIES.register("pet_panda",
        () -> EntityType.Builder.of(PetPanda::new, MobCategory.CREATURE)
            .sized(0.9f, EntityType.PANDA.getDimensions().height())
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "pet_panda"))));

    @SuppressWarnings("null")
    public static final DeferredHolder<EntityType<?>, EntityType<PetDragon>> PET_DRAGON = ENTITIES.register("pet_dragon",
        () -> EntityType.Builder.of(PetDragon::new, MobCategory.CREATURE)
            .sized(0.8F, 1.8F)
            .eyeHeight(1.62F)
            .clientTrackingRange(10)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "pet_dragon"))));

    /*
    * BLOCKS
    */
    // MineColonies' AbstractBlockMinecolonies constructor requires RegistryConstructionContext's own
    // BLOCK_KEY ThreadLocal to be set (it calls applyBlockKey() internally) since these hut blocks
    // build their BlockBehaviour.Properties themselves (AbstractColonyBlock's no-arg constructor),
    // not via this file's blockProps()/blockPropsFrom() helpers.
    public static final DeferredBlock<MCTPBaseBlockHut> blockHutMarketplace = registerBlock(BlockHutMarketplace.HUT_NAME,
        () -> RegistryConstructionContext.constructBlock(Identifier.fromNamespaceAndPath(MODID, BlockHutMarketplace.HUT_NAME), BlockHutMarketplace::new));
    public static final DeferredBlock<MCTPBaseBlockHut> blockHutResort = registerBlock(BlockHutResort.HUT_NAME,
        () -> RegistryConstructionContext.constructBlock(Identifier.fromNamespaceAndPath(MODID, BlockHutResort.HUT_NAME), BlockHutResort::new));
    public static final DeferredBlock<MCTPBaseBlockHut> blockHutRecycling = registerBlock(BlockHutRecycling.HUT_NAME,
        () -> RegistryConstructionContext.constructBlock(Identifier.fromNamespaceAndPath(MODID, BlockHutRecycling.HUT_NAME), BlockHutRecycling::new));
    public static final DeferredBlock<MCTPBaseBlockHut> blockHutStation = registerBlock(BlockHutStation.HUT_NAME,
        () -> RegistryConstructionContext.constructBlock(Identifier.fromNamespaceAndPath(MODID, BlockHutStation.HUT_NAME), BlockHutStation::new));
    public static final DeferredBlock<MCTPBaseBlockHut> blockHutPetShop = registerBlock(BlockHutPetShop.HUT_NAME,
        () -> RegistryConstructionContext.constructBlock(Identifier.fromNamespaceAndPath(MODID, BlockHutPetShop.HUT_NAME), BlockHutPetShop::new));
    public static final DeferredBlock<MCTPBaseBlockHut> blockHutOutpost = registerBlock(BlockHutOutpost.HUT_NAME,
        () -> RegistryConstructionContext.constructBlock(Identifier.fromNamespaceAndPath(MODID, BlockHutOutpost.HUT_NAME), BlockHutOutpost::new));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockMixedStone> MIXED_STONE = registerBlock(BlockMixedStone.MIXED_STONE_ID, () -> new BlockMixedStone(blockProps()
            .mapColor(NullnessBridge.assumeNonnull(MapColor.STONE))
            .strength(2.0f, 6.0f)
            .sound(NullnessBridge.assumeNonnull(SoundType.STONE))));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockTradeDock> TRADE_DOCK = registerBlock(BlockTradeDock.ID,
        () -> new BlockTradeDock(blockProps().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockTradeInterchange> TRADE_INTERCHANGE = registerBlock(BlockTradeInterchange.ID,
        () -> new BlockTradeInterchange(blockProps().mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE)));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockMooringBay> MOORING_BAY = registerBlock(BlockMooringBay.ID,
        () -> new BlockMooringBay(blockProps().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockGasifier> GASIFIER = registerBlock(BlockGasifier.ID,
        () -> new BlockGasifier(blockProps().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)));

    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> MIXED_STONE_STAIRS =
        registerBlock(BlockMixedStone.MIXED_STONE_STAIRS_ID,
            () -> new StairBlock(MIXED_STONE.get().defaultBlockState(),  // base block state supplier
                blockPropsFrom(MIXED_STONE.get())                           // reuse base block's properties
            ));

    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> MIXED_STONE_WALL = registerBlock(BlockMixedStone.MIXED_STONE_WALL_ID, () -> new WallBlock(blockProps()
            .mapColor(MapColor.STONE)
            .strength(2.0f, 6.0f)
            .sound(SoundType.STONE)));

    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> MIXED_STONE_SLAB = registerBlock(BlockMixedStone.MIXED_STONE_SLAB_ID, () -> new SlabBlock(blockProps()
            .mapColor(MapColor.STONE)
            .strength(2.0f, 6.0f)
            .sound(SoundType.STONE)));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> MIXED_STONE_BRICK = registerBlock(ModBlocksInitializer.MIXED_STONE_BRICK_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> MIXED_STONE_BRICK_STAIRS = registerBlock(ModBlocksInitializer.MIXED_STONE_BRICK_STAIRS_NAME,
        () -> new StairBlock(MIXED_STONE_BRICK.get().defaultBlockState(), blockPropsFrom(MIXED_STONE_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> MIXED_STONE_BRICK_WALL =
        registerBlock(ModBlocksInitializer.MIXED_STONE_BRICK_WALL_NAME, () -> new WallBlock(blockPropsFrom(MIXED_STONE_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> MIXED_STONE_BRICK_SLAB =
        registerBlock(ModBlocksInitializer.MIXED_STONE_BRICK_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(MIXED_STONE_BRICK.get())));


    public static final DeferredBlock<BlockDistressed> DISTRESSED = registerBlock(BlockDistressed.DISTRESSED_ID, () -> new BlockDistressed());
    public static final DeferredBlock<BlockStackedSlab> STACKED_SLAB = registerBlock(BlockStackedSlab.STACKED_SLAB_ID, () -> new BlockStackedSlab());
    public static final DeferredBlock<BlockSideSlab> SIDE_SLAB = registerBlock(BlockSideSlab.SIDE_SLAB_ID, () -> new BlockSideSlab());
    public static final DeferredBlock<BlockSideSlabInterleaved> SIDE_SLAB_INTERLEAVED = registerBlock(BlockSideSlabInterleaved.SIDE_SLAB_INTERLEAVED_ID, () -> new BlockSideSlabInterleaved());
    public static final DeferredBlock<BlockGlazed> GLAZED = registerBlock(BlockGlazed.GLAZED_ID, () -> new BlockGlazed());
    public static final DeferredBlock<BlockCheckerboard> CHECKERBOARD = registerBlock(BlockCheckerboard.CHECKERBOARD_ID, () -> new BlockCheckerboard());

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> THATCH = registerBlock(ModBlocksInitializer.THATCH_NAME, () -> new Block(blockPropsFrom(Blocks.HAY_BLOCK)));

    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> THATCH_STAIRS =
        registerBlock(ModBlocksInitializer.THATCH_STAIRS_NAME,
            () -> new StairBlock(THATCH.get().defaultBlockState(),  // base block state supplier
                blockPropsFrom(THATCH.get())                           // reuse base block's properties
            ));

    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> THATCH_WALL = registerBlock(ModBlocksInitializer.THATCH_WALL_NAME, () -> new WallBlock(blockPropsFrom(THATCH.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> THATCH_SLAB = registerBlock(ModBlocksInitializer.THATCH_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(THATCH.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> PLASTER = registerBlock(ModBlocksInitializer.PLASTER_NAME, () -> new Block(blockPropsCopyOf(Blocks.OAK_PLANKS)
            .mapColor(MapColor.TERRACOTTA_WHITE)
            .sound(SoundType.BAMBOO_WOOD)));


    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> PLASTER_STAIRS =
        registerBlock(ModBlocksInitializer.PLASTER_STAIRS_NAME,
            () -> new StairBlock(PLASTER.get().defaultBlockState(),
                blockPropsFrom(PLASTER.get())
            ));

    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> PLASTER_WALL = registerBlock(ModBlocksInitializer.PLASTER_WALL_NAME, () -> new WallBlock(blockPropsFrom(PLASTER.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> PLASTER_SLAB = registerBlock(ModBlocksInitializer.PLASTER_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(PLASTER.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> QUARTZ_PLASTER = registerBlock(ModBlocksInitializer.QUARTZ_PLASTER_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> QUARTZ_PLASTER_STAIRS = registerBlock(ModBlocksInitializer.QUARTZ_PLASTER_STAIRS_NAME,
        () -> new StairBlock(QUARTZ_PLASTER.get().defaultBlockState(), blockPropsFrom(QUARTZ_PLASTER.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> QUARTZ_PLASTER_WALL =
        registerBlock(ModBlocksInitializer.QUARTZ_PLASTER_WALL_NAME, () -> new WallBlock(blockPropsFrom(QUARTZ_PLASTER.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> QUARTZ_PLASTER_SLAB =
        registerBlock(ModBlocksInitializer.QUARTZ_PLASTER_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(QUARTZ_PLASTER.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> ROUGH_STONE = registerBlock(ModBlocksInitializer.ROUGH_STONE_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> ROUGH_STONE_STAIRS = registerBlock(ModBlocksInitializer.ROUGH_STONE_STAIRS_NAME,
        () -> new StairBlock(ROUGH_STONE.get().defaultBlockState(), blockPropsFrom(ROUGH_STONE.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> ROUGH_STONE_WALL =
        registerBlock(ModBlocksInitializer.ROUGH_STONE_WALL_NAME, () -> new WallBlock(blockPropsFrom(ROUGH_STONE.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> ROUGH_STONE_SLAB =
        registerBlock(ModBlocksInitializer.ROUGH_STONE_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(ROUGH_STONE.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> ROUGH_BRICK = registerBlock(ModBlocksInitializer.ROUGH_BRICK_NAME, () -> new Block(blockProps()
            .mapColor(MapColor.STONE)
            .strength(1.5f, 2.0f)
            .sound(SoundType.STONE)));


    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> ROUGH_BRICK_STAIRS =
        registerBlock(ModBlocksInitializer.ROUGH_BRICK_STAIRS_NAME,
            () -> new StairBlock(ROUGH_BRICK.get().defaultBlockState(),  // base block state supplier
                blockPropsFrom(ROUGH_BRICK.get())                           // reuse base block's properties
            ));

    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> ROUGH_BRICK_WALL = registerBlock(ModBlocksInitializer.ROUGH_BRICK_WALL_NAME, () -> new WallBlock(blockPropsFrom(ROUGH_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> ROUGH_BRICK_SLAB = registerBlock(ModBlocksInitializer.ROUGH_BRICK_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(ROUGH_BRICK.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> DIAGONAL_BRICK = registerBlock(ModBlocksInitializer.DIAGONAL_BRICK_NAME, () -> new Block(blockProps()
            .mapColor(MapColor.STONE)
            .strength(1.5f, 2.0f)
            .sound(SoundType.STONE)));


    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> DIAGONAL_BRICK_STAIRS =
        registerBlock(ModBlocksInitializer.DIAGONAL_BRICK_STAIRS_NAME,
            () -> new StairBlock(DIAGONAL_BRICK.get().defaultBlockState(),  // base block state supplier
                blockPropsFrom(DIAGONAL_BRICK.get())                           // reuse base block's properties
            ));

    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> DIAGONAL_BRICK_WALL = registerBlock(ModBlocksInitializer.DIAGONAL_BRICK_WALL_NAME, () -> new WallBlock(blockPropsFrom(DIAGONAL_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> DIAGONAL_BRICK_SLAB = registerBlock(ModBlocksInitializer.DIAGONAL_BRICK_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(DIAGONAL_BRICK.get())));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MIXED_STONE_ITEM =
        registerItem(BlockMixedStone.MIXED_STONE_ID, () -> new BlockItem(MIXED_STONE.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> TRADE_DOCK_ITEM =
        registerItem(BlockTradeDock.ID, () -> new BlockItem(TRADE_DOCK.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> TRADE_INTERCHANGE_ITEM =
        registerItem(BlockTradeInterchange.ID, () -> new BlockItem(TRADE_INTERCHANGE.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MOORING_BAY_ITEM =
        registerItem(BlockMooringBay.ID, () -> new BlockItem(MOORING_BAY.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> GASIFIER_ITEM =
        registerItem(BlockGasifier.ID, () -> new BlockItem(GASIFIER.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MIXED_STONE_STAIRS_ITEM =
        registerItem(BlockMixedStone.MIXED_STONE_STAIRS_ID, () -> new BlockItem(MIXED_STONE_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MIXED_STONE_WALL_ITEM =
        registerItem(BlockMixedStone.MIXED_STONE_WALL_ID, () -> new BlockItem(MIXED_STONE_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MIXED_STONE_SLAB_ITEM =
        registerItem(BlockMixedStone.MIXED_STONE_SLAB_ID, () -> new BlockItem(MIXED_STONE_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MIXED_STONE_BRICK_ITEM =
        registerItem(ModBlocksInitializer.MIXED_STONE_BRICK_NAME, () -> new BlockItem(MIXED_STONE_BRICK.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MIXED_STONE_BRICK_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.MIXED_STONE_BRICK_STAIRS_NAME, () -> new BlockItem(MIXED_STONE_BRICK_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MIXED_STONE_BRICK_WALL_ITEM =
        registerItem(ModBlocksInitializer.MIXED_STONE_BRICK_WALL_NAME, () -> new BlockItem(MIXED_STONE_BRICK_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MIXED_STONE_BRICK_SLAB_ITEM =
        registerItem(ModBlocksInitializer.MIXED_STONE_BRICK_SLAB_NAME, () -> new BlockItem(MIXED_STONE_BRICK_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<BlockDistressedItem> DISTRESSED_ITEM =
        registerItem(BlockDistressed.DISTRESSED_ID, () -> new BlockDistressedItem(DISTRESSED.get(), itemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<BlockStackedSlabItem> STACKED_SLAB_ITEM =
        registerItem(BlockStackedSlab.STACKED_SLAB_ID, () -> new BlockStackedSlabItem(STACKED_SLAB.get(), itemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<BlockSideSlabItem> SIDE_SLAB_ITEM =
        registerItem(BlockSideSlab.SIDE_SLAB_ID, () -> new BlockSideSlabItem(SIDE_SLAB.get(), itemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<BlockSideSlabInterleavedItem> SIDE_SLAB_INTERLEAVED_ITEM =
        registerItem(BlockSideSlabInterleaved.SIDE_SLAB_INTERLEAVED_ID, () -> new BlockSideSlabInterleavedItem(SIDE_SLAB_INTERLEAVED.get(), itemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<BlockGlazedItem> GLAZED_ITEM =
        registerItem(BlockGlazed.GLAZED_ID, () -> new BlockGlazedItem(GLAZED.get(), itemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<BlockCheckerboardItem> CHECKERBOARD_ITEM =
        registerItem(BlockCheckerboard.CHECKERBOARD_ID, () -> new BlockCheckerboardItem(CHECKERBOARD.get(), itemProps()));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> ENDETHYST = registerBlock(ModBlocksInitializer.ENDETHYST_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(2.5f, 3.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> ENDETHYST_STAIRS = registerBlock(ModBlocksInitializer.ENDETHYST_STAIRS_NAME,
        () -> new StairBlock(ENDETHYST.get().defaultBlockState(), blockPropsFrom(ENDETHYST.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> ENDETHYST_WALL =
        registerBlock(ModBlocksInitializer.ENDETHYST_WALL_NAME, () -> new WallBlock(blockPropsFrom(ENDETHYST.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> ENDETHYST_SLAB =
        registerBlock(ModBlocksInitializer.ENDETHYST_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(ENDETHYST.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> ENDETHYST_BRICK = registerBlock(ModBlocksInitializer.ENDETHYST_BRICK_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> ENDETHYST_BRICK_STAIRS = registerBlock(ModBlocksInitializer.ENDETHYST_BRICK_STAIRS_NAME,
        () -> new StairBlock(ENDETHYST_BRICK.get().defaultBlockState(), blockPropsFrom(ENDETHYST_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> ENDETHYST_BRICK_WALL =
        registerBlock(ModBlocksInitializer.ENDETHYST_BRICK_WALL_NAME, () -> new WallBlock(blockPropsFrom(ENDETHYST_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> ENDETHYST_BRICK_SLAB =
        registerBlock(ModBlocksInitializer.ENDETHYST_BRICK_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(ENDETHYST_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<Block> ENDMARINE = registerBlock(ModBlocksInitializer.ENDMARINE_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> ENDMARINE_STAIRS = registerBlock(ModBlocksInitializer.ENDMARINE_STAIRS_NAME,
        () -> new StairBlock(ENDMARINE.get().defaultBlockState(), blockPropsFrom(ENDMARINE.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> ENDMARINE_WALL =
        registerBlock(ModBlocksInitializer.ENDMARINE_WALL_NAME, () -> new WallBlock(blockPropsFrom(ENDMARINE.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> ENDMARINE_SLAB =
        registerBlock(ModBlocksInitializer.ENDMARINE_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(ENDMARINE.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> ENDMARINE_BRICK = registerBlock(ModBlocksInitializer.ENDMARINE_BRICK_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> ENDMARINE_BRICK_STAIRS = registerBlock(ModBlocksInitializer.ENDMARINE_BRICK_STAIRS_NAME,
        () -> new StairBlock(ENDMARINE_BRICK.get().defaultBlockState(), blockPropsFrom(ENDMARINE_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> ENDMARINE_BRICK_WALL =
        registerBlock(ModBlocksInitializer.ENDMARINE_BRICK_WALL_NAME, () -> new WallBlock(blockPropsFrom(ENDMARINE_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> ENDMARINE_BRICK_SLAB =
        registerBlock(ModBlocksInitializer.ENDMARINE_BRICK_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(ENDMARINE_BRICK.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> MARINE_LAPIS = registerBlock(ModBlocksInitializer.MARINE_LAPIS_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> MARINE_LAPIS_STAIRS = registerBlock(ModBlocksInitializer.MARINE_LAPIS_STAIRS_NAME,
        () -> new StairBlock(MARINE_LAPIS.get().defaultBlockState(), blockPropsFrom(MARINE_LAPIS.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> MARINE_LAPIS_WALL =
        registerBlock(ModBlocksInitializer.MARINE_LAPIS_WALL_NAME, () -> new WallBlock(blockPropsFrom(MARINE_LAPIS.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> MARINE_LAPIS_SLAB =
        registerBlock(ModBlocksInitializer.MARINE_LAPIS_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(MARINE_LAPIS.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> MARINE_LAPIS_BRICK = registerBlock(ModBlocksInitializer.MARINE_LAPIS_BRICK_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> MARINE_LAPIS_BRICK_STAIRS = registerBlock(ModBlocksInitializer.MARINE_LAPIS_BRICK_STAIRS_NAME,
        () -> new StairBlock(MARINE_LAPIS_BRICK.get().defaultBlockState(), blockPropsFrom(MARINE_LAPIS_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> MARINE_LAPIS_BRICK_WALL =
        registerBlock(ModBlocksInitializer.MARINE_LAPIS_BRICK_WALL_NAME, () -> new WallBlock(blockPropsFrom(MARINE_LAPIS_BRICK.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> MARINE_LAPIS_BRICK_SLAB =
        registerBlock(ModBlocksInitializer.MARINE_LAPIS_BRICK_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(MARINE_LAPIS_BRICK.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> BLOCK_IVY_BRICK =
        registerBlock(ModBlocksInitializer.IVY_BRICK_NAME, () -> new Block(blockPropsFrom(Blocks.VINE)));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> WEATHERED_ROUGH_STONE = registerBlock(ModBlocksInitializer.WEATHERED_ROUGH_STONE_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> WEATHERED_ROUGH_STONE_STAIRS = registerBlock(ModBlocksInitializer.WEATHERED_ROUGH_STONE_STAIRS_NAME,
        () -> new StairBlock(WEATHERED_ROUGH_STONE.get().defaultBlockState(), blockPropsFrom(WEATHERED_ROUGH_STONE.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> WEATHERED_ROUGH_STONE_WALL =
        registerBlock(ModBlocksInitializer.WEATHERED_ROUGH_STONE_WALL_NAME, () -> new WallBlock(blockPropsFrom(WEATHERED_ROUGH_STONE.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> WEATHERED_ROUGH_STONE_SLAB =
        registerBlock(ModBlocksInitializer.WEATHERED_ROUGH_STONE_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(WEATHERED_ROUGH_STONE.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> MARINE_BASALT = registerBlock(ModBlocksInitializer.MARINE_BASALT_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> MARINE_BASALT_STAIRS = registerBlock(ModBlocksInitializer.MARINE_BASALT_STAIRS_NAME,
        () -> new StairBlock(MARINE_BASALT.get().defaultBlockState(), blockPropsFrom(MARINE_BASALT.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> MARINE_BASALT_WALL =
        registerBlock(ModBlocksInitializer.MARINE_BASALT_WALL_NAME, () -> new WallBlock(blockPropsFrom(MARINE_BASALT.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> MARINE_BASALT_SLAB =
        registerBlock(ModBlocksInitializer.MARINE_BASALT_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(MARINE_BASALT.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockTrough> TROUGH =
        registerBlock(ModBlocksInitializer.TROUGH_NAME, () -> new BlockTrough(blockPropsFrom(THATCH.get()).lightLevel(state -> 4)));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockScavenge> SCAVENGE =
        registerBlock(ModBlocksInitializer.SCAVENGE_NAME, () -> new BlockScavenge(blockPropsFrom(THATCH.get()).lightLevel(state -> 4)));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockDredger> DREDGER =
        registerBlock(ModBlocksInitializer.DREDGER_NAME, () -> new BlockDredger(blockPropsFrom(THATCH.get()).lightLevel(state -> 4)));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockFeeder> FEEDER =
        registerBlock(ModBlocksInitializer.FEEDER_NAME, () -> new BlockFeeder(blockPropsFrom(THATCH.get()).lightLevel(state -> 4)));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockHauler> HAULER =
        registerBlock(ModBlocksInitializer.HAULER_NAME, () -> new BlockHauler(blockPropsFrom(THATCH.get()).noOcclusion().lightLevel(state -> 4)));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> WOVEN_KELP = registerBlock(ModBlocksInitializer.WOVEN_KELP_NAME,
        () -> new Block(blockProps().mapColor(MapColor.STONE).strength(1.5f, 2.0f).sound(SoundType.STONE)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> WOVEN_KELP_STAIRS = registerBlock(ModBlocksInitializer.WOVEN_KELP_STAIRS_NAME,
        () -> new StairBlock(WOVEN_KELP.get().defaultBlockState(), blockPropsFrom(WOVEN_KELP.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> WOVEN_KELP_WALL =
        registerBlock(ModBlocksInitializer.WOVEN_KELP_WALL_NAME, () -> new WallBlock(blockPropsFrom(WOVEN_KELP.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> WOVEN_KELP_SLAB =
        registerBlock(ModBlocksInitializer.WOVEN_KELP_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(WOVEN_KELP.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<Block> CHITIN = registerBlock(ModBlocksInitializer.CHITIN_NAME,
        () -> new Block(blockPropsCopyOf(Blocks.OAK_PLANKS)));
    @SuppressWarnings("null")
    public static final DeferredBlock<StairBlock> CHITIN_STAIRS = registerBlock(ModBlocksInitializer.CHITIN_STAIRS_NAME,
        () -> new StairBlock(CHITIN.get().defaultBlockState(), blockPropsFrom(CHITIN.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<WallBlock> CHITIN_WALL =
        registerBlock(ModBlocksInitializer.CHITIN_WALL_NAME, () -> new WallBlock(blockPropsFrom(CHITIN.get())));
    @SuppressWarnings("null")
    public static final DeferredBlock<SlabBlock> CHITIN_SLAB =
        registerBlock(ModBlocksInitializer.CHITIN_SLAB_NAME, () -> new SlabBlock(blockPropsFrom(CHITIN.get())));

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockOutpostMarker> BLOCK_OUTPOST_MARKER =
        registerBlock(ModBlocksInitializer.BLOCK_OUTPOST_MARKER_NAME, () -> new BlockOutpostMarker(blockPropsFrom(Blocks.BLACK_BANNER)));

    public static final DeferredBlock<StewpotBlock> STEWPOT_FILLED =
        registerBlock(ModBlocksInitializer.STEWPOT_FILLED_NAME,
            () -> new StewpotBlock(blockProps()
                .strength(2.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
            )
        );

    @SuppressWarnings("null")
    public static final DeferredBlock<BlockLamp> BLOCK_LAMP =
        registerBlock(ModBlocksInitializer.LAMP,
            () -> new BlockLamp(
                blockProps()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F, 4.0F)
                    .sound(SoundType.LANTERN)
                    .lightLevel(state -> 15)
                    .noOcclusion()
            )
        );

    /*
    * ITEMS (Block)
    */
    @SuppressWarnings("null")
    public static final DeferredItem<Item> THATCH_ITEM =
        registerItem(ModBlocksInitializer.THATCH_NAME, () -> new BlockItem(THATCH.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> THATCH_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.THATCH_STAIRS_NAME, () -> new BlockItem(THATCH_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> THATCH_WALL_ITEM =
        registerItem(ModBlocksInitializer.THATCH_WALL_NAME, () -> new BlockItem(THATCH_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> THATCH_SLAB_ITEM =
        registerItem(ModBlocksInitializer.THATCH_SLAB_NAME, () -> new BlockItem(THATCH_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> PLASTER_ITEM =
        registerItem(ModBlocksInitializer.PLASTER_NAME, () -> new BlockItem(PLASTER.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> PLASTER_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.PLASTER_STAIRS_NAME, () -> new BlockItem(PLASTER_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> PLASTER_WALL_ITEM =
        registerItem(ModBlocksInitializer.PLASTER_WALL_NAME, () -> new BlockItem(PLASTER_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> PLASTER_SLAB_ITEM =
        registerItem(ModBlocksInitializer.PLASTER_SLAB_NAME, () -> new BlockItem(PLASTER_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> QUARTZ_PLASTER_ITEM =
        registerItem(ModBlocksInitializer.QUARTZ_PLASTER_NAME, () -> new BlockItem(QUARTZ_PLASTER.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> QUARTZ_PLASTER_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.QUARTZ_PLASTER_STAIRS_NAME, () -> new BlockItem(QUARTZ_PLASTER_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> QUARTZ_PLASTER_WALL_ITEM =
        registerItem(ModBlocksInitializer.QUARTZ_PLASTER_WALL_NAME, () -> new BlockItem(QUARTZ_PLASTER_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> QUARTZ_PLASTER_SLAB_ITEM =
        registerItem(ModBlocksInitializer.QUARTZ_PLASTER_SLAB_NAME, () -> new BlockItem(QUARTZ_PLASTER_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ROUGH_STONE_ITEM =
        registerItem(ModBlocksInitializer.ROUGH_STONE_NAME, () -> new BlockItem(ROUGH_STONE.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ROUGH_STONE_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.ROUGH_STONE_STAIRS_NAME, () -> new BlockItem(ROUGH_STONE_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ROUGH_STONE_WALL_ITEM =
        registerItem(ModBlocksInitializer.ROUGH_STONE_WALL_NAME, () -> new BlockItem(ROUGH_STONE_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ROUGH_STONE_SLAB_ITEM =
        registerItem(ModBlocksInitializer.ROUGH_STONE_SLAB_NAME, () -> new BlockItem(ROUGH_STONE_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ROUGH_BRICK_ITEM =
        registerItem(ModBlocksInitializer.ROUGH_BRICK_NAME, () -> new BlockItem(ROUGH_BRICK.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ROUGH_BRICK_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.ROUGH_BRICK_STAIRS_NAME, () -> new BlockItem(ROUGH_BRICK_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ROUGH_BRICK_WALL_ITEM =
        registerItem(ModBlocksInitializer.ROUGH_BRICK_WALL_NAME, () -> new BlockItem(ROUGH_BRICK_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ROUGH_BRICK_SLAB_ITEM =
        registerItem(ModBlocksInitializer.ROUGH_BRICK_SLAB_NAME, () -> new BlockItem(ROUGH_BRICK_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> DIAGONAL_BRICK_ITEM =
        registerItem(ModBlocksInitializer.DIAGONAL_BRICK_NAME, () -> new BlockItem(DIAGONAL_BRICK.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> DIAGONAL_BRICK_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.DIAGONAL_BRICK_STAIRS_NAME, () -> new BlockItem(DIAGONAL_BRICK_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> DIAGONAL_BRICK_WALL_ITEM =
        registerItem(ModBlocksInitializer.DIAGONAL_BRICK_WALL_NAME, () -> new BlockItem(DIAGONAL_BRICK_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> DIAGONAL_BRICK_SLAB_ITEM =
        registerItem(ModBlocksInitializer.DIAGONAL_BRICK_SLAB_NAME, () -> new BlockItem(DIAGONAL_BRICK_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDETHYST_ITEM =
        registerItem(ModBlocksInitializer.ENDETHYST_NAME, () -> new BlockItem(ENDETHYST.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDETHYST_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.ENDETHYST_STAIRS_NAME, () -> new BlockItem(ENDETHYST_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDETHYST_WALL_ITEM =
        registerItem(ModBlocksInitializer.ENDETHYST_WALL_NAME, () -> new BlockItem(ENDETHYST_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDETHYST_SLAB_ITEM =
        registerItem(ModBlocksInitializer.ENDETHYST_SLAB_NAME, () -> new BlockItem(ENDETHYST_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDETHYST_BRICK_ITEM =
        registerItem(ModBlocksInitializer.ENDETHYST_BRICK_NAME, () -> new BlockItem(ENDETHYST_BRICK.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDETHYST_BRICK_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.ENDETHYST_BRICK_STAIRS_NAME, () -> new BlockItem(ENDETHYST_BRICK_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDETHYST_BRICK_WALL_ITEM =
        registerItem(ModBlocksInitializer.ENDETHYST_BRICK_WALL_NAME, () -> new BlockItem(ENDETHYST_BRICK_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDETHYST_BRICK_SLAB_ITEM =
        registerItem(ModBlocksInitializer.ENDETHYST_BRICK_SLAB_NAME, () -> new BlockItem(ENDETHYST_BRICK_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDMARINE_ITEM =
        registerItem(ModBlocksInitializer.ENDMARINE_NAME, () -> new BlockItem(ENDMARINE.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDMARINE_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.ENDMARINE_STAIRS_NAME, () -> new BlockItem(ENDMARINE_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDMARINE_WALL_ITEM =
        registerItem(ModBlocksInitializer.ENDMARINE_WALL_NAME, () -> new BlockItem(ENDMARINE_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDMARINE_SLAB_ITEM =
        registerItem(ModBlocksInitializer.ENDMARINE_SLAB_NAME, () -> new BlockItem(ENDMARINE_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDMARINE_BRICK_ITEM =
        registerItem(ModBlocksInitializer.ENDMARINE_BRICK_NAME, () -> new BlockItem(ENDMARINE_BRICK.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDMARINE_BRICK_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.ENDMARINE_BRICK_STAIRS_NAME, () -> new BlockItem(ENDMARINE_BRICK_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDMARINE_BRICK_WALL_ITEM =
        registerItem(ModBlocksInitializer.ENDMARINE_BRICK_WALL_NAME, () -> new BlockItem(ENDMARINE_BRICK_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> ENDMARINE_BRICK_SLAB_ITEM =
        registerItem(ModBlocksInitializer.ENDMARINE_BRICK_SLAB_NAME, () -> new BlockItem(ENDMARINE_BRICK_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_LAPIS_ITEM =
        registerItem(ModBlocksInitializer.MARINE_LAPIS_NAME, () -> new BlockItem(MARINE_LAPIS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_LAPIS_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.MARINE_LAPIS_STAIRS_NAME, () -> new BlockItem(MARINE_LAPIS_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_LAPIS_WALL_ITEM =
        registerItem(ModBlocksInitializer.MARINE_LAPIS_WALL_NAME, () -> new BlockItem(MARINE_LAPIS_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_LAPIS_SLAB_ITEM =
        registerItem(ModBlocksInitializer.MARINE_LAPIS_SLAB_NAME, () -> new BlockItem(MARINE_LAPIS_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_LAPIS_BRICK_ITEM =
        registerItem(ModBlocksInitializer.MARINE_LAPIS_BRICK_NAME, () -> new BlockItem(MARINE_LAPIS_BRICK.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_LAPIS_BRICK_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.MARINE_LAPIS_BRICK_STAIRS_NAME, () -> new BlockItem(MARINE_LAPIS_BRICK_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_LAPIS_BRICK_WALL_ITEM =
        registerItem(ModBlocksInitializer.MARINE_LAPIS_BRICK_WALL_NAME, () -> new BlockItem(MARINE_LAPIS_BRICK_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_LAPIS_BRICK_SLAB_ITEM =
        registerItem(ModBlocksInitializer.MARINE_LAPIS_BRICK_SLAB_NAME, () -> new BlockItem(MARINE_LAPIS_BRICK_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> IVY_BRICK_ITEM =
        registerItem(ModBlocksInitializer.IVY_BRICK_NAME, () -> new BlockItem(BLOCK_IVY_BRICK.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> WEATHERED_ROUGH_STONE_ITEM =
        registerItem(ModBlocksInitializer.WEATHERED_ROUGH_STONE_NAME, () -> new BlockItem(WEATHERED_ROUGH_STONE.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> WEATHERED_ROUGH_STONE_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.WEATHERED_ROUGH_STONE_STAIRS_NAME, () -> new BlockItem(WEATHERED_ROUGH_STONE_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> WEATHERED_ROUGH_STONE_WALL_ITEM =
        registerItem(ModBlocksInitializer.WEATHERED_ROUGH_STONE_WALL_NAME, () -> new BlockItem(WEATHERED_ROUGH_STONE_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> WEATHERED_ROUGH_STONE_SLAB_ITEM =
        registerItem(ModBlocksInitializer.WEATHERED_ROUGH_STONE_SLAB_NAME, () -> new BlockItem(WEATHERED_ROUGH_STONE_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_BASALT_ITEM =
        registerItem(ModBlocksInitializer.MARINE_BASALT_NAME, () -> new BlockItem(MARINE_BASALT.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_BASALT_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.MARINE_BASALT_STAIRS_NAME, () -> new BlockItem(MARINE_BASALT_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_BASALT_WALL_ITEM =
        registerItem(ModBlocksInitializer.MARINE_BASALT_WALL_NAME, () -> new BlockItem(MARINE_BASALT_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> MARINE_BASALT_SLAB_ITEM =
        registerItem(ModBlocksInitializer.MARINE_BASALT_SLAB_NAME, () -> new BlockItem(MARINE_BASALT_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> TROUGH_ITEM =
        registerItem(ModBlocksInitializer.TROUGH_NAME, () -> new PetWorkingLocationBlockItem(TROUGH.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> SCAVENGE_ITEM =
        registerItem(ModBlocksInitializer.SCAVENGE_NAME, () -> new PetWorkingLocationBlockItem(SCAVENGE.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> DREDGER_ITEM =
        registerItem(ModBlocksInitializer.DREDGER_NAME, () -> new PetWorkingLocationBlockItem(DREDGER.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> FEEDER_ITEM =
        registerItem(ModBlocksInitializer.FEEDER_NAME, () -> new PetWorkingLocationBlockItem(FEEDER.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> HAULER_ITEM =
        registerItem(ModBlocksInitializer.HAULER_NAME, () -> new PetWorkingLocationBlockItem(HAULER.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> WOVEN_KELP_ITEM =
        registerItem(ModBlocksInitializer.WOVEN_KELP_NAME, () -> new BlockItem(WOVEN_KELP.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> WOVEN_KELP_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.WOVEN_KELP_STAIRS_NAME, () -> new BlockItem(WOVEN_KELP_STAIRS.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> WOVEN_KELP_WALL_ITEM =
        registerItem(ModBlocksInitializer.WOVEN_KELP_WALL_NAME, () -> new BlockItem(WOVEN_KELP_WALL.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> WOVEN_KELP_SLAB_ITEM =
        registerItem(ModBlocksInitializer.WOVEN_KELP_SLAB_NAME, () -> new BlockItem(WOVEN_KELP_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> CHITIN_ITEM =
        registerItem(ModBlocksInitializer.CHITIN_NAME, () -> new BlockItem(CHITIN.get(), blockItemProps()));
    @SuppressWarnings("null")
    public static final DeferredItem<Item> CHITIN_STAIRS_ITEM =
        registerItem(ModBlocksInitializer.CHITIN_STAIRS_NAME, () -> new BlockItem(CHITIN_STAIRS.get(), blockItemProps()));
    @SuppressWarnings("null")
    public static final DeferredItem<Item> CHITIN_WALL_ITEM =
        registerItem(ModBlocksInitializer.CHITIN_WALL_NAME, () -> new BlockItem(CHITIN_WALL.get(), blockItemProps()));
    @SuppressWarnings("null")
    public static final DeferredItem<Item> CHITIN_SLAB_ITEM =
        registerItem(ModBlocksInitializer.CHITIN_SLAB_NAME, () -> new BlockItem(CHITIN_SLAB.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> STEWPOT_FILLED_ITEM =
        registerItem(ModBlocksInitializer.STEWPOT_FILLED_NAME, () -> new BlockItem(STEWPOT_FILLED.get(), blockItemProps()));

    @SuppressWarnings("null")
    public static final DeferredItem<Item> BLOCK_LAMP_ITEM =
        registerItem(ModBlocksInitializer.LAMP, () -> new BlockItem(BLOCK_LAMP.get(), blockItemProps()));
    /*
    * Creative Mode Tabs
    */
    @SuppressWarnings("null")
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    @SuppressWarnings("null")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRADEPOST_TAB = CREATIVE_MODE_TABS.register(CREATIVE_TRADEPOST_TABNAME, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup:mctradepost")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> MCTP_COIN_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(MCTP_COIN_ITEM.get());
                output.accept(MCTP_COIN_GOLD.get());
                output.accept(MCTP_COIN_DIAMOND.get());
                output.accept(COMPANION_STAFF.get());
                output.accept(ROUTE_SURVEY.get());
                output.accept(NAVIGATION_CHART.get());
                output.accept(GASIFIER_ITEM.get());
                output.accept(LIFTING_GAS_BUCKET.get());
                output.accept(SCRAP_PILE.get());
                output.accept(PET_WASTE.get());
            }).build());

    /*
     * RECIPES
     */
    @SuppressWarnings("null")
    public static final DeferredHolder<RecipeType<?>, RecipeType<DeconstructionRecipe>> DECON_RECIPE_TYPE =
        MCTradePostMod.RECIPES.register(
            DeconstructionRecipe.DECON_RECIPE_KEY,
            () -> RecipeType.<DeconstructionRecipe>simple(
                Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, DeconstructionRecipe.DECON_RECIPE_KEY)
            )
        );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DeconstructionRecipe>> DECON_RECIPE_SERIALIZER =
        MCTradePostMod.RECIPE_SERIALIZERS.register(
            DeconstructionRecipe.DECON_RECIPE_KEY,
            () -> DeconstructionRecipe.Serializer.INSTANCE
        );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<UniqueTagShapelessRecipe>> UNIQUE_TAG_SHAPELESS_SERIALIZER =
        RECIPE_SERIALIZERS.register(UniqueTagShapelessRecipe.UNIQUE_TAG_SHAPELESS_RECIPE_KEY, () -> UniqueTagShapelessRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PotionShapelessRecipe>> POTION_SHAPELESS_SERIALIZER =
        RECIPE_SERIALIZERS.register(PotionShapelessRecipe.POTION_SHAPELESS_RECIPE_KEY, () -> PotionShapelessRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DurableToolShapelessRecipe>> DURABLE_TOOL_SHAPELESS_SERIALIZER =
        RECIPE_SERIALIZERS.register(DurableToolShapelessRecipe.DURABLE_TOOL_SHAPELESS_RECIPE_KEY, () -> DurableToolShapelessRecipe.Serializer.INSTANCE);

    @SuppressWarnings("null")
    public static final DeferredHolder<RecipeType<?>, RecipeType<GasifierRecipe>> GASIFIER_RECIPE_TYPE =
        RECIPES.register(GasifierRecipe.ID, () -> RecipeType.simple(Identifier.fromNamespaceAndPath(MODID, GasifierRecipe.ID)));
        
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GasifierRecipe>> GASIFIER_RECIPE_SERIALIZER =
        RECIPE_SERIALIZERS.register(GasifierRecipe.ID, () -> GasifierRecipe.Serializer.INSTANCE);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MCTradePostMod(@Nonnull IEventBus modEventBus, ModContainer modContainer)
    {

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        MCTPFluids.FLUID_TYPES.register(modEventBus);
        MCTPFluids.FLUIDS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so entities get registered
        ENTITIES.register(modEventBus);

        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register custom advancements
        MCTPAdvancementTriggers.DEFERRED_REGISTER.register(modEventBus);

        // Register recipe support
        RECIPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        TICKET_TYPES.register(modEventBus);
        MENUS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        // NeoForge.EVENT_BUS.register(RitualReloadListener.class);
        NeoForge.EVENT_BUS.register(BurnoutRemedyManager.class);
        NeoForge.EVENT_BUS.register(RecyclingBlacklistManager.class);
        NeoForge.EVENT_BUS.register(RareFindBlacklistManager.class);

        // Add a listener for the common setup event.
        modEventBus.addListener(this::onCommonSetup);

        // Add a listener for entities joining the world.
        NeoForge.EVENT_BUS.addListener(MCTradePostMod::onEntityJoinWorld);
        NeoForge.EVENT_BUS.addListener(MCTradePostMod::onItemExpire);


        // Add a listener for the completion of the load.
        modEventBus.addListener(this::onLoadComplete);

        // Add a listener for default data components being bound (see onDefaultDataComponentsBound).
        // Fired on the global game bus (NeoForge.EVENT_BUS), not the per-mod bus -- matches how
        // MineColonies itself registers FMLEventHandler.onDefaultDataComponentsBound.
        NeoForge.EVENT_BUS.addListener(this::onDefaultDataComponentsBound);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        MCTPConfig.register(modContainer);
        clientSideInitializations(modContainer);                            // Environment enforcement is handled inside this method.

        ModJobsInitializer.DEFERRED_REGISTER.register(modEventBus);
        TileEntityInitializer.BLOCK_ENTITIES.register(modEventBus);
        // ModBuildingsInitializer.DEFERRED_REGISTER.register(modEventBus);  // NETSYNC: Building registration at this point with static initialization, server load fails due to early access of objects.
        MCTPModSoundEvents.SOUND_EVENTS.register(modEventBus);

        // Register our data components.
        MCTPModDataComponents.REGISTRAR.register(modEventBus);

        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);

        LOGGER.info("MCTradePost mod initialized.");
    }

    /**
     * This method is called on the client side only.
     * It is responsible for setting up the client side configuration screen.
     * The configuration screen is created by registering a IConfigScreenFactory
     * with the mod container. This factory is used to create the configuration screen
     * for this mod.
     */
    private void clientSideInitializations(ModContainer modContainer) {
        // This will use NeoForge's ConfigurationScreen to display this mod's configs
        if (FMLEnvironment.getDist().isClient()) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    /**
     * This method is called on both the client and server side during the mod's initialization.
     * It is responsible for setting up and initializing any code that needs to be shared between
     * the client and server. This includes things like setting up custom damage sources,
     * registering capabilities, and setting up custom registry objects.
     */
    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            if (!FACTORIES_REGISTERED.compareAndSet(false, true))
            {
                LOGGER.warn("Skipping duplicate request factory registration (already registered).");
                return;
            }

            LOGGER.info("Registering MCTradePost request factories...");

            StandardFactoryController.getInstance().registerNewFactory(new OutpostRequestResolverFactory());
            StandardFactoryController.getInstance().registerNewFactory(new TrainDeliveryResolverFactory());
            StandardFactoryController.getInstance().registerNewFactory(new MCTPSettingsFactory.SortSettingFactory());

            PlacementHandlers.add(new OutpostPlacementHandler());

            IMinecoloniesAPI.getInstance().getEventBus().subscribe(CitizenBuriedModEvent.class,
                BurialEventHandler::onCitizenBuried);
        });
    }


    /**
     * This method is called on both the client and server side after the mod has finished loading.
     * It is responsible for injecting the sound events from MCTradePost into MineColonies' CITIZEN_SOUND_EVENTS.
     * This is a temporary solution until sounds in MineColonies have the flexibility to look up sound events from other modpacks.
     */
    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        LOGGER.info("MCTradePost onLoadComplete");
        MCTradePostMod.LOGGER.info("Injecting sounds.");
        MCTPModSoundEvents.injectSounds();              // These need to be injected both on client (to play) and server (to register)

        MCTradePostMod.LOGGER.info("Injecting crafting rules.");
        MCTPCraftingSetup.injectCraftingRules();

        MCTradePostMod.LOGGER.info("Injecting interaction handlers.");
        MCTPInteractionInitializer.injectInteractionHandlers();

        MCTradePostMod.LOGGER.info("Injecting building modules.");
        ModBuildingsInitializer.injectBuildingModules();
        com.deathfrog.mctradepost.core.colony.buildings.modules.CitizenIncentiveModule.registerLifecycleHooks();
    }

    /**
     * Keep the improved armor legal for level-one MineColonies guards. Explicit registrations take
     * precedence over MineColonies' automatic defense-based classification.
     * <p>
     * {@link Compatibility#registerItemTier} constructs a plain {@code new ItemStack(item)} internally,
     * which as of 26.1.2 requires the item's component map to already be bound
     * ({@code Holder.components()}); that binding isn't complete yet during {@code FMLCommonSetupEvent}
     * or even {@code FMLLoadCompleteEvent} (both throw "Components not bound yet"). MineColonies' own
     * equivalent call ({@code ModEquipmentTypes.initRegisterEquipmentTiers()}, see
     * {@code FMLEventHandler.onDefaultDataComponentsBound}) is triggered from exactly this event, which
     * is NeoForge's authoritative signal that default data components have finished binding -- so this
     * mod hooks the same event rather than guessing at a "safe enough" point in mod-loading.
     */
    private void onDefaultDataComponentsBound(final DefaultDataComponentsBoundEvent event)
    {
        MCTradePostMod.LOGGER.info("Registering MCTradePost armor tiers.");
        Compatibility.registerItemTier(REINFORCED_LEATHER_HELMET.get(), EquipmentLevelConstants.ARMOR_LEVEL_GOLD);
        Compatibility.registerItemTier(REINFORCED_LEATHER_CHESTPLATE.get(), EquipmentLevelConstants.ARMOR_LEVEL_GOLD);
        Compatibility.registerItemTier(REINFORCED_LEATHER_LEGGINGS.get(), EquipmentLevelConstants.ARMOR_LEVEL_GOLD);
        Compatibility.registerItemTier(REINFORCED_LEATHER_BOOTS.get(), EquipmentLevelConstants.ARMOR_LEVEL_GOLD);
    }

    @EventBusSubscriber(modid = MCTradePostMod.MODID)
    public class NetworkHandler {

        /**
         * Handles the registration of network payload handlers for the mod.
         * This method is invoked when the RegisterPayloadHandlersEvent is fired,
         * allowing the mod to set up its network communication protocols.
         *
         * @param event The event that provides the registrar for registering payload handlers.
         */
        @SuppressWarnings("null")
        @SubscribeEvent
        public static void onNetworkRegistry(final RegisterPayloadHandlersEvent event) {
            // Sets the current network version
            final PayloadRegistrar registrar = event.registrar("1");

            // Register the payload handler for the ConfigurationPacket - used to update the client with configurations they need to be aware of.
            // playBidirectional(type, codec, serverHandler, clientHandler) now takes both handlers
            // directly; DirectionalPayloadHandler (a wrapper combining exactly this pair into one
            // IPayloadHandler for the old single-handler overload) was removed in 26.1.2.
            registrar.playBidirectional(
                ConfigurationPacket.TYPE,
                ConfigurationPacket.STREAM_CODEC,
                ConfigurationPacket::handleDataInServerOnMain,
                ConfigurationPacket::handleDataInClientOnMain
            );

            // Register the payload handler for the ConfigurationPacket - used to update the client with ritual recipes they need to be aware of.
            registrar.playToClient(
                RitualPacket.TYPE,
                RitualPacket.RITUAL_CODEC,
                (payload, ctx) -> payload.handleDataInClientOnMain(ctx)
            );

            registrar.playToClient(
                ItemValueSyncPacket.TYPE,
                ItemValueSyncPacket.STREAM_CODEC,
                (payload, ctx) -> payload.handleDataInClientOnMain(ctx)
            );

            registrar.playToClient(
                RareFindBlacklistSyncPacket.TYPE,
                RareFindBlacklistSyncPacket.STREAM_CODEC,
                (payload, ctx) -> payload.handleDataInClientOnMain(ctx)
            );

            registrar.playToClient(
                PetForagingJeiSyncPacket.TYPE,
                PetForagingJeiSyncPacket.STREAM_CODEC,
                (payload, ctx) -> payload.handleDataInClientOnMain(ctx)
            );

            registrar.playToClient(
                com.deathfrog.mctradepost.network.TradePathDebugPacket.TYPE,
                com.deathfrog.mctradepost.network.TradePathDebugPacket.STREAM_CODEC,
                (payload, ctx) -> payload.handleDataInClientOnMain(ctx)
            );

            TradeMessage.TYPE.register(registrar);
            WithdrawMessage.TYPE.register(registrar);
            com.deathfrog.mctradepost.core.colony.buildings.modules.CitizenIncentiveMessage.TYPE.register(registrar);
            PetMessage.TYPE.register(registrar);
            CancelRecyclingMessage.TYPE.register(registrar);
            ResortGuestMessage.TYPE.register(registrar);
            StationLinkageMessage.TYPE.register(registrar);
            StationConnectionAttemptMessage.TYPE.register(registrar);
            StationRouteRefreshMessage.TYPE.register(registrar);
            OutpostAssignMessage.TYPE.register(registrar);
            StewIngredientMessage.TYPE.register(registrar);
            ThriftShopMessage.TYPE.register(registrar);
            MarketplaceSourcingMessage.TYPE.register(registrar);

        }

        @EventBusSubscriber(modid = MCTradePostMod.MODID)
        public class ServerLoginHandler {

            @SubscribeEvent
            public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
                if (event.getEntity() instanceof ServerPlayer player) {
                    MCTradePostMod.LOGGER.debug("Synchronizing Trade Post information to new player: {} ", player);
                    ConfigurationPacket.sendPacketsToPlayer(player);
                    RitualPacket.sendPacketsToPlayer(player);
                    ItemValueSyncPacket.sendPacketsToPlayer(player);
                    RareFindBlacklistSyncPacket.sendToPlayer(player);
                    PetForagingJeiSyncPacket.sendPacketsToPlayer(player);
                }
            }

            @SubscribeEvent
            public static void onDatapackSync(OnDatapackSyncEvent event)
            {
                FocusedForagingIndex.rebuild(event.getPlayerList().getServer());
                ServerPlayer localPlayer = event.getPlayer();

                if (localPlayer != null)
                {
                    RitualPacket.sendPacketsToPlayer(localPlayer);
                    ItemValueSyncPacket.sendPacketsToPlayer(localPlayer);
                    RareFindBlacklistSyncPacket.sendToPlayer(localPlayer);
                    PetForagingJeiSyncPacket.sendPacketsToPlayer(localPlayer);
                    return;
                }

                for (ServerPlayer player : event.getPlayerList().getPlayers())
                {
                    RitualPacket.sendPacketsToPlayer(player);
                }
                ItemValueSyncPacket.sendPacketsToAllPlayers(event.getPlayerList().getServer());
                RareFindBlacklistSyncPacket.sendToAllPlayers(event.getPlayerList().getServer());
                PetForagingJeiSyncPacket.sendPacketsToAllPlayers(event.getPlayerList().getServer());
            }
        }
    }

    /**
     * Handles the event when an entity joins the world. This method checks if the
     * entity is an instance of ITradePostPet and, if so, registers the pet in the
     * global pet registry. This ensures that all trade post pets are tracked for
     * functionality related to the MCTradePost mod.
     *
     * @param event The event that occurs when an entity joins the world. Contains
     *              information about the entity and the level it joins.
     */
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // MCTradePostMod.LOGGER.info("Entity joining world: {}", event.getEntity());

        if (!(event.getLevel() instanceof ServerLevel)) return;

        if (event.getEntity() instanceof ITradePostPet pet
            && (!(event.getEntity() instanceof PetDragon dragon) || !dragon.isCompanion())) {
            // MCTradePostMod.LOGGER.info("Registering pet: {}", pet);
            try {
                PetRegistryUtil.register(pet);
            } catch (IllegalArgumentException e) {
                MCTradePostMod.LOGGER.error("Error registering pet: {}. Discarding.", pet, e);
                event.getEntity().discard();
            }
        }
    }

    /**
     * Cleans up persistent companion state when a dropped Companion Staff expires naturally.
     *
     * @param event item-expiration event
     */
    @SuppressWarnings("null")
    public static void onItemExpire(ItemExpireEvent event)
    {
        if (event.getEntity().getItem().getItem() instanceof CompanionStaffItem)
        {
            java.util.UUID binding = com.deathfrog.mctradepost.core.entity.pets.companion.CompanionManager
                .bindingId(event.getEntity().getItem());

            if (binding != null)
            {
                com.deathfrog.mctradepost.core.entity.pets.companion.CompanionManager.destroy(event.getEntity(), binding);
            }
        }
    }

    /**
     * Events that can safely take place at world loading.
     *
     * @param event The level load event.
     */
    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {

    }

    @SubscribeEvent
    public void onAddReloadListener(AddServerReloadListenersEvent e)
    {
        e.addListener(Identifier.fromNamespaceAndPath(MODID, "item_values"), new ItemValueManager());
    }

    @EventBusSubscriber(modid = MODID)
    public class ModServerEventHandler
    {
        /**
         * Handles the entity attribute creation event for custom entities.
         * This method is responsible for assigning attribute modifiers to entities
         * when they are created. Specifically, it assigns attributes to the PET_WOLF
         * entity type by building the default attribute map for pet entities.
         *
         * @param event The event that triggers the creation of entity attributes.
         */
        @SuppressWarnings("null")
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event)
        {
            event.put(MCTradePostMod.PET_WOLF.get(), Wolf.createAttributes().build());
            event.put(MCTradePostMod.PET_FOX.get(), Fox.createAttributes().build());
            event.put(MCTradePostMod.PET_AXOLOTL.get(), Axolotl.createAttributes().build());
            event.put(MCTradePostMod.PET_PARROT.get(), Parrot.createAttributes().add(Attributes.ATTACK_DAMAGE, 1.0D).build());
            event.put(MCTradePostMod.PET_CAT.get(), Cat.createAttributes().build());
            event.put(MCTradePostMod.PET_PANDA.get(), Panda.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.25D).build());
            event.put(MCTradePostMod.PET_DRAGON.get(), PetDragon.createAttributes().build());
        }

        /**
         * Registers entities for the mod.
         * This method is invoked when the RegisterEvent is fired for the ENTITY_TYPE registry key.
         * @param event The register event containing the registry to register the entities with.
         */
        @SubscribeEvent
        public static void registerEntities(RegisterEvent event)
        {
            if (event.getRegistryKey().equals(Registries.ENTITY_TYPE))
            {
                MCTradePostMod.LOGGER.info("Placeholder: Registering entities");
            }
        }

        @SuppressWarnings("null")
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void registerCaps(final RegisterCapabilitiesEvent event)
        {
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, MCTradePostTileEntities.MOORING_BAY.get(), (be, side) -> be.fluidHandler());
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, MCTradePostTileEntities.GASIFIER.get(), (be, side) -> be.fluidHandler());
            event.registerBlockEntity(Capabilities.Item.BLOCK, MCTradePostTileEntities.GASIFIER.get(), (be, side) -> VanillaContainerWrapper.of(be));
            event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                MCTradePostTileEntities.PET_WORK_LOCATION.get(),
                (petWorkingBlockEntity, side) -> LegacyItemHandlerResourceAdapter.of(new InvWrapper(petWorkingBlockEntity)));

            registerBuildingItemHandlerCap(event, MCTradePostTileEntities.BUILDING.get());
            registerBuildingItemHandlerCap(event, MCTradePostTileEntities.MARKETPLACE.get());
            registerBuildingItemHandlerCap(event, MCTradePostTileEntities.RESORT.get());
            registerBuildingItemHandlerCap(event, MCTradePostTileEntities.RECYCLING.get());
            registerBuildingItemHandlerCap(event, MCTradePostTileEntities.STATION.get());
            registerBuildingItemHandlerCap(event, MCTradePostTileEntities.PET_SHOP.get());
            registerBuildingItemHandlerCap(event, MCTradePostTileEntities.OUTPOST.get());
        }

        @SuppressWarnings("null")
        private static void registerBuildingItemHandlerCap(
            final RegisterCapabilitiesEvent event,
            final net.minecraft.world.level.block.entity.BlockEntityType<? extends IItemHandlerCapProvider> blockEntityType)
        {
            event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                blockEntityType,
                IItemHandlerCapProvider::getItemResourceHandlerCap);
        }
    }

    @EventBusSubscriber(modid = MODID)
    public class GameplayServerEventHandler
    {
        /**
         * Handles the BlockEvent.EntityPlaceEvent.
         * This event is fired by the EntityPlaceEvent class when a block is placed by an entity.
         * Specifically, it is fired when a player places a block.
         * This method is responsible for registering the placed block as a work location for pets in the colony.
         * @param event The event that is fired when an entity places a block.
         */
        @SubscribeEvent
        public static void onBlockPlaced(final BlockEvent.EntityPlaceEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            if (!(event.getLevel() instanceof ServerLevel level)) return;

            BlockState state = event.getPlacedBlock();
            if (!(state.getBlock() instanceof AbstractBlockPetWorkingLocation)) return;

            BlockPos pos = event.getPos();
            IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(level, pos);
            if (colony != null) {
                PetRegistryUtil.registerWorkLocation(colony, pos);
                MCTradePostMod.LOGGER.info("Registered Work Location block at {} for colony {}", pos, colony.getID());
            }
        }

        /**
         * Called when a block is broken in the world.
         * If the broken block is a Work Location block, this will unregister the BlockPos
         * as a valid work location for a pet in the colony at that BlockPos.
         * @param event The event containing the broken block and its BlockPos.
         */
        /**
         * Handles server-side block break cleanup for pet work locations and cached station route invalidation.
         *
         * @param event block break event
         */
        @SuppressWarnings("null")
        @SubscribeEvent
        public static void onBlockBroken(final BreakBlockEvent event) {
            if (!(event.getLevel() instanceof ServerLevel level)) return;

            BlockState state = event.getState();
            BlockPos pos = event.getPos();
            if (state.getBlock() instanceof AbstractBlockPetWorkingLocation)
            {
                IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(level, pos);
                if (colony != null) {
                    PetRegistryUtil.unregisterWorkLocation(colony, pos);
                    MCTradePostMod.LOGGER.info("Unregistered Work Location block at {} for colony {}", pos, colony.getID());
                }
            }

            if (state.getBlock() instanceof BaseRailBlock ||
                state.is(NullnessBridge.assumeNonnull(ModTags.BLOCKS.TRACK_TAG)) ||
                state.is(NullnessBridge.assumeNonnull(ModTags.BLOCKS.TRADE_ROADS_TAG)) ||
                state.is(TRADE_DOCK.get()) || state.is(TRADE_INTERCHANGE.get()) || state.is(MOORING_BAY.get()))
            {
                invalidateTrackCachesForBrokenTrack(level, pos);
            }
        }

        /**
         * Marks cached station/outpost routes disconnected when they contain a broken track.
         * <p>
         * This deliberately avoids path recomputation during the block event; normal station validation will discover replacement
         * routes later if an alternate path exists.
         *
         * @param level level where the track was broken
         * @param pos broken track position
         */
        private static void invalidateTrackCachesForBrokenTrack(final ServerLevel level, final BlockPos pos)
        {
            int invalidatedRoutes = 0;
            int affectedLinkages = 0;

            for (IColony colony : IColonyManager.getInstance().getAllColonies())
            {
                for (IBuilding building : colony.getServerBuildingManager().getBuildings().values())
                {
                    if (building instanceof BuildingStation station)
                    {
                        int invalidatedStationRoutes = invalidateCachedRoutes(station.getConnectionResults(), level, pos);
                        int affectedStationLinkages = dirtyBrokenEndpointLinkages(station, level, pos);
                        if (invalidatedStationRoutes > 0 || affectedStationLinkages > 0)
                        {
                            station.markTradesDirty();
                        }
                        invalidatedRoutes += invalidatedStationRoutes;
                        affectedLinkages += affectedStationLinkages;
                    }
                    else if (building instanceof BuildingOutpost outpost)
                    {
                        int invalidatedOutpostRoutes = invalidateCachedRoutes(outpost.getConnectionResults(), level, pos);
                        if (invalidatedOutpostRoutes > 0)
                        {
                            invalidatedRoutes += invalidatedOutpostRoutes;
                            outpost.markTradesDirty();
                        }
                    }
                }
            }

            if (invalidatedRoutes > 0 || affectedLinkages > 0)
            {
                MCTradePostMod.LOGGER.info("Broken track at {} in {} invalidated {} cached route(s) and affected {} dimensional linkage(s).",
                    pos.toShortString(), level.dimension().identifier(), invalidatedRoutes, affectedLinkages);
            }
        }

        /**
         * Marks every cached connection result containing the supplied position as disconnected.
         *
         * @param results cached route results to inspect
         * @param level level where the track was broken
         * @param pos broken track position
         * @return number of route results invalidated
         */
        private static int invalidateCachedRoutes(final Map<StationData, TrackConnectionResult> results, final ServerLevel level, final BlockPos pos)
        {
            int invalidated = 0;
            for (TrackConnectionResult result : results.values())
            {
                if (result == null || !result.connected || !routeContains(result, level, pos))
                {
                    continue;
                }

                result.setConnected(false);
                result.lastChecked = level.getGameTime();
                invalidated++;
            }
            return invalidated;
        }

        /**
         * Tests whether a cached connection result contains a position in the supplied dimension.
         *
         * @param result cached route result
         * @param level level to match against route segment dimensions
         * @param pos position to search for
         * @return true when the route includes the position in the same dimension
         */
        private static boolean routeContains(final TrackConnectionResult result, final ServerLevel level, final BlockPos pos)
        {
            TrackRoute route = result.getRoute();
            if (route != null)
            {
                for (TrackRoute.Segment segment : route.segments())
                {
                    if (segment.dimension().equals(level.dimension()) && segment.path() != null && segment.path().contains(pos))
                    {
                        return true;
                    }
                }
                return false;
            }

            return result.path != null && result.path.contains(pos);
        }

        /**
         * Marks a station linkage module dirty when a broken track is one of its recorded endpoints.
         *
         * @param station station whose installed linkages should be checked
         * @param level level where the track was broken
         * @param pos broken track position
         * @return number of installed linkage endpoints affected
         */
        private static int dirtyBrokenEndpointLinkages(final BuildingStation station, final ServerLevel level, final BlockPos pos)
        {
            BuildingStationConnectionModule module = station.getModule(MCTPBuildingModules.STATION_CONNECTION);
            if (module == null)
            {
                return 0;
            }

            int affected = 0;
            for (ItemStack stack : module.getDimensionalLinkages())
            {
                DimensionalLinkageRecord record = DimensionalLinkageItem.linkageRecord(stack);
                if (isEndpoint(record.overworldEndpoint().orElse(null), level, pos) ||
                    isEndpoint(record.netherEndpoint().orElse(null), level, pos))
                {
                    affected++;
                }
            }

            if (affected > 0)
            {
                module.markDirty();
            }
            return affected;
        }

        /**
         * Tests whether a dimensional endpoint matches a level and block position.
         *
         * @param endpoint endpoint to test
         * @param level level to match
         * @param pos position to match
         * @return true when endpoint dimension and position match
         */
        private static boolean isEndpoint(final DimPos endpoint, final ServerLevel level, final BlockPos pos)
        {
            return endpoint != null && endpoint.dimension().equals(level.dimension()) && endpoint.pos().equals(pos);
        }

        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            MCTradePostMod.LOGGER.info("Server starting.");
        }

        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event)
        {
            MCTradePostMod.LOGGER.info("Server started.");
        }

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents
    {

        // Add items to their creative tabs.
        @SuppressWarnings("null")
        @SubscribeEvent
        private static void addCreative(BuildCreativeModeTabContentsEvent event)
        {

            if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
                // No-op placeholde for future use.
            }

            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                // No-op placeholder for future use.
            }

            TRADEPOST_TAB.unwrapKey().ifPresent(key -> {
                if (event.getTabKey().equals(key)) {
                    event.accept(MCTradePostMod.blockHutMarketplace.get());
                    event.accept(MCTradePostMod.blockHutResort.get());
                    event.accept(MCTradePostMod.blockHutRecycling.get());
                    event.accept(MCTradePostMod.blockHutStation.get());
                    event.accept(MCTradePostMod.blockHutPetShop.get());
                    event.accept(MCTradePostMod.blockHutOutpost.get());
                    event.accept(MCTradePostMod.TRADE_DOCK.get());
                    event.accept(MCTradePostMod.TRADE_INTERCHANGE.get());
                    event.accept(MCTradePostMod.MOORING_BAY.get());
                    event.accept(MCTradePostMod.ADVANCED_CLIPBOARD.get());
                    event.accept(MCTradePostMod.ICECREAM.get());
                    event.accept(MCTradePostMod.DAIQUIRI.get());
                    event.accept(MCTradePostMod.IMMERSION_BLENDER.get());
                    event.accept(MCTradePostMod.CURRENCY_EXCHANGE.get());
                    event.accept(MCTradePostMod.DIMENSIONAL_LINKAGE.get());
                    event.accept(MCTradePostMod.VEGGIE_JUICE.get());
                    event.accept(MCTradePostMod.FRUIT_JUICE.get());
                    event.accept(MCTradePostMod.PROTIEN_SHAKE.get());
                    event.accept(MCTradePostMod.SMETANA.get());
                    event.accept(MCTradePostMod.DUMPLINGS_AND_CREAM.get());
                    event.accept(MCTradePostMod.ENERGY_SHAKE.get());
                    event.accept(MCTradePostMod.VANILLA_MILKSHAKE.get());
                    event.accept(MCTradePostMod.BAR_NUTS.get());
                    event.accept(MCTradePostMod.PERPETUAL_STEW.get());
                    event.accept(MCTradePostMod.HEARTY_PERPETUAL_STEW.get());
                    event.accept(MCTradePostMod.GOURMET_PERPETUAL_STEW.get());
                    event.accept(MCTradePostMod.CHICKPEA_STROGANOFF.get());
                    event.accept(MCTradePostMod.STIR_FRY.get());
                    event.accept(MCTradePostMod.COLD_BREW.get());
                    event.accept(MCTradePostMod.MYSTIC_TEA.get());
                    event.accept(MCTradePostMod.NAPKIN.get());
                    event.accept(MCTradePostMod.REINFORCED_LEATHER.get());
                    event.accept(MCTradePostMod.REINFORCED_LEATHER_HELMET.get());
                    event.accept(MCTradePostMod.REINFORCED_LEATHER_CHESTPLATE.get());
                    event.accept(MCTradePostMod.REINFORCED_LEATHER_LEGGINGS.get());
                    event.accept(MCTradePostMod.REINFORCED_LEATHER_BOOTS.get());
                    event.accept(MCTradePostMod.END_MORTAR.get());
                    event.accept(MCTradePostMod.PRISMARINE_MORTAR.get());
                    event.accept(MCTradePostMod.QUARTZ_MORTAR.get());
                    event.accept(MCTradePostMod.STEW_SEASONING.get());
                    event.accept(MCTradePostMod.MIXED_STONE.get());
                    event.accept(MCTradePostMod.MIXED_STONE_STAIRS.get());
                    event.accept(MCTradePostMod.MIXED_STONE_WALL.get());
                    event.accept(MCTradePostMod.MIXED_STONE_SLAB.get());
                    event.accept(MCTradePostMod.MIXED_STONE_BRICK.get());
                    event.accept(MCTradePostMod.MIXED_STONE_BRICK_STAIRS.get());
                    event.accept(MCTradePostMod.MIXED_STONE_BRICK_WALL.get());
                    event.accept(MCTradePostMod.MIXED_STONE_BRICK_SLAB.get());
                    event.accept(MCTradePostMod.THATCH.get());
                    event.accept(MCTradePostMod.THATCH_STAIRS.get());
                    event.accept(MCTradePostMod.THATCH_WALL.get());
                    event.accept(MCTradePostMod.THATCH_SLAB.get());
                    event.accept(MCTradePostMod.PLASTER.get());
                    event.accept(MCTradePostMod.PLASTER_STAIRS.get());
                    event.accept(MCTradePostMod.PLASTER_WALL.get());
                    event.accept(MCTradePostMod.PLASTER_SLAB.get());
                    event.accept(MCTradePostMod.QUARTZ_PLASTER.get());
                    event.accept(MCTradePostMod.QUARTZ_PLASTER_STAIRS.get());
                    event.accept(MCTradePostMod.QUARTZ_PLASTER_WALL.get());
                    event.accept(MCTradePostMod.QUARTZ_PLASTER_SLAB.get());
                    event.accept(MCTradePostMod.ROUGH_STONE.get());
                    event.accept(MCTradePostMod.ROUGH_STONE_STAIRS.get());
                    event.accept(MCTradePostMod.ROUGH_STONE_WALL.get());
                    event.accept(MCTradePostMod.ROUGH_STONE_SLAB.get());
                    event.accept(MCTradePostMod.ROUGH_BRICK.get());
                    event.accept(MCTradePostMod.ROUGH_BRICK_STAIRS.get());
                    event.accept(MCTradePostMod.ROUGH_BRICK_WALL.get());
                    event.accept(MCTradePostMod.ROUGH_BRICK_SLAB.get());
                    event.accept(MCTradePostMod.DIAGONAL_BRICK.get());
                    event.accept(MCTradePostMod.DIAGONAL_BRICK_STAIRS.get());
                    event.accept(MCTradePostMod.DIAGONAL_BRICK_WALL.get());
                    event.accept(MCTradePostMod.DIAGONAL_BRICK_SLAB.get());
                    event.accept(MCTradePostMod.ENDETHYST.get());
                    event.accept(MCTradePostMod.ENDETHYST_STAIRS.get());
                    event.accept(MCTradePostMod.ENDETHYST_WALL.get());
                    event.accept(MCTradePostMod.ENDETHYST_SLAB.get());
                    event.accept(MCTradePostMod.ENDETHYST_BRICK.get());
                    event.accept(MCTradePostMod.ENDETHYST_BRICK_STAIRS.get());
                    event.accept(MCTradePostMod.ENDETHYST_BRICK_WALL.get());
                    event.accept(MCTradePostMod.ENDETHYST_BRICK_SLAB.get());
                    event.accept(MCTradePostMod.ENDMARINE.get());
                    event.accept(MCTradePostMod.ENDMARINE_STAIRS.get());
                    event.accept(MCTradePostMod.ENDMARINE_WALL.get());
                    event.accept(MCTradePostMod.ENDMARINE_SLAB.get());
                    event.accept(MCTradePostMod.ENDMARINE_BRICK.get());
                    event.accept(MCTradePostMod.ENDMARINE_BRICK_STAIRS.get());
                    event.accept(MCTradePostMod.ENDMARINE_BRICK_WALL.get());
                    event.accept(MCTradePostMod.ENDMARINE_BRICK_SLAB.get());
                    event.accept(MCTradePostMod.MARINE_LAPIS.get());
                    event.accept(MCTradePostMod.MARINE_LAPIS_STAIRS.get());
                    event.accept(MCTradePostMod.MARINE_LAPIS_WALL.get());
                    event.accept(MCTradePostMod.MARINE_LAPIS_SLAB.get());
                    event.accept(MCTradePostMod.MARINE_LAPIS_BRICK.get());
                    event.accept(MCTradePostMod.MARINE_LAPIS_BRICK_STAIRS.get());
                    event.accept(MCTradePostMod.MARINE_LAPIS_BRICK_WALL.get());
                    event.accept(MCTradePostMod.MARINE_LAPIS_BRICK_SLAB.get());
                    event.accept(MCTradePostMod.BLOCK_IVY_BRICK.get());
                    event.accept(MCTradePostMod.WEATHERED_ROUGH_STONE.get());
                    event.accept(MCTradePostMod.WEATHERED_ROUGH_STONE_STAIRS.get());
                    event.accept(MCTradePostMod.WEATHERED_ROUGH_STONE_WALL.get());
                    event.accept(MCTradePostMod.WEATHERED_ROUGH_STONE_SLAB.get());
                    event.accept(MCTradePostMod.MARINE_BASALT.get());
                    event.accept(MCTradePostMod.MARINE_BASALT_STAIRS.get());
                    event.accept(MCTradePostMod.MARINE_BASALT_WALL.get());
                    event.accept(MCTradePostMod.MARINE_BASALT_SLAB.get());
                    event.accept(MCTradePostMod.TROUGH.get());
                    event.accept(MCTradePostMod.SCAVENGE.get());
                    event.accept(MCTradePostMod.FEEDER.get());
                    event.accept(MCTradePostMod.DREDGER.get());
                    event.accept(MCTradePostMod.HAULER.get());
                    event.accept(MCTradePostMod.WOVEN_KELP.get());
                    event.accept(MCTradePostMod.WOVEN_KELP_STAIRS.get());
                    event.accept(MCTradePostMod.WOVEN_KELP_WALL.get());
                    event.accept(MCTradePostMod.WOVEN_KELP_SLAB.get());
                    event.accept(MCTradePostMod.CHITIN.get());
                    event.accept(MCTradePostMod.CHITIN_STAIRS.get());
                    event.accept(MCTradePostMod.CHITIN_WALL.get());
                    event.accept(MCTradePostMod.CHITIN_SLAB.get());
                    event.accept(MCTradePostMod.WISH_PLENTY.get());
                    event.accept(MCTradePostMod.WISH_HEALTH.get());
                    event.accept(MCTradePostMod.WISH_KNOWLEDGE.get());
                    event.accept(MCTradePostMod.WISH_GATHERING_COW.get());
                    event.accept(MCTradePostMod.WISH_GATHERING_PIG.get());
                    event.accept(MCTradePostMod.WISH_GATHERING_CHICKEN.get());
                    event.accept(MCTradePostMod.WISH_GATHERING_SHEEP.get());
                    event.accept(MCTradePostMod.WISH_SHELTER.get());
                    event.accept(MCTradePostMod.WISH_RESURRECTION.get());
                    event.accept(MCTradePostMod.OUTPOST_CLAIM.get());
                    event.accept(MCTradePostMod.STEWPOT_FILLED.get());
                    event.accept(MCTradePostMod.BLOCK_LAMP.get());
                    event.accept(MCTradePostMod.COPPER_NUGGET.get());
                }
            });

        }
        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            // Register the layer definitions
            MCTradePostMod.LOGGER.info("Registering model definitions.");
            ModelRegistryHandler.registerModels(event);
        }

        @SuppressWarnings("null")
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(MCTradePostMod.COIN_ENTITY_TYPE.get(), CoinRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.GHOST_CART.get(), GhostCartRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.GHOST_BOAT.get(), GhostBoatRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.WAGON.get(), WagonRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.AIRSHIP.get(), AirshipRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.PET_WOLF.get(), WolfRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.PET_FOX.get(), FoxRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.PET_AXOLOTL.get(), AxolotlRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.PET_PARROT.get(), ParrotRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.PET_CAT.get(), CatRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.PET_PANDA.get(), PandaRenderer::new);
            event.registerEntityRenderer(MCTradePostMod.PET_DRAGON.get(), PetDragonRenderer::new);
        }

        /**
         * Handles the addition of custom layers to entity renderers.
         * This method is called on the client side when the AddLayers event is fired.
         * It initializes a context with various rendering components and
         * invokes the model type initializer to set up custom entity models.
         *
         * @param event The event providing access to entity models for registering custom layers.
         */
        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event)
        {
            LOGGER.info("Handling model initialization");

            // EntityRendererProvider.Context grew several new required constructor arguments
            // (BlockModelResolver, ItemModelResolver, MapRenderer, EquipmentAssetManager, AtlasManager,
            // PlayerSkinRenderCache) that aren't individually reachable off Minecraft.getInstance() the
            // way the old ItemRenderer/BlockRenderDispatcher pair was. AddLayers now exposes the real,
            // fully-constructed Context directly, so there's no need to hand-assemble one any more.
            Context context = event.getContext();

            ModModelTypeInitializer.init(context);
        }

        /**
         * Registers item decorations for the mod.
         * This method is invoked when the RegisterEvent is fired for the ITEM_DECORATIONS registry key.
         * @param event The register event containing the registry to register the item decorations with.
         */
        @SuppressWarnings("null")
        @SubscribeEvent
        public static void onRegisterItemDecorations(final RegisterItemDecorationsEvent event)
        {
            LOGGER.info("Registering item decorations");
            event.register(MCTradePostMod.ADVANCED_CLIPBOARD, new AdvancedClipBoardDecorator());
        }

        /**
         * Registers blocks for the mod.
         * This method is invoked when the RegisterEvent is fired for the BLOCK registry key.
         * @param event The register event containing the registry to register the blocks with.
         */
        @SubscribeEvent
        public static void registerBlocks(RegisterEvent event)
        {
            if (event.getRegistryKey().equals(Registries.BLOCK))
            {
                LOGGER.info("Placeholder: Registering blocks");
            }
        }

        /**
         * Registers the special model renderer used to draw the souvenir item's wrapped item inside a
         * glass "snow globe". Replaces the pre-26.1 geometry-loader + BlockEntityWithoutLevelRenderer
         * (ISTER) pipeline, which no longer exists.
         * @param event The register special model renderer event.
         */
        @SubscribeEvent
        public static void registerSpecialModelRenderers(final RegisterSpecialModelRendererEvent event)
        {
            LOGGER.info("Registering souvenir special model renderer");
            event.register(Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "souvenir"), SouvenirSpecialRenderer.Unbaked.MAP_CODEC);
        }

        /**
         * Lifting gas uses the gas_bubbles texture for both still and flowing states.
         * 26.1.2 port: fluid textures are registered as fluid models (replaces IClientFluidTypeExtensions#getStillTexture).
         * @param event The register fluid models event.
         */
        @SubscribeEvent
        public static void registerFluidModels(final RegisterFluidModelsEvent event)
        {
            final Material gasBubbles = new Material(Identifier.fromNamespaceAndPath(MODID, "block/gas_bubbles"));
            event.register(new FluidModel.Unbaked(gasBubbles, gasBubbles, null, null), MCTPFluids.LIFTING_GAS, MCTPFluids.FLOWING_LIFTING_GAS);
        }

        // Register client-side listeners on the Mod bus
        @SubscribeEvent
        public static void registerClientReloadListeners(AddClientReloadListenersEvent event) {
            event.addListener(Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "rituals_client"), new RitualManager());
        }

        // The outpost-claim "linked/unlinked" model switch used to be registered here via
        // ItemProperties.register (removed in 26.1.2). It's now fully data-driven: see
        // assets/mctradepost/items/outpost_claim.json, which uses a minecraft:condition item
        // model keyed on minecraft:has_component against mctradepost:linked_block_pos - the exact
        // same predicate OutpostClaimMarkerItem.hasLinkedBlockPos(stack) tested here before.

        /**
         * Registers the custom pet-working inventory screen on the client.
         *
         * @param event menu-screen registration event
         */
        @SuppressWarnings("null")
        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event)
        {
            event.register(PET_WORKING_MENU.get(), com.deathfrog.mctradepost.core.client.gui.PetWorkingScreen::new);
            event.register(MOORING_BAY_MENU.get(), com.deathfrog.mctradepost.core.client.gui.MooringBayScreen::new);
            event.register(GASIFIER_MENU.get(), com.deathfrog.mctradepost.core.client.gui.GasifierScreen::new);
            event.register(TRADE_DOCK_MENU.get(), com.deathfrog.mctradepost.core.client.gui.TradeDockScreen::new);
        }
    }

}
