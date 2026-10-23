package com.arpatsche.coloniexaerocompat.map;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.element.render.ElementReader;
import xaero.map.element.render.ElementRenderLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Tells Xaero where a building icon is. The icon has a fixed size in screen pixels,
 * centred on the building, no matter how far the map is zoomed.
 */
public class BuildingReader extends ElementReader<BuildingElement, ClaimContext, BuildingRenderer> {

    /** Half the icon size in pixels (the icon is 2 * ICON_HALF wide). */
    static final int ICON_HALF = 14;

    /** Below this zoom (pixels per block) the icons are hidden so the map does not get cluttered. */
    static final double MIN_SCALE = 1.0;

    static String displayName(BuildingElement element) {
        return Component.translatable(element.typeKey()).getString();
    }

    @Override
    public boolean isHidden(BuildingElement element, ClaimContext context) {
        return context.scale < MIN_SCALE;
    }

    // Anchor: the middle of the building block
    @Override
    public double getRenderX(BuildingElement element, ClaimContext context, float partialTicks) {
        return element.pos().getX() + 0.5;
    }

    @Override
    public double getRenderZ(BuildingElement element, ClaimContext context, float partialTicks) {
        return element.pos().getZ() + 0.5;
    }

    // Box used for mouse hover
    @Override
    public int getInteractionBoxLeft(BuildingElement element, ClaimContext context, float partialTicks) {
        return -ICON_HALF;
    }

    @Override
    public int getInteractionBoxRight(BuildingElement element, ClaimContext context, float partialTicks) {
        return ICON_HALF;
    }

    @Override
    public int getInteractionBoxTop(BuildingElement element, ClaimContext context, float partialTicks) {
        return -ICON_HALF;
    }

    @Override
    public int getInteractionBoxBottom(BuildingElement element, ClaimContext context, float partialTicks) {
        return ICON_HALF;
    }

    // Box used to decide whether the element is on screen
    @Override
    public int getRenderBoxLeft(BuildingElement element, ClaimContext context, float partialTicks) {
        return -ICON_HALF;
    }

    @Override
    public int getRenderBoxRight(BuildingElement element, ClaimContext context, float partialTicks) {
        return ICON_HALF;
    }

    @Override
    public int getRenderBoxTop(BuildingElement element, ClaimContext context, float partialTicks) {
        return -ICON_HALF;
    }

    @Override
    public int getRenderBoxBottom(BuildingElement element, ClaimContext context, float partialTicks) {
        return ICON_HALF;
    }

    // Menu features we do not use
    @Override
    public int getLeftSideLength(BuildingElement element, Minecraft mc) {
        return 0;
    }

    @Override
    public String getMenuName(BuildingElement element) {
        return displayName(element);
    }

    @Override
    public String getFilterName(BuildingElement element) {
        return displayName(element);
    }

    @Override
    public int getMenuTextFillLeftPadding(BuildingElement element) {
        return 0;
    }

    @Override
    public int getRightClickTitleBackgroundColor(BuildingElement element) {
        return 0;
    }

    // The box is already given in final pixels, so no extra scaling.
    @Override
    public boolean shouldScaleBoxWithOptionalScale() {
        return false;
    }

    @Override
    public boolean isInteractable(ElementRenderLocation location, BuildingElement element) {
        return true;
    }

    @Override
    public Tooltip getTooltip(BuildingElement element, ClaimContext context, boolean overMenu) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(displayName(element)).withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal("Colony: " + element.colonyName()));
        lines.add(Component.literal("Position: " + element.pos().toShortString()));
        return new MultiLineTooltip(lines);
    }
}
