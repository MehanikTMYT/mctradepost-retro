package com.deathfrog.mctradepost.core.client.render;

import javax.annotation.Nonnull;

import org.joml.Quaternionf;

import com.deathfrog.mctradepost.api.entity.GhostCartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
public class GhostCartRenderer extends AbstractMinecartRenderer<GhostCartEntity, GhostCartRenderer.GhostCartRenderState>
{
    /**
     * Minecraft 26.1's entity renderers no longer draw directly from an entity reference; render()
     * was split into extractRenderState() (main thread, reads the entity) and submit() (render
     * thread, reads only this immutable snapshot). The trade item's resolved render state is
     * captured here so submit() never has to touch the entity itself.
     */
    public static class GhostCartRenderState extends MinecartRenderState
    {
        public final ItemStackRenderState tradeItem = new ItemStackRenderState();
        public boolean hasTradeItem = false;
    }

    private final ItemModelResolver itemModelResolver;

    public GhostCartRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx, ModelLayers.MINECART);
        this.itemModelResolver = ctx.getItemModelResolver();
    }

    @Override
    public GhostCartRenderState createRenderState()
    {
        return new GhostCartRenderState();
    }

    /**
     * Culling now happens on the renderer rather than the entity (Entity#getBoundingBoxForCulling()
     * was removed in 26.1.2). This reproduces the 0.25 inflate GhostCartEntity used to apply itself,
     * so the cart doesn't pop out of view early while riding its path.
     */
    @Override
    protected AABB getBoundingBoxForCulling(@Nonnull GhostCartEntity entity)
    {
        return super.getBoundingBoxForCulling(entity).inflate(0.25);
    }

    @Override
    public void extractRenderState(@Nonnull GhostCartEntity cart, @Nonnull GhostCartRenderState state, float partialTicks)
    {
        super.extractRenderState(cart, state, partialTicks);

        final ItemStack stack = cart.getTradeItem();
        state.hasTradeItem = !stack.isEmpty();
        if (state.hasTradeItem)
        {
            itemModelResolver.updateForNonLiving(state.tradeItem, stack, ItemDisplayContext.GROUND, cart);
        }
    }

    /**
     * Submits the cart model (via the vanilla superclass) plus, if present, the trade item floating
     * above it. The item is scaled up slightly and spun to stay visible while the cart travels.
     */
    @Override
    public void submit(@Nonnull GhostCartRenderState state,
        @Nonnull PoseStack pose,
        @Nonnull SubmitNodeCollector collector,
        @Nonnull CameraRenderState camera)
    {
        super.submit(state, pose, collector, camera); // cart model

        if (!state.hasTradeItem)
        {
            return;
        }

        pose.pushPose();
        pose.scale(1.20F, 1.20F, 1.20F);  // a bit larger than default block
        pose.translate(0.0, 0.1, 0.0);   // Sitting low in the cart

        final Quaternionf spin = Axis.YP.rotationDegrees(state.ageInTicks * 4F);
        pose.mulPose(spin);

        state.tradeItem.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

        pose.popPose();
    }
}
