package com.deathfrog.mctradepost.core.client.render;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.api.entity.WagonEntity;
import com.deathfrog.mctradepost.core.client.model.TradeCartModel;
import com.deathfrog.mctradepost.core.event.ModelRegistryHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders {@link WagonEntity} using the shared {@link TradeCartModel}.
 * <p>
 * 26.1.2 split entity rendering into a main-thread {@code extractRenderState} step (reads the entity) and a render-thread
 * {@code submit} step (reads only the resulting immutable {@link WagonRenderState}), replacing the old direct
 * {@code render(entity, yaw, partialTick, pose, buffers, light)} override. This mirrors the pattern already established by
 * {@link GhostCartRenderer} in this mod.
 * </p>
 */
public class WagonRenderer extends EntityRenderer<WagonEntity, WagonRenderer.WagonRenderState>
{
    /** Render-thread-safe snapshot of a wagon's per-frame animation and carried-item state. */
    public static class WagonRenderState extends EntityRenderState
    {
        public float yRot;
        public float wheelRotation;
        public final ItemStackRenderState tradeItem = new ItemStackRenderState();
        public boolean hasTradeItem = false;
    }

    @SuppressWarnings("null")
    private static final @Nonnull Identifier TEXTURE = Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, "textures/entity/tradecart.png");
    private final TradeCartModel model;
    private final ItemModelResolver itemModelResolver;

    @SuppressWarnings("null")
    public WagonRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        model = new TradeCartModel(context.bakeLayer(ModelRegistryHandler.TRADE_CART));
        itemModelResolver = context.getItemModelResolver();
        shadowRadius = 0.75F;
    }

    @Override
    public WagonRenderState createRenderState()
    {
        return new WagonRenderState();
    }

    @SuppressWarnings("null")
    @Override
    public void extractRenderState(@Nonnull WagonEntity wagon, @Nonnull WagonRenderState state, float partialTick)
    {
        super.extractRenderState(wagon, state, partialTick);
        state.yRot = wagon.getYRot(partialTick);
        state.wheelRotation = wagon.getWheelRotation();

        ItemStack stack = wagon.getTradeItem();
        state.hasTradeItem = !stack.isEmpty();
        if (state.hasTradeItem)
        {
            itemModelResolver.updateForNonLiving(state.tradeItem, stack, ItemDisplayContext.GROUND, wagon);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void submit(@Nonnull WagonRenderState state, @Nonnull PoseStack pose, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState camera)
    {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.5F, 0.0F);
        collector.submitModel(model, state, pose, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        pose.popPose();

        if (state.hasTradeItem)
        {
            pose.pushPose();
            pose.translate(0.0D, 0.72D, 0.0D);
            pose.scale(1.1F, 1.1F, 1.1F);
            pose.mulPose(Axis.YP.rotationDegrees(state.ageInTicks * 4.0F));
            state.tradeItem.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            pose.popPose();
        }
        super.submit(state, pose, collector, camera);
    }
}
