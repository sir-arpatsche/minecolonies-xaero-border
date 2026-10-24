package com.arpatsche.coloniexaerocompat.minimap;

import com.arpatsche.coloniexaerocompat.config.ClientConfig;
import com.arpatsche.coloniexaerocompat.map.BuildingElement;
import com.arpatsche.coloniexaerocompat.map.BuildingIcons;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderProvider;
import xaero.hud.minimap.element.render.MinimapElementRenderer;

import java.util.List;

/**
 * Draws a small icon for every building on Xaero's minimap. The icon keeps its size when the minimap
 * rotates or zooms.
 */
public class MinimapBuildingRenderer extends MinimapElementRenderer<BuildingElement, MinimapContext> {

    /** Half the icon size in pixels on the minimap. */
    private static final int ICON_HALF = 6;
    private static final int FALLBACK_COLOR = 0xFF4A6FA5;
    private static final int OUTLINE_COLOR = 0xFF000000;

    public MinimapBuildingRenderer() {
        super(new Reader(), new Provider(), new MinimapContext());
    }

    @Override
    public int getOrder() {
        return 10; // above the claim areas
    }

    @Override
    public boolean shouldRender(MinimapElementRenderLocation location) {
        return location == MinimapElementRenderLocation.IN_MINIMAP;
    }

    @Override
    public void preRender(MinimapElementRenderInfo renderInfo, MultiBufferSource.BufferSource bufferSource,
                          MultiTextureRenderTypeRendererProvider rendererProvider) {
        context.dimension = renderInfo.mapDimension;
        context.playerX = renderInfo.renderPos.x;
        context.playerZ = renderInfo.renderPos.z;
    }

    @Override
    public void postRender(MinimapElementRenderInfo renderInfo, MultiBufferSource.BufferSource bufferSource,
                           MultiTextureRenderTypeRendererProvider rendererProvider) {
    }

    @Override
    public boolean renderElement(BuildingElement element, boolean highlighted, boolean outOfBounds, double optionalDepth,
                                 float optionalScale, double partialX, double partialY,
                                 MinimapElementRenderInfo renderInfo, GuiGraphics guiGraphics,
                                 MultiBufferSource.BufferSource bufferSource) {
        if (outOfBounds) {
            return false;
        }
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(partialX, partialY, optionalDepth);

        String typeKey = element.typeKey();
        String type = typeKey.substring(typeKey.lastIndexOf('.') + 1);
        ResourceLocation icon = BuildingIcons.getIcon(type);
        if (icon != null) {
            int size = ICON_HALF * 2;
            int tex = BuildingIcons.TEXTURE_SIZE;
            guiGraphics.blit(icon, -ICON_HALF, -ICON_HALF, size, size, 0f, 0f, tex, tex, tex, tex);
        } else {
            guiGraphics.fill(-ICON_HALF / 2 - 1, -ICON_HALF / 2 - 1, ICON_HALF / 2 + 1, ICON_HALF / 2 + 1, OUTLINE_COLOR);
            guiGraphics.fill(-ICON_HALF / 2, -ICON_HALF / 2, ICON_HALF / 2, ICON_HALF / 2, FALLBACK_COLOR);
        }
        pose.popPose();
        return true;
    }

    static class Reader extends MinimapElementReader<BuildingElement, MinimapContext> {
        @Override
        public boolean isHidden(BuildingElement element, MinimapContext context) {
            return false;
        }

        @Override
        public double getRenderX(BuildingElement element, MinimapContext context, float partialTicks) {
            return element.pos().getX() + 0.5;
        }

        @Override
        public double getRenderY(BuildingElement element, MinimapContext context, float partialTicks) {
            return element.pos().getY();
        }

        @Override
        public double getRenderZ(BuildingElement element, MinimapContext context, float partialTicks) {
            return element.pos().getZ() + 0.5;
        }

        // Our positions are already in the coordinates of the dimension the minimap shows.
        @Override
        public double getCoordinateScale(BuildingElement element, MinimapContext context, MinimapElementRenderInfo renderInfo) {
            return renderInfo.backgroundCoordinateScale;
        }

        @Override
        public int getInteractionBoxLeft(BuildingElement element, MinimapContext context, float partialTicks) {
            return -ICON_HALF;
        }

        @Override
        public int getInteractionBoxRight(BuildingElement element, MinimapContext context, float partialTicks) {
            return ICON_HALF;
        }

        @Override
        public int getInteractionBoxTop(BuildingElement element, MinimapContext context, float partialTicks) {
            return -ICON_HALF;
        }

        @Override
        public int getInteractionBoxBottom(BuildingElement element, MinimapContext context, float partialTicks) {
            return ICON_HALF;
        }

        @Override
        public int getRenderBoxLeft(BuildingElement element, MinimapContext context, float partialTicks) {
            return -ICON_HALF;
        }

        @Override
        public int getRenderBoxRight(BuildingElement element, MinimapContext context, float partialTicks) {
            return ICON_HALF;
        }

        @Override
        public int getRenderBoxTop(BuildingElement element, MinimapContext context, float partialTicks) {
            return -ICON_HALF;
        }

        @Override
        public int getRenderBoxBottom(BuildingElement element, MinimapContext context, float partialTicks) {
            return ICON_HALF;
        }

        @Override
        public int getLeftSideLength(BuildingElement element, Minecraft mc) {
            return 0;
        }

        @Override
        public String getMenuName(BuildingElement element) {
            return element.colonyName();
        }

        @Override
        public String getFilterName(BuildingElement element) {
            return element.colonyName();
        }

        @Override
        public int getMenuTextFillLeftPadding(BuildingElement element) {
            return 0;
        }

        @Override
        public int getRightClickTitleBackgroundColor(BuildingElement element) {
            return 0;
        }

        @Override
        public boolean shouldScaleBoxWithOptionalScale() {
            return false;
        }
    }

    static class Provider extends MinimapElementRenderProvider<BuildingElement, MinimapContext> {
        private List<BuildingElement> elements = List.of();
        private int index = 0;

        @Override
        public void begin(MinimapElementRenderLocation location, MinimapContext context) {
            index = 0;
            if (context.dimension == null || !ClientConfig.showBuildings()) {
                elements = List.of();
                return;
            }
            elements = MinimapSources.buildings(context.dimension, context.playerX, context.playerZ);
        }

        @Override
        public boolean hasNext(MinimapElementRenderLocation location, MinimapContext context) {
            return index < elements.size();
        }

        @Override
        public BuildingElement getNext(MinimapElementRenderLocation location, MinimapContext context) {
            return elements.get(index++);
        }

        @Override
        public void end(MinimapElementRenderLocation location, MinimapContext context) {
            elements = List.of();
        }
    }
}
