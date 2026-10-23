package com.arpatsche.coloniexaerocompat.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;

/**
 * Draws an icon for every building. Types without an icon texture get a small
 * coloured square with a letter instead.
 */
public class BuildingRenderer extends ElementRenderer<BuildingElement, ClaimContext, BuildingRenderer> {

    private static final int OUTLINE_COLOR = 0x99FFFFFF;
    private static final int HOVER_OUTLINE_COLOR = 0xFFFFFFFF;

    public BuildingRenderer() {
        super(new ClaimContext(), new BuildingProvider(), new BuildingReader());
    }

    // Draw after (on top of) the claim areas, which use the default order 0.
    @Override
    public int getOrder() {
        return 10;
    }

    // Our positions are already in the coordinates of the dimension shown on the map.
    @Override
    public boolean shouldBeDimScaled() {
        return false;
    }

    // Remember the current zoom and the dimension shown on the map.
    @Override
    public void preRender(ElementRenderInfo renderInfo, MultiBufferSource.BufferSource bufferSource,
                          MultiTextureRenderTypeRendererProvider rendererProvider, boolean pre) {
        context.scale = renderInfo.scale;
        context.dimension = renderInfo.mapDimension;
    }

    @Override
    public void postRender(ElementRenderInfo renderInfo, MultiBufferSource.BufferSource bufferSource,
                           MultiTextureRenderTypeRendererProvider rendererProvider, boolean pre) {
    }

    @Override
    public void renderElementShadow(BuildingElement element, boolean highlighted, float optionalScale,
                                    double partialX, double partialY, ElementRenderInfo renderInfo,
                                    GuiGraphics guiGraphics, MultiBufferSource.BufferSource bufferSource,
                                    MultiTextureRenderTypeRendererProvider rendererProvider) {
    }

    @Override
    public boolean renderElement(BuildingElement element, boolean highlighted, double optionalDepth,
                                 float optionalScale, double partialX, double partialY,
                                 ElementRenderInfo renderInfo, GuiGraphics guiGraphics,
                                 MultiBufferSource.BufferSource bufferSource,
                                 MultiTextureRenderTypeRendererProvider rendererProvider) {
        int half = BuildingReader.ICON_HALF;
        String type = typeId(element.typeKey());

        // Xaero has already moved the origin to the anchor point (middle of the building).
        if (highlighted) {
            guiGraphics.fill(-half - 2, -half - 2, half + 2, half + 2, HOVER_OUTLINE_COLOR);
        }

        ResourceLocation icon = BuildingIcons.getIcon(type);
        if (icon != null) {
            int size = half * 2;
            int tex = BuildingIcons.TEXTURE_SIZE;
            guiGraphics.blit(icon, -half, -half, size, size, 0f, 0f, tex, tex, tex, tex);
            return true;
        }

        // Fallback: coloured square with a letter
        int background = 0xFF000000 | colorFor(type);
        guiGraphics.fill(-half - 1, -half - 1, half + 1, half + 1, OUTLINE_COLOR);
        guiGraphics.fill(-half, -half, half, half, background);
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawCenteredString(font, letterFor(type), 0, -4, 0xFFFFFFFF);
        return true;
    }

    // Only draw in the main pass (pre == false) and only on the world map.
    @Override
    public boolean shouldRender(ElementRenderLocation location, boolean pre) {
        return !pre && location == ElementRenderLocation.WORLD_MAP;
    }

    /** "com.minecolonies.building.guardtower" -> "guardtower" */
    private static String typeId(String typeKey) {
        return typeKey.substring(typeKey.lastIndexOf('.') + 1);
    }

    private static int colorFor(String type) {
        return switch (type) {
            case "townhall" -> 0xD9A21B;
            case "residence" -> 0x3C9A3C;
            case "guardtower", "barracks", "barrackstower" -> 0xC03030;
            case "builder" -> 0xD07020;
            case "warehouse" -> 0x8B5A2B;
            default -> 0x4A6FA5;
        };
    }

    private static String letterFor(String type) {
        return switch (type) {
            case "townhall" -> "T";
            case "residence" -> "H";
            case "guardtower", "barracks", "barrackstower" -> "G";
            case "warehouse" -> "W";
            default -> type.isEmpty() ? "?" : type.substring(0, 1).toUpperCase();
        };
    }
}