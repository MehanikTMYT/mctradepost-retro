package com.deathfrog.mctradepost.core.client.model;

import javax.annotation.Nonnull;

import com.deathfrog.mctradepost.core.client.render.WagonRenderer.WagonRenderState;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Java representation of the Blockbench tradecart model.
 * <p>
 * 26.1.2's {@code Model}/{@code EntityModel} hierarchy binds directly to a render-state type and exposes only
 * {@code setupAnim(S state)} (the classic five-argument limbSwing/ageInTicks/headYaw/headPitch overload is gone) -- so this
 * model is parameterized on {@link WagonRenderState} rather than {@code WagonEntity} itself, matching
 * {@code WagonRenderer#extractRenderState} which copies the entity's wheel rotation onto the render state on the main
 * thread. {@code renderToBuffer} is intentionally not overridden: the base {@code Model} implementation already delegates
 * to {@code root().render(...)}, which is exactly what the previous manual override did.
 * </p>
 */
public class TradeCartModel extends EntityModel<WagonRenderState>
{
    private final ModelPart[] wheels;

    public TradeCartModel(ModelPart root)
    {
        super(root);
        this.wheels = new ModelPart[] {
            root.getChild("wheel_back_left"), root.getChild("wheel_back_right"),
            root.getChild("wheel_front_left"), root.getChild("wheel_front_right")
        };
    }

    @SuppressWarnings("null")
    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        add(root, "chassis", 0, 0, 0,
            cube(52, 41, -4, -8, -8, 8, 5, 4)
                .texOffs(1, 28).addBox(-6.5F, -5.5F, -9, 13, 3, 3));

        addWheel(root, "wheel_back_left", -7.41667F, 4, 7.5F, true, false);
        addWheel(root, "wheel_back_right", 7.41667F, 4, 7.5F, false, false);
        addWheel(root, "wheel_front_left", -7.41667F, 4, -7.5F, true, true);
        addWheel(root, "wheel_front_right", 7.41667F, 4, -7.5F, false, true);

        add(root, "body", -4, 4, 7.5F,
            cube(50, 13, -2.5F, -2, 0.5F, 13, 5, 1)
                .texOffs(0, 0).addBox(-2.5F, -1, -11.5F, 13, 2, 12));

        CubeListBuilder cargo = cube(52, 50, -6, -2, 6.5F, 12, 6, 1)
            .texOffs(0, 53).addBox(-6, -2, -5.5F, 12, 6, 1)
            .texOffs(20, 67).addBox(-7, -2, -4, 1, 6, 1)
            .texOffs(20, 74).addBox(-7, -2, -1, 1, 6, 1)
            .texOffs(68, 74).addBox(-7, -2, 2, 1, 6, 1)
            .texOffs(72, 74).addBox(-7, -2, 5, 1, 6, 1)
            .texOffs(24, 75).addBox(6, -2, 2, 1, 6, 1)
            .texOffs(28, 75).addBox(6, -2, -1, 1, 6, 1)
            .texOffs(32, 75).addBox(6, -2, -4, 1, 6, 1)
            .texOffs(36, 75).addBox(6, -2, 5, 1, 6, 1)
            .texOffs(50, 0).addBox(5.5F, 1, -5, 1, 1, 12)
            .texOffs(50, 0).addBox(5.5F, -1, -5, 1, 1, 12)
            .texOffs(50, 0).addBox(-6.5F, -1, -5, 1, 1, 12)
            .texOffs(50, 0).addBox(-6.5F, 1, -5, 1, 1, 12);
        add(root, "cargo_area", 0, 8, 1, cargo);

        return LayerDefinition.create(mesh, 128, 128);
    }

    private static CubeListBuilder cube(int u, int v, float x, float y, float z, float dx, float dy, float dz)
    {
        return CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, dx, dy, dz);
    }

    @SuppressWarnings("null")
    private static void add(PartDefinition parent, @Nonnull String name, float x, float y, float z, @Nonnull CubeListBuilder cubes)
    {
        parent.addOrReplaceChild(name, cubes, PartPose.offset(x, 24 - y, z));
    }

    private static void addWheel(PartDefinition root, @Nonnull String name, float ox, float oy, float oz, boolean left, boolean front)
    {
        float x = left ? -0.58333F : -0.41667F;
        float z = front ? -3 : -3;
        CubeListBuilder wheel = cube(26, 63, x, -3, z, 1, 6, 6)
            .texOffs(40, 76).addBox(x, -2, -4, 1, 4, 1)
            .texOffs(40, 76).addBox(x, -2, 3, 1, 4, 1)
            .texOffs(10, 72).addBox(x, -4, -2, 1, 1, 4)
            .texOffs(10, 72).addBox(x, 3, -2, 1, 1, 4);
        if (left)
            wheel.texOffs(60, front ? 39 : 37).addBox(-1.08333F, -0.5F, -0.5F, 3, 1, 1);
        else
            wheel.texOffs(front ? 68 : 50, front ? 37 : 25).addBox(-1.91667F, -0.5F, -0.5F, 3, 1, 1);
        add(root, name, ox, oy, oz, wheel);
    }

    @Override
    public void setupAnim(@Nonnull WagonRenderState state)
    {
        float rotation = state.wheelRotation;
        for (ModelPart wheel : wheels) wheel.xRot = rotation;
    }
}
