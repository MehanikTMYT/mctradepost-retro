package com.deathfrog.mctradepost.core.client.render;

import java.util.List;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.network.TradePathDebugPacket.VisualSegment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Renders a temporary, player-local visualization of a production trade route.
 * <p>
 * Route snapshots are supplied by {@link com.deathfrog.mctradepost.network.TradePathDebugPacket}
 * and remain visible for thirty seconds of client game time. Each route segment
 * is drawn in a color associated with its transport type, while the first and
 * last non-empty path positions receive white endpoint markers. Segments in
 * dimensions other than the client's current dimension are ignored.
 * </p>
 * <p>
 * {@code LevelRenderer.renderLineBox} and the old {@code RenderType.lines()} texture-free line pass were removed in
 * 26.1.2 in favor of {@link ShapeRenderer} plus the line-width vertex format ({@code POSITION_COLOR_NORMAL_LINE_WIDTH}),
 * matching the pattern already established by {@link ForageAreaOverlay} in this mod.
 * </p>
 */
@EventBusSubscriber(modid = MCTradePostMod.MODID, value = Dist.CLIENT)
public final class TradePathDebugOverlay
{
    /** Number of client game ticks for which a received route remains visible. */
    private static final long DURATION_TICKS = 20L * 30L;

    /** Immutable snapshot of the route segments currently being displayed. */
    private static List<VisualSegment> segments = List.of();

    /** Client game time at which the current route snapshot expires. */
    private static long expiresAt;

    /** Prevents instantiation of this event-driven utility class. */
    private TradePathDebugOverlay() { }

    /**
     * Replaces the current route snapshot and restarts its display duration.
     *
     * @param newSegments route segments to display; the list is defensively
     *        copied, although the contained segment records are not copied
     */
    public static void show(List<VisualSegment> newSegments)
    {
        Minecraft minecraft = Minecraft.getInstance();
        segments = List.copyOf(newSegments);
        expiresAt = minecraft.level == null ? DURATION_TICKS : minecraft.level.getGameTime() + DURATION_TICKS;
    }

    /** Clears the current route snapshot and its expiration time. */
    public static void clear()
    {
        segments = List.of();
        expiresAt = 0;
    }

    /**
     * Draws the active route snapshot after opaque world geometry and block-entity/feature renderers have run.
     * <p>
     * Nodes outside the client's effective render distance and segments from
     * other dimensions are skipped. An expired snapshot is cleared on the next
     * applicable render event.
     * </p>
     *
     * @param event level-render stage event providing the camera and pose stack
     */
    @SuppressWarnings("null")
    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterOpaqueFeatures event)
    {
        if (segments.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level.getGameTime() >= expiresAt)
        {
            clear();
            return;
        }

        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        double renderDistance = Math.max(32, minecraft.options.getEffectiveRenderDistance() * 16 + 16);
        double renderDistanceSqr = renderDistance * renderDistance;
        float lineWidth = minecraft.gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        var buffers = minecraft.renderBuffers().bufferSource();
        RenderType lineType = RenderTypes.lines();
        var lines = buffers.getBuffer(lineType);

        for (VisualSegment segment : segments)
        {
            ClientLevel localLevel = minecraft.level;

            if (localLevel == null) continue;

            if (!localLevel.dimension().identifier().equals(segment.dimension())) continue;
            float[] color = color(segment.type());
            int packedColor = ARGB.colorFromFloat(0.92F, color[0], color[1], color[2]);
            List<BlockPos> path = segment.path();
            for (int index = 0; index < path.size(); index++)
            {
                BlockPos pos = path.get(index);
                if (pos.distToCenterSqr(camera.x, camera.y, camera.z) > renderDistanceSqr) continue;
                ShapeRenderer.renderShape(pose, lines, Shapes.create(new AABB(pos).inflate(0.012D)), 0.0, 0.0, 0.0, packedColor, lineWidth);
                if (index + 1 < path.size()) drawConnection(pose, lines, pos, path.get(index + 1), packedColor, lineWidth);
            }
        }

        BlockPos first = endpoint(true);
        BlockPos last = endpoint(false);
        int white = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
        if (first != null) ShapeRenderer.renderShape(pose, lines, Shapes.create(new AABB(first).inflate(0.045D)), 0.0, 0.0, 0.0, white, lineWidth);
        if (last != null) ShapeRenderer.renderShape(pose, lines, Shapes.create(new AABB(last).inflate(0.045D)), 0.0, 0.0, 0.0, white, lineWidth);
        buffers.endBatch(lineType);
        pose.popPose();
    }

    /**
     * Draws a line between the centers of two consecutive route nodes.
     *
     * @param pose pose stack translated into world coordinates
     * @param lines vertex consumer for the line render type
     * @param from starting route node
     * @param to ending route node
     * @param packedColor packed ARGB color used for both line vertices
     * @param lineWidth device-appropriate line width, matching {@link ShapeRenderer}'s wireframe boxes
     */
    @SuppressWarnings("null")
    private static void drawConnection(PoseStack pose, VertexConsumer lines, BlockPos from, BlockPos to, int packedColor, float lineWidth)
    {
        float x1 = from.getX() + 0.5F;
        float y1 = from.getY() + 0.5F;
        float z1 = from.getZ() + 0.5F;
        float x2 = to.getX() + 0.5F;
        float y2 = to.getY() + 0.5F;
        float z2 = to.getZ() + 0.5F;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length == 0) return;
        PoseStack.Pose last = pose.last();
        lines.addVertex(last, x1, y1, z1).setColor(packedColor).setNormal(last, dx / length, dy / length, dz / length).setLineWidth(lineWidth);
        lines.addVertex(last, x2, y2, z2).setColor(packedColor).setNormal(last, dx / length, dy / length, dz / length).setLineWidth(lineWidth);
    }

    /**
     * Finds the first or last node among the non-empty displayed segments.
     *
     * @param first {@code true} to search from the beginning of the route;
     *        {@code false} to search from the end
     * @return the requested endpoint, or {@code null} when every path is empty
     */
    private static BlockPos endpoint(boolean first)
    {
        for (int i = first ? 0 : segments.size() - 1; first ? i < segments.size() : i >= 0; i += first ? 1 : -1)
        {
            List<BlockPos> path = segments.get(i).path();
            if (!path.isEmpty()) return first ? path.getFirst() : path.getLast();
        }
        return null;
    }

    /**
     * Selects the overlay color associated with a route segment type.
     *
     * @param type transport or transition type represented by the segment
     * @return a newly allocated array containing red, green, and blue values
     */
    private static float[] color(com.deathfrog.mctradepost.core.entity.ai.workers.trade.TrackRoute.SegmentType type)
    {
        return switch (type)
        {
            case RAIL -> new float[] {1.0F, 0.72F, 0.08F};
            case ROAD -> new float[] {0.18F, 0.95F, 0.28F};
            case WATER -> new float[] {0.12F, 0.52F, 1.0F};
            case AIR -> new float[] {0.72F, 0.92F, 1.0F};
            case AIR_TRANSIT -> new float[] {0.42F, 0.72F, 1.0F};
            case DOCK -> new float[] {0.05F, 1.0F, 1.0F};
            case MOORING -> new float[] {0.88F, 0.88F, 1.0F};
            case INTERCHANGE -> new float[] {0.72F, 0.2F, 1.0F};
            case TRANSFER -> new float[] {1.0F, 0.12F, 0.72F};
        };
    }
}
