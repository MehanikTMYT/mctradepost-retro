package com.deathfrog.mctradepost.core.client.render.souvenir;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.joml.Vector3fc;

import com.deathfrog.mctradepost.item.SouvenirItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/**
 * Renders an item wrapped by {@link SouvenirItem} as a souvenir in a glass block "snow globe".
 * <p>
 * Minecraft 26.1 removed the old geometry-loader + BlockEntityWithoutLevelRenderer (ISTER) pipeline
 * this used to be built on (both {@code IGeometryLoader} and {@code BlockEntityWithoutLevelRenderer}
 * itself are gone). Custom per-item 3D rendering is now done through the data-driven
 * {@link SpecialModelRenderer} system instead: an item model JSON references a registered
 * "special_model" type by id, that id resolves (via {@link SpecialModelRenderer.Unbaked#type()}'s
 * MapCodec) to an instance of this class, and {@link #submit} is called during the item's render
 * submission instead of a direct render() call. The {@code mctradepost:souvenir} type id used in
 * {@code assets/mctradepost/items/souvenir.json} is registered in
 * {@code MCTradePostMod.ClientModEvents#registerSpecialModelRenderers}, replacing the old
 * (now-removed) geometry loader {@code mctradepost:souvenir_loader}.
 */
public class SouvenirSpecialRenderer implements SpecialModelRenderer<Item>
{
    @Override
    public void submit(@Nullable final Item original,
        final PoseStack pose,
        final SubmitNodeCollector collector,
        final int lightCoords,
        final int overlayCoords,
        final boolean hasFoil,
        final int outlineColor)
    {
        if (original == null)
        {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        final ItemModelResolver resolver = mc.getItemModelResolver();

        // First, render a glass block as base "snow globe"
        final ItemStackRenderState glassState = new ItemStackRenderState();
        resolver.updateForTopItem(glassState, new ItemStack(Blocks.GLASS.asItem()), ItemDisplayContext.FIXED, mc.level, null, 0);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.75);
        pose.scale(1.2f, 1.2f, 1.2f);
        glassState.submit(pose, collector, lightCoords, overlayCoords, outlineColor);
        pose.popPose();

        // Then, render the original item inside the glass
        final ItemStackRenderState innerState = new ItemStackRenderState();
        resolver.updateForTopItem(innerState, new ItemStack(original), ItemDisplayContext.GROUND, mc.level, null, 0);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.7);
        pose.scale(0.7f, 0.7f, 0.7f);
        pose.mulPose(Axis.XP.rotationDegrees(90));
        final float rotation = mc.level != null ? (mc.level.getGameTime() % 360) * 1.0f : 0f;
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        innerState.submit(pose, collector, lightCoords, overlayCoords, outlineColor);
        pose.popPose();
    }

    @Override
    public void getExtents(final Consumer<Vector3fc> output)
    {
        // No custom culling extents beyond the default item bounds.
    }

    @Override
    @Nullable
    public Item extractArgument(final ItemStack stack)
    {
        return SouvenirItem.getOriginal(stack);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Item>
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        @Nullable
        public SpecialModelRenderer<Item> bake(final SpecialModelRenderer.BakingContext context)
        {
            return new SouvenirSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type()
        {
            return MAP_CODEC;
        }
    }
}
