package com.arpatsche.coloniexaerocompat.map;

import com.arpatsche.coloniexaerocompat.cache.ClaimCache;
import com.arpatsche.coloniexaerocompat.cache.ColonyInfoCache;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.element.render.ElementReader;
import xaero.map.element.render.ElementRenderLocation;

import java.util.ArrayList;
import java.util.List;

public class ClaimReader extends ElementReader<ClaimElement, ClaimContext, ClaimRenderer> {

    private static int sizeInPixels(ClaimContext context) {
        return (int) Math.round(16 * context.scale);
    }

    @Override
    public boolean isHidden(ClaimElement element, ClaimContext context) {
        return false;
    }

    @Override
    public double getRenderX(ClaimElement element, ClaimContext context, float partialTicks) {
        return element.chunkX() * 16.0;
    }

    @Override
    public double getRenderZ(ClaimElement element, ClaimContext context, float partialTicks) {
        return element.chunkZ() * 16.0;
    }

    @Override
    public int getInteractionBoxLeft(ClaimElement element, ClaimContext context, float partialTicks) {
        return 0;
    }

    @Override
    public int getInteractionBoxRight(ClaimElement element, ClaimContext context, float partialTicks) {
        return sizeInPixels(context);
    }

    @Override
    public int getInteractionBoxTop(ClaimElement element, ClaimContext context, float partialTicks) {
        return 0;
    }

    @Override
    public int getInteractionBoxBottom(ClaimElement element, ClaimContext context, float partialTicks) {
        return sizeInPixels(context);
    }

    @Override
    public int getRenderBoxLeft(ClaimElement element, ClaimContext context, float partialTicks) {
        return 0;
    }

    @Override
    public int getRenderBoxRight(ClaimElement element, ClaimContext context, float partialTicks) {
        return sizeInPixels(context);
    }

    @Override
    public int getRenderBoxTop(ClaimElement element, ClaimContext context, float partialTicks) {
        return 0;
    }

    @Override
    public int getRenderBoxBottom(ClaimElement element, ClaimContext context, float partialTicks) {
        return sizeInPixels(context);
    }

    @Override
    public int getLeftSideLength(ClaimElement element, Minecraft mc) {
        return 0;
    }

    @Override
    public String getMenuName(ClaimElement element) {
        return element.colonyName();
    }

    @Override
    public String getFilterName(ClaimElement element) {
        return element.colonyName();
    }

    @Override
    public int getMenuTextFillLeftPadding(ClaimElement element) {
        return 0;
    }

    @Override
    public int getRightClickTitleBackgroundColor(ClaimElement element) {
        return 0;
    }

    @Override
    public boolean shouldScaleBoxWithOptionalScale() {
        return false;
    }

    @Override
    public boolean isInteractable(ElementRenderLocation location, ClaimElement element) {
        return true;
    }

    @Override
    public Tooltip getTooltip(ClaimElement element, ClaimContext context, boolean overMenu) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }

        ResourceKey<Level> dimension = context.dimension != null ? context.dimension : level.dimension();
        int claimCount = ClaimCache.getClaims(dimension, element.colonyId()).size();
        IColonyView colony = IColonyManager.getInstance().getColonyView(element.colonyId(), dimension);
        BlockPos center = colony != null ? colony.getCenter()
                : ColonyInfoCache.getCenter(dimension, element.colonyId());

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(element.colonyName()).withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal("ID: " + element.colonyId()));
        lines.add(Component.literal("Dimension: " + dimension.location()));
        lines.add(Component.literal("Chunks: " + claimCount));
        if (center != null) {
            lines.add(Component.literal("Center: " + center.toShortString()));
        }
        return new MultiLineTooltip(lines);
    }
}
