package com.deathfrog.mctradepost.core.client.render;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.api.entity.pets.PetDragon;
import com.deathfrog.mctradepost.core.client.model.PetDragonModel;
import com.deathfrog.mctradepost.core.event.ModelRegistryHandler;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renders the mythical companion dragon pet.
 * <p>
 * 26.1.2 split entity rendering into a main-thread {@code extractRenderState} step and a render-thread
 * {@code submit} step reading only the resulting immutable render state; there is no per-entity
 * {@code getTextureLocation(T entity)} hook any more -- {@link MobRenderer} requires
 * {@code getTextureLocation(S state)}, and the base {@link net.minecraft.client.renderer.entity.LivingEntityRenderer#submit}
 * already calls {@code submitNodeCollector.submitModel(...)} (which internally invokes
 * {@code model.setupAnim(state)}), so unlike the old upstream version this renderer does not need to override
 * {@code render}/{@code submit} itself -- only the render-state extraction and the upstream 0.72 scale-down.
 * </p>
 */
public class PetDragonRenderer extends MobRenderer<PetDragon, PetDragonRenderer.PetDragonRenderState, PetDragonModel>
{
    /** Render-thread-safe snapshot of a dragon's per-frame animation state. */
    public static class PetDragonRenderState extends LivingEntityRenderState
    {
        public boolean isOnGround;
    }

    @SuppressWarnings("null")
    private static final @Nonnull Identifier TEXTURE = Identifier.fromNamespaceAndPath(
        MCTradePostMod.MODID, "textures/entity/pet_dragon.png");

    @SuppressWarnings("null")
    public PetDragonRenderer(EntityRendererProvider.Context context)
    {
        super(context, new PetDragonModel(context.bakeLayer(ModelRegistryHandler.PET_DRAGON)), 0.5F);
    }

    @Override
    public PetDragonRenderState createRenderState()
    {
        return new PetDragonRenderState();
    }

    @SuppressWarnings("null")
    @Override
    public void extractRenderState(@Nonnull PetDragon entity, @Nonnull PetDragonRenderState state, float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        state.isOnGround = entity.onGround();
    }

    @Override
    public @Nonnull Identifier getTextureLocation(@Nonnull PetDragonRenderState state)
    {
        return TEXTURE;
    }

    @Override
    protected void scale(@Nonnull PetDragonRenderState state, @Nonnull PoseStack pose)
    {
        pose.scale(0.72F, 0.72F, 0.72F);
    }
}
