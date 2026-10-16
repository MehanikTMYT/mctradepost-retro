package com.deathfrog.mctradepost.core.client.render;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.core.blocks.BlockFeeder;
import com.deathfrog.mctradepost.core.blocks.BlockDredger;
import com.deathfrog.mctradepost.api.entity.pets.goals.scavenge.DredgerForageRange;
import com.deathfrog.mctradepost.api.entity.pets.goals.scavenge.ScavengeHarvestability;
import com.deathfrog.mctradepost.api.entity.pets.goals.scavenge.VegetationForageRange;
import com.deathfrog.mctradepost.api.entity.pets.PetRoles;
import com.deathfrog.mctradepost.core.entity.pets.scavenge.PetForagingJeiCache;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Renders a client-local wireframe around the forage-search volume associated
 * with a pet working block.
 * <p>
 * At most one area is displayed at a time. Feeders use the dimensions from
 * {@link VegetationForageRange}, including the different height for hanging
 * feeders, while dredgers use {@link DredgerForageRange}. The selected origin
 * is not persisted or sent to the server; it remains active only for the
 * current client session until disabled, replaced, or found to be invalid.
 * </p>
 */
@EventBusSubscriber(modid = MCTradePostMod.MODID, value = Dist.CLIENT)
public final class ForageAreaOverlay
{
    private static final int MAX_TARGET_HIGHLIGHTS = 2048;
    /**
     * Shared pipeline plumbing for {@code core/rendertype_lines}: the same shaders, uniforms, blend function, vertex
     * format and culling that vanilla's own {@code RenderPipelines.LINES}/{@code LINES_TRANSLUCENT} use. Only the depth
     * test differs between the obscured and visible passes below.
     */
    private static RenderPipeline.Builder linesPipeline(String path)
    {
        return RenderPipeline.builder()
            // withLocation(String) treats its whole argument as a bare path under the "minecraft"
            // namespace (it calls Identifier.withDefaultNamespace(), which rejects the ':' in
            // "mctradepost:pipeline/..."); use the Identifier overload to namespace it under this mod.
            .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath(MCTradePostMod.MODID, path))
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader("core/rendertype_lines")
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("Fog", UniformType.UNIFORM_BUFFER)
            .withUniform("Globals", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES);
    }

    /**
     * Main-target line type that draws only fragments behind existing world
     * depth. This makes obscured boundary segments visible without allowing the
     * bright pass to show through terrain.
     */
    @SuppressWarnings("null")
    private static final RenderType OBSCURED_LINES = RenderType.create(
        "mctradepost_forage_area_obscured_lines",
        RenderSetup.builder(
                linesPipeline("pipeline/forage_area_obscured_lines")
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, false))
                    .build())
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.MAIN_TARGET)
            .bufferSize(1536)
            .createRenderSetup());

    /**
     * Main-target line type that explicitly tests against the world depth
     * buffer, allowing this bright pass to cover only directly visible parts
     * of the darker complete boundary.
     */
    @SuppressWarnings("null")
    private static final RenderType VISIBLE_LINES = RenderType.create(
        "mctradepost_forage_area_visible_lines",
        RenderSetup.builder(
                linesPipeline("pipeline/forage_area_visible_lines")
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .build())
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.MAIN_TARGET)
            .bufferSize(1536)
            .createRenderSetup());

    /** Dimension containing the currently selected working block. */
    private static ResourceKey<Level> dimension;

    /** Position of the currently selected feeder or dredger. */
    private static BlockPos origin;

    /** Focus item captured when the player enabled this snapshot. */
    private static Item snapshotFocusItem;

    /** Role used to interpret readiness for the captured focused sources. */
    private static PetRoles snapshotRole;

    /** Compatible block types capable of producing the captured focus item. */
    private static Set<Block> snapshotSources = Set.of();

    /** Compatible focused-source positions captured when the overlay was enabled. */
    private static List<BlockPos> compatibleTargets = List.of();

    /** Captured sources that are currently ready to harvest. */
    private static List<BlockPos> readyTargets = List.of();

    /** Captured compatible sources that are not currently ready to harvest. */
    private static List<BlockPos> immatureTargets = List.of();

    /** Next client game time at which captured source readiness is refreshed. */
    private static long nextReadinessRefreshTime;

    /** Prevents instantiation of this event-driven utility class. */
    private ForageAreaOverlay() { }

    /**
     * Checks whether the overlay is currently enabled for a particular working
     * block.
     *
     * @param level client level containing the working block, or {@code null}
     * @param pos position of the working block to test
     * @return {@code true} when the supplied dimension and position match the
     *         currently selected overlay origin
     */
    public static boolean isEnabled(@Nullable Level level, BlockPos pos)
    {
        return level != null && pos != null && level.dimension().equals(dimension) && pos.equals(origin);
    }

    /**
     * Enables or disables the forage-area overlay.
     * <p>
     * Enabling replaces any previously selected origin. Disabling, or passing
     * a missing level or position, clears the current selection.
     * </p>
     *
     * @param level client level containing the selected working block
     * @param pos position of the selected feeder or dredger
     * @param focusStack focus item whose currently harvestable sources should
     *        be captured, or an empty stack to capture only the range
     * @param enabled {@code true} to display this area; {@code false} to clear
     *        the overlay
     */
    public static void setEnabled(
        @Nullable Level level, @Nullable BlockPos pos, final ItemStack focusStack, boolean enabled)
    {
        if (!enabled || level == null || pos == null)
        {
            clear();
            return;
        }
        dimension = level.dimension();
        origin = pos.immutable();
        snapshotFocusItem = focusStack == null || focusStack.isEmpty() ? null : focusStack.getItem();

        if (origin == null) return;

        compatibleTargets = captureFocusedTargets(level, origin, snapshotFocusItem);
        refreshTargetReadiness(level);
    }

    /**
     * Verifies that an open working-block screen still has the focus used by
     * the current snapshot. A changed focus invalidates the complete overlay.
     *
     * @return {@code true} when the overlay remains active and its focus agrees
     */
    public static boolean validateFocus(
        @Nullable final Level level, @Nullable final BlockPos pos, final ItemStack currentFocus)
    {
        if (!isEnabled(level, pos)) return false;
        final Item currentItem = currentFocus == null || currentFocus.isEmpty() ? null : currentFocus.getItem();
        if (currentItem == snapshotFocusItem) return true;
        clear();
        return false;
    }

    private static void clear()
    {
        dimension = null;
        origin = null;
        snapshotFocusItem = null;
        snapshotRole = null;
        snapshotSources = Set.of();
        compatibleTargets = List.of();
        readyTargets = List.of();
        immatureTargets = List.of();
        nextReadinessRefreshTime = 0L;
    }

    @SuppressWarnings("null")
    private static List<BlockPos> captureFocusedTargets(
        final Level level, final @Nonnull BlockPos workPos, @Nullable final Item focusItem)
    {
        snapshotRole = null;
        snapshotSources = Set.of();
        if (focusItem == null) return List.of();
        final BlockState workState = level.getBlockState(workPos);
        final PetRoles role;
        final int radius;
        final int minY;
        final int maxY;
        if (workState.getBlock() instanceof BlockFeeder)
        {
            final boolean hanging = workState.hasProperty(BlockFeeder.HANGING) && workState.getValue(BlockFeeder.HANGING);
            role = PetRoles.SCAVENGE_VEGETATION;
            radius = VegetationForageRange.HORIZONTAL_RADIUS;
            minY = VegetationForageRange.MIN_VERTICAL_OFFSET;
            maxY = VegetationForageRange.maxVerticalOffset(hanging);
        }
        else if (workState.getBlock() instanceof BlockDredger)
        {
            role = PetRoles.SCAVENGE_WATER;
            radius = DredgerForageRange.HORIZONTAL_RADIUS;
            minY = DredgerForageRange.MIN_VERTICAL_OFFSET;
            maxY = DredgerForageRange.MAX_VERTICAL_OFFSET;
        }
        else
        {
            return List.of();
        }

        final Set<Block> sources = new HashSet<>();
        PetForagingJeiCache.getEntries().stream()
            .filter(entry -> entry.role() == role)
            .filter(entry -> entry.outputs().stream().anyMatch(output -> output.getItem() == focusItem))
            .forEach(entry -> sources.add(BuiltInRegistries.BLOCK.getValue(entry.sourceBlock())));
        snapshotRole = role;
        snapshotSources = Set.copyOf(sources);
        if (sources.isEmpty()) return List.of();

        final List<BlockPos> matches = new ArrayList<>();
        for (BlockPos candidate : BlockPos.betweenClosed(
            workPos.offset(-radius, minY, -radius), workPos.offset(radius, maxY, radius)))
        {
            if (candidate == null) continue;

            if (!level.isLoaded(candidate)) continue;
            final BlockState state = level.getBlockState(candidate);
            if (!sources.contains(state.getBlock())) continue;
            matches.add(candidate.immutable());
            if (matches.size() >= MAX_TARGET_HIGHLIGHTS) break;
        }
        return List.copyOf(matches);
    }

    /** Reclassifies only the compatible positions captured in the snapshot. */
    private static void refreshTargetReadiness(final Level level)
    {
        final List<BlockPos> ready = new ArrayList<>();
        final List<BlockPos> immature = new ArrayList<>();
        for (BlockPos target : compatibleTargets)
        {
            if (target == null) continue;

            if (!level.isLoaded(target)) continue;
            final BlockState state = level.getBlockState(target);
            if (!snapshotSources.contains(state.getBlock())) continue;
            final boolean harvestable = snapshotRole == PetRoles.SCAVENGE_VEGETATION
                ? ScavengeHarvestability.isVegetationHarvestable(state)
                : ScavengeHarvestability.isDredgerHarvestable(state);
            (harvestable ? ready : immature).add(target);
        }
        readyTargets = List.copyOf(ready);
        immatureTargets = List.copyOf(immature);
        nextReadinessRefreshTime = level.getGameTime() + 10L;
    }

    /**
     * Draws the selected forage volume after block entities have rendered.
     * This stage retains the solid-world depth buffer and avoids the unreliable
     * Fabulous transparency target used while translucent blocks are rendered.
     * <p>
     * Rendering is skipped when the selection belongs to another dimension or
     * its chunk is not loaded. If the selected block is no longer a feeder or
     * dredger, the stale selection is cleared automatically.
     * </p>
     *
     * @param event level-render stage event providing the camera and pose stack
     */
    @SuppressWarnings("null")
    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterOpaqueFeatures event)
    {
        BlockPos localOrigin = origin;

        if (localOrigin == null) return;

        final Minecraft minecraft = Minecraft.getInstance();
        final Level level = minecraft.level;
        if (level == null || !level.dimension().equals(dimension) || !level.isLoaded(localOrigin)) return;
        if (level.getGameTime() >= nextReadinessRefreshTime) refreshTargetReadiness(level);

        final BlockState state = level.getBlockState(localOrigin);
        final int radius;
        final int minY;
        final int maxY;
        if (state.getBlock() instanceof BlockFeeder)
        {
            final boolean hanging = state.hasProperty(BlockFeeder.HANGING) && state.getValue(BlockFeeder.HANGING);
            radius = VegetationForageRange.HORIZONTAL_RADIUS;
            minY = VegetationForageRange.MIN_VERTICAL_OFFSET;
            maxY = VegetationForageRange.maxVerticalOffset(hanging);
        }
        else if (state.getBlock() instanceof BlockDredger)
        {
            radius = DredgerForageRange.HORIZONTAL_RADIUS;
            minY = DredgerForageRange.MIN_VERTICAL_OFFSET;
            maxY = DredgerForageRange.MAX_VERTICAL_OFFSET;
        }
        else
        {
            clear();
            return;
        }
        final AABB area = new AABB(
            localOrigin.getX() - radius, localOrigin.getY() + minY, localOrigin.getZ() - radius,
            localOrigin.getX() + radius + 1, localOrigin.getY() + maxY + 1, localOrigin.getZ() + radius + 1);
        final Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        final PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        final BufferSource buffers = minecraft.renderBuffers().bufferSource();
        final float lineWidth = minecraft.gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;

        // Complementary depth tests split the boundary into obscured and
        // directly visible fragments without relying on draw-order overlap.
        renderLineBox(pose, buffers.getBuffer(OBSCURED_LINES), area,
            0.04F, 0.28F, 0.07F, 0.65F, lineWidth);
        buffers.endBatch(OBSCURED_LINES);
        renderLineBox(pose, buffers.getBuffer(VISIBLE_LINES), area,
            0.2F, 1.0F, 0.25F, 0.9F, lineWidth);
        buffers.endBatch(VISIBLE_LINES);

        for (BlockPos target : immatureTargets)
        {
            renderLineBox(pose, buffers.getBuffer(OBSCURED_LINES),
                new AABB(target).inflate(0.003D), 0.24F, 0.035F, 0.012F, 0.68F, lineWidth);
        }
        buffers.endBatch(OBSCURED_LINES);
        for (BlockPos target : immatureTargets)
        {
            renderLineBox(pose, buffers.getBuffer(VISIBLE_LINES),
                new AABB(target).inflate(0.003D), 0.78F, 0.16F, 0.045F, 0.92F, lineWidth);
        }
        buffers.endBatch(VISIBLE_LINES);

        for (BlockPos target : readyTargets)
        {
            renderLineBox(pose, buffers.getBuffer(OBSCURED_LINES),
                new AABB(target).inflate(0.003D), 0.32F, 0.16F, 0.02F, 0.7F, lineWidth);
        }
        buffers.endBatch(OBSCURED_LINES);
        for (BlockPos target : readyTargets)
        {
            renderLineBox(pose, buffers.getBuffer(VISIBLE_LINES),
                new AABB(target).inflate(0.003D), 1.0F, 0.62F, 0.08F, 0.95F, lineWidth);
        }
        buffers.endBatch(VISIBLE_LINES);
        pose.popPose();
    }

    /**
     * Draws an AABB wireframe. Replaces the vanilla {@code LevelRenderer.renderLineBox} helper, which was removed when
     * line rendering moved onto {@link ShapeRenderer}'s per-vertex line-width vertex format.
     */
    private static void renderLineBox(
        PoseStack pose, VertexConsumer buffer, AABB box, float red, float green, float blue, float alpha, float width)
    {
        ShapeRenderer.renderShape(pose, buffer, Shapes.create(box), 0.0, 0.0, 0.0, ARGB.colorFromFloat(alpha, red, green, blue), width);
    }
}
