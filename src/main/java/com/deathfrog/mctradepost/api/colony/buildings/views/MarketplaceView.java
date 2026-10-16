package com.deathfrog.mctradepost.api.colony.buildings.views;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;

/*
 * ✅ Best Practice If a field should: Be visible on the client UI → put it in serializeNBT() in the building and deserializeNBT() both
 * in the building and deserialize here in the view.
 */
public class MarketplaceView extends AbstractBuildingView
{
    private boolean displayShelfContents = true;
    private final List<ItemStack> shelfItems = new ArrayList<>();

    public MarketplaceView(final IColonyView colony, final BlockPos location)
    {
        super(colony, location);
    }

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.deserialize(buf);

        CompoundTag compound = buf.readNbt();
        if (compound != null)
        {
            displayShelfContents = compound.getBooleanOr("displayShelfContents", false);

            shelfItems.clear();
            for (int i = 0; compound.contains("ritualItem" + i); i++)
            {
                String id = compound.getStringOr("ritualItem" + i, "");
                ItemStack stack = id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.tryParse(id)));
                shelfItems.add(stack);
            }
        }
    }

    public boolean shouldDisplayShelfContents()
    {
        return displayShelfContents;
    }

    public List<ItemStack> getShelfItems()
    {
        return shelfItems;
    }
}
