package com.deathfrog.mctradepost.item;

import com.deathfrog.mctradepost.MCTradePostMod;
import com.deathfrog.mctradepost.core.blocks.ExtendedTimberFrameType;
import com.deathfrog.mctradepost.core.blocks.ExtendedTimberFrameBlock;
import com.google.common.collect.ImmutableList;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.item.interfaces.IDoItem;
import com.ldtteam.domumornamentum.util.BlockUtils;
import com.ldtteam.domumornamentum.util.MaterialTextureDataUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class ExtendedTimberframeItem extends BlockItem implements IDoItem
{
    private final ExtendedTimberFrameBlock sourceBlock;

    public ExtendedTimberframeItem(final @Nonnull ExtendedTimberFrameBlock blockIn, final @Nonnull Properties builder)
    {
        super(blockIn, builder);
        this.sourceBlock = blockIn;
    }

    @Override
    public Component getName(final @Nonnull ItemStack stack)
    {
        final MaterialTextureData textureData = MaterialTextureData.readFromItemStack(stack);

        final IMateriallyTexturedBlockComponent centerComponent = sourceBlock.getComponents().get(1);
        final Block centerBlock =
            textureData.components().getOrDefault(centerComponent.getId(), centerComponent.getDefault());
        final Component centerBlockName = BlockUtils.getHoverName(centerBlock);

        return Component.translatable(com.ldtteam.domumornamentum.util.Constants.MOD_ID + ".timber.frame.name.format",
            centerBlockName);
    }

    @Override
    public void appendHoverText(final @Nonnull ItemStack stack,
        final @Nonnull TooltipContext tooltipContext,
        final @Nonnull TooltipDisplay tooltipDisplay,
        final @Nonnull Consumer<Component> tooltip,
        final @Nonnull TooltipFlag flagIn)
    {
        super.appendHoverText(stack, tooltipContext, tooltipDisplay, tooltip, flagIn);

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(stack);
        if (textureData.isEmpty())
        {
            textureData = MaterialTextureDataUtil.generateRandomTextureDataFrom(stack);
        }

        final ExtendedTimberFrameType type = sourceBlock.getExtendedTimberFrameType();
        tooltip.accept(Component.translatable(com.ldtteam.domumornamentum.util.Constants.MOD_ID + ".origin.tooltip"));
        tooltip.accept(Component.literal(""));
        tooltip.accept(Component.translatable(com.ldtteam.domumornamentum.util.Constants.MOD_ID + ".timber.frame.header"));
        tooltip.accept(Component.translatable(com.ldtteam.domumornamentum.util.Constants.MOD_ID + ".timber.frame.type.format",
            Component.translatable(MCTradePostMod.MODID + ".timber.frame.type." + type.getName())));

        final IMateriallyTexturedBlockComponent frameComponent = sourceBlock.getComponents().get(0);
        final Block frameBlock = textureData.components().getOrDefault(frameComponent.getId(), frameComponent.getDefault());
        final Component frameBlockName = BlockUtils.getHoverName(frameBlock);
        tooltip.accept(Component.translatable(com.ldtteam.domumornamentum.util.Constants.MOD_ID + ".desc.frame",
            Component.translatable(com.ldtteam.domumornamentum.util.Constants.MOD_ID + ".desc.material", frameBlockName)));

        final IMateriallyTexturedBlockComponent centerComponent = sourceBlock.getComponents().get(1);
        final Block centerBlock =
            textureData.components().getOrDefault(centerComponent.getId(), centerComponent.getDefault());
        final Component centerBlockName = BlockUtils.getHoverName(centerBlock);
        tooltip.accept(Component.translatable(com.ldtteam.domumornamentum.util.Constants.MOD_ID + ".desc.center",
            Component.translatable(com.ldtteam.domumornamentum.util.Constants.MOD_ID + ".desc.material", centerBlockName)));
    }

    @SuppressWarnings("null")
    @Override
    public List<Identifier> getInputIds()
    {
        return ImmutableList.of(Identifier.fromNamespaceAndPath(com.ldtteam.domumornamentum.util.Constants.MOD_ID, "frame"),
            Identifier.fromNamespaceAndPath(com.ldtteam.domumornamentum.util.Constants.MOD_ID, "center"));
    }

    @Override
    public Identifier getGroup()
    {
        return Identifier.fromNamespaceAndPath(com.ldtteam.domumornamentum.util.Constants.MOD_ID, "btimberframe");
    }
}
