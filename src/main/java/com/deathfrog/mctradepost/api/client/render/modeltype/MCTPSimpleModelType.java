package com.deathfrog.mctradepost.api.client.render.modeltype;

import org.jetbrains.annotations.NotNull;
import com.deathfrog.mctradepost.MCTradePostMod;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.ISimpleModelType;
import com.minecolonies.api.client.render.modeltype.SimpleModelType;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_TEXTURE_SUFFIX;
import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_STYLE;

public class MCTPSimpleModelType extends SimpleModelType
{
    public MCTPSimpleModelType(Identifier name,
        int numTextures,
        CitizenModel maleModel,
        CitizenModel femaleModel)
    {
        super(name, numTextures, maleModel, femaleModel);
    }

    @Override
    /**
     * Method used to get the path to the texture every time it is updated on the entity. By default this uses the textureBase + sex
     * marker + randomly assigned texture index + metadata as a format.
     *
     * @param entityCitizen The citizen in question to get the path.
     * @return The path to the citizen.
     */
    public Identifier getTexture(@NotNull final AbstractEntityCitizen entityCitizen)
    {
        // return super.getTexture(entityCitizen);

        @SuppressWarnings("null")
        String style = entityCitizen.getEntityData().get(DATA_STYLE);

        final int moddedTextureId = (entityCitizen.getTextureId() % getNumTextures()) + 1;

        @SuppressWarnings("null")
        final String textureIdentifier = getName().getPath() + (entityCitizen.isFemale() ? "female" : "male") +
            moddedTextureId +
            entityCitizen.getEntityData().get(DATA_TEXTURE_SUFFIX);
        final Identifier modified = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID,
            ISimpleModelType.BASE_FOLDER + style + "/" + textureIdentifier + ".png");

        if (modified != null && Minecraft.getInstance().getResourceManager().getResource(modified).isPresent())
        {
            return modified;
        }

        return Identifier.fromNamespaceAndPath(MCTradePostMod.MODID,
            ISimpleModelType.BASE_FOLDER + ISimpleModelType.DEFAULT_FOLDER + "/" + textureIdentifier + ".png");
    }
}
