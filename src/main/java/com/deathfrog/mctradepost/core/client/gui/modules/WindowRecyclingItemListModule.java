package com.deathfrog.mctradepost.core.client.gui.modules;

import com.minecolonies.api.colony.buildings.modules.IItemListModuleView;
import com.minecolonies.core.client.gui.modules.building.ItemListModuleWindow;

import net.minecraft.resources.Identifier;

public class WindowRecyclingItemListModule extends ItemListModuleWindow
{

    public WindowRecyclingItemListModule(IItemListModuleView moduleView, Identifier resource)
    {
        super(moduleView, resource);
    }

   protected void updateResourceList() {
        super.updateResourceList();
   }


    
}
