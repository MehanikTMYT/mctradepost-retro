package com.deathfrog.mctradepost.core.client.render;

import java.util.List;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.api.entity.GhostBoatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the shared trade vehicle controller with the vanilla oak boat entity model.
 * <p>
 * 26.1.2 split entity rendering into a main-thread {@code extractRenderState} step and a render-thread {@code submit} step
 * reading only the resulting immutable {@link GhostBoatRenderState}; there is no per-entity {@code getTextureLocation}
 * hook to override any more (textures are supplied directly to {@code submitModelPart}), and individual
 * {@code ModelPart}s are submitted through {@link SubmitNodeCollector#submitModelPart} rather than rendered into a
 * {@code MultiBufferSource} buffer directly. This mirrors the pattern already established by {@link GhostCartRenderer}.
 * </p>
 */
public class GhostBoatRenderer extends EntityRenderer<GhostBoatEntity, GhostBoatRenderer.GhostBoatRenderState>
{
    /** Render-thread-safe snapshot of a ghost boat's per-frame animation and carried-item state. */
    public static class GhostBoatRenderState extends EntityRenderState
    {
        public float yRot;
        public final ItemStackRenderState tradeItem = new ItemStackRenderState();
        public boolean hasTradeItem = false;
    }

    @SuppressWarnings("null")
    private static final @Nonnull Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/boat/oak.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE);
    private final ItemModelResolver itemModelResolver;
    private final List<ModelPart> boatParts;
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;

    @SuppressWarnings("null")
    public GhostBoatRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        itemModelResolver = context.getItemModelResolver();
        ModelPart root = context.bakeLayer(ModelLayers.OAK_BOAT);
        leftPaddle = root.getChild("left_paddle");
        rightPaddle = root.getChild("right_paddle");
        boatParts = List.of(root.getChild("bottom"), root.getChild("back"), root.getChild("front"),
            root.getChild("right"), root.getChild("left"), leftPaddle, rightPaddle);
        shadowRadius = 0.8F;
    }

    @Override
    public GhostBoatRenderState createRenderState()
    {
        return new GhostBoatRenderState();
    }

    @SuppressWarnings("null")
    @Override
    public void extractRenderState(@Nonnull GhostBoatEntity entity, @Nonnull GhostBoatRenderState state, float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = entity.getYRot(partialTick);

        ItemStack stack = entity.getTradeItem();
        state.hasTradeItem = !stack.isEmpty();
        if (state.hasTradeItem)
        {
            itemModelResolver.updateForNonLiving(state.tradeItem, stack, ItemDisplayContext.GROUND, entity);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void submit(@Nonnull GhostBoatRenderState state, @Nonnull PoseStack pose, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState camera)
    {
        pose.pushPose();
        pose.translate(0.0F, 0.375F, 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        animatePaddles(state.ageInTicks);
        for (ModelPart part : boatParts)
        {
            collector.submitModelPart(part, pose, RENDER_TYPE, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();
        if (state.hasTradeItem)
        {
            pose.pushPose();
            pose.translate(0.0D, 0.55D, 0.0D);
            pose.scale(1.1F, 1.1F, 1.1F);
            pose.mulPose(Axis.YP.rotationDegrees(state.ageInTicks * 4.0F));
            state.tradeItem.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            pose.popPose();
        }
        super.submit(state, pose, collector, camera);
    }

    private void animatePaddles(float age)
    {
        animatePaddle(leftPaddle, age * 0.18F, false);
        animatePaddle(rightPaddle, age * 0.18F + (float) Math.PI, true);
    }

    private static void animatePaddle(ModelPart paddle, float phase, boolean right)
    {
        paddle.xRot = Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (Mth.sin(-phase) + 1.0F) / 2.0F);
        paddle.yRot = Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (Mth.sin(-phase + 1.0F) + 1.0F) / 2.0F);
        if (right) paddle.yRot = (float) Math.PI - paddle.yRot;
    }
}
