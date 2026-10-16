package com.deathfrog.mctradepost.apiimp.initializer;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.api.colony.buildings.ModBuildings;
import com.deathfrog.mctradepost.api.tileentities.MCTPTileEntityColonyBuilding;
import com.deathfrog.mctradepost.api.tileentities.MCTradePostTileEntities;
import com.deathfrog.mctradepost.core.blocks.AbstractBlockPetWorkingLocation;
import com.deathfrog.mctradepost.core.blocks.BlockTradeDock;
import com.deathfrog.mctradepost.core.blocks.blockentity.PetWorkingBlockEntity;
import com.deathfrog.mctradepost.core.blocks.blockentity.MooringBayBlockEntity;
import com.deathfrog.mctradepost.core.blocks.blockentity.GasifierBlockEntity;
import com.deathfrog.mctradepost.core.blocks.blockentity.TradeDockBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;


@SuppressWarnings("null")
public class TileEntityInitializer
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MCTradePostMod.MODID);

    static
    {
        MCTradePostTileEntities.MOORING_BAY = BLOCK_ENTITIES.register("mooring_bay",
            () -> new BlockEntityType<>(MooringBayBlockEntity::new, MCTradePostMod.MOORING_BAY.get()));
        MCTradePostTileEntities.GASIFIER = BLOCK_ENTITIES.register("gasifier",
            () -> new BlockEntityType<>(GasifierBlockEntity::new, MCTradePostMod.GASIFIER.get()));
        MCTradePostTileEntities.TRADE_DOCK = BLOCK_ENTITIES.register(BlockTradeDock.ID,
            () -> new BlockEntityType<>(TradeDockBlockEntity::new, MCTradePostMod.TRADE_DOCK.get()));
        MCTradePostTileEntities.PET_WORK_LOCATION = BLOCK_ENTITIES.register("pet_work_location", 
            () -> new BlockEntityType<>(PetWorkingBlockEntity::new, AbstractBlockPetWorkingLocation.getPetWorkBlocks()));

        MCTradePostTileEntities.BUILDING = BLOCK_ENTITIES.register("mctp_colonybuilding", 
            () -> new BlockEntityType<>(MCTPTileEntityColonyBuilding::new, ModBuildings.getHuts()));

        MCTradePostTileEntities.MARKETPLACE = BLOCK_ENTITIES.register(ModBuildings.MARKETPLACE_ID, 
            () -> new BlockEntityType<>(MCTPTileEntityColonyBuilding::new, ModBuildings.getHuts()));

        MCTradePostTileEntities.RESORT = BLOCK_ENTITIES.register(ModBuildings.RESORT_ID, 
            () -> new BlockEntityType<>(MCTPTileEntityColonyBuilding::new, ModBuildings.getHuts()));

        MCTradePostTileEntities.RECYCLING = BLOCK_ENTITIES.register(ModBuildings.RECYCLING_ID, 
            () -> new BlockEntityType<>(MCTPTileEntityColonyBuilding::new, ModBuildings.getHuts()));

        MCTradePostTileEntities.STATION = BLOCK_ENTITIES.register(ModBuildings.STATION_ID, 
            () -> new BlockEntityType<>(MCTPTileEntityColonyBuilding::new, ModBuildings.getHuts()));

        MCTradePostTileEntities.PET_SHOP = BLOCK_ENTITIES.register(ModBuildings.PETSHOP_ID, 
            () -> new BlockEntityType<>(MCTPTileEntityColonyBuilding::new, ModBuildings.getHuts()));

        MCTradePostTileEntities.OUTPOST = BLOCK_ENTITIES.register(ModBuildings.OUTPOST_ID, 
            () -> new BlockEntityType<>(MCTPTileEntityColonyBuilding::new, ModBuildings.getHuts()));
 
            
    }
}
