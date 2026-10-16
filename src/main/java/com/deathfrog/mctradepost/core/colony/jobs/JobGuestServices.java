package com.deathfrog.mctradepost.core.colony.jobs;

import com.deathfrog.mctradepost.core.entity.ai.workers.EntityAIWorkGuestServices;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJobCrafter;

import net.minecraft.resources.Identifier;

public class JobGuestServices extends AbstractJobCrafter<EntityAIWorkGuestServices, JobGuestServices> {

    public JobGuestServices(ICitizenData entity) {
        super(entity);
    }

    @Override
    public EntityAIWorkGuestServices generateAI() {
        return new EntityAIWorkGuestServices(this);
    }
    
    @Override
    public Identifier getModel()
    {
        // MCTradePostMod.LOGGER.info("Getting JobShopkeeper model {}", ModModelTypes.SHOPKEEPER_MODEL_ID);
        // MCTradePostMod.LOGGER.warn("Model load trace", new Exception("Model trace"));
        // return ModModelTypes.SHOPKEEPER_MODEL_ID;
        return super.getModel();
    }
}
