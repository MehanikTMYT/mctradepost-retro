package com.deathfrog.mctradepost.core.client.render;

import javax.annotation.Nonnull;
import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.api.entity.AirshipEntity;
import com.deathfrog.mctradepost.core.client.model.AirshipModel;
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
 * Airship renderer.
 * 26.1.2 port: render() is split into extractRenderState() (reads the entity) and submit() (draws from the snapshot),
 * following WagonRenderer; transforms and the floating trade item are unchanged from upstream.
 */
public class AirshipRenderer extends EntityRenderer<AirshipEntity, AirshipRenderer.AirshipRenderState>
{
    /** Render-thread-safe snapshot of an airship's orientation and carried item. */
    public static class AirshipRenderState extends EntityRenderState
    {
        public float yRot;
        public final ItemStackRenderState tradeItem = new ItemStackRenderState();
        public boolean hasTradeItem = false;
    }

    @SuppressWarnings("null")
    private static final @Nonnull Identifier TEXTURE = Identifier.fromNamespaceAndPath(
        MCTradePostMod.MODID, "textures/entity/airship.png");

    private final AirshipModel model;
    private final ItemModelResolver itemModelResolver;

    @SuppressWarnings("null")
    public AirshipRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        model = new AirshipModel(context.bakeLayer(ModelRegistryHandler.AIRSHIP));
        itemModelResolver = context.getItemModelResolver();
        shadowRadius = 1.0F;
    }

    @Override
    public AirshipRenderState createRenderState()
    {
        return new AirshipRenderState();
    }

    @SuppressWarnings("null")
    @Override
    public void extractRenderState(@Nonnull AirshipEntity airship, @Nonnull AirshipRenderState state, float partialTick)
    {
        super.extractRenderState(airship, state, partialTick);
        state.yRot = airship.getYRot(partialTick);

        ItemStack stack = airship.getTradeItem();
        state.hasTradeItem = !stack.isEmpty();
        if (state.hasTradeItem)
        {
            itemModelResolver.updateForNonLiving(state.tradeItem, stack, ItemDisplayContext.GROUND, airship);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void submit(@Nonnull AirshipRenderState state, @Nonnull PoseStack pose, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState camera)
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
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
            pose.translate(0.0D, 0.3D, 0.125D);
            pose.scale(0.9F, 0.9F, 0.9F);
            pose.mulPose(Axis.YP.rotationDegrees(state.ageInTicks * 4.0F));
            state.tradeItem.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            pose.popPose();
        }

        super.submit(state, pose, collector, camera);
    }
}
