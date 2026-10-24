package com.arpatsche.coloniexaerocompat.minimap;

import com.arpatsche.coloniexaerocompat.config.ClientConfig;
import com.arpatsche.coloniexaerocompat.map.ClaimElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderProvider;
import xaero.hud.minimap.element.render.MinimapElementRenderer;

import java.util.List;

/**
 * Draws every claimed chunk as a semi-transparent, rotated square on Xaero's minimap,
 * with an opaque border along the outer edge of each colony's area.
 */
public class MinimapClaimRenderer extends MinimapElementRenderer<ClaimElement, MinimapContext> {

    // We draw in units of 1/8 block so that thin borders are possible.
    private static final int UNITS_PER_BLOCK = 8;
    private static final int HALF = 8 * UNITS_PER_BLOCK; // half a chunk (8 blocks)
    private static final float BORDER_PIXELS = 1.5F;

    private static final int[] RGB = {
            0xFF5555, 0x5599FF, 0x55DD55, 0xFFCC33, 0xCC66FF, 0x33DDDD
    };
    private static final int FILL_ALPHA = 0x55 << 24;
    private static final int BORDER_ALPHA = 0xFF << 24;

    public MinimapClaimRenderer() {
        super(new Reader(), new Provider(), new MinimapContext());
    }

    @Override
    public int getOrder() {
        return 0; // below the building icons
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
        context.transformKnown = MinimapTransform.read(context);
    }

    @Override
    public void postRender(MinimapElementRenderInfo renderInfo, MultiBufferSource.BufferSource bufferSource,
                           MultiTextureRenderTypeRendererProvider rendererProvider) {
    }

    @Override
    public boolean renderElement(ClaimElement element, boolean highlighted, boolean outOfBounds, double optionalDepth,
                                 float optionalScale, double partialX, double partialY,
                                 MinimapElementRenderInfo renderInfo, GuiGraphics guiGraphics,
                                 MultiBufferSource.BufferSource bufferSource) {
        if (!context.transformKnown) {
            return false;
        }
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        // The minimap placed us at the rounded pixel position of the chunk centre; add the remainder.
        pose.translate(partialX, partialY, optionalDepth);
        pose.mulPose(Axis.ZP.rotation((float) context.angle));
        float scale = (float) (context.zoom / UNITS_PER_BLOCK);
        pose.scale(scale, scale, 1.0F);

        int rgb = RGB[Math.floorMod(element.colonyId(), RGB.length)];
        guiGraphics.fill(-HALF, -HALF, HALF, HALF, FILL_ALPHA | rgb);

        // Border thickness: about BORDER_PIXELS screen pixels, converted to our units
        int t = Math.round((float) (BORDER_PIXELS * UNITS_PER_BLOCK / context.zoom));
        t = Math.max(1, Math.min(t, HALF / 2));
        int border = BORDER_ALPHA | rgb;
        // Local -y is north (-Z), local -x is west (-X); the whole square is rotated with the minimap.
        if (element.openNorth()) {
            guiGraphics.fill(-HALF, -HALF, HALF, -HALF + t, border);
        }
        if (element.openSouth()) {
            guiGraphics.fill(-HALF, HALF - t, HALF, HALF, border);
        }
        if (element.openWest()) {
            guiGraphics.fill(-HALF, -HALF, -HALF + t, HALF, border);
        }
        if (element.openEast()) {
            guiGraphics.fill(HALF - t, -HALF, HALF, HALF, border);
        }
        pose.popPose();
        return true;
    }

    /** Where each claimed chunk is: the centre of the chunk. */
    static class Reader extends MinimapElementReader<ClaimElement, MinimapContext> {
        @Override
        public boolean isHidden(ClaimElement element, MinimapContext context) {
            return false;
        }

        @Override
        public double getRenderX(ClaimElement element, MinimapContext context, float partialTicks) {
            return element.chunkX() * 16 + 8.0;
        }

        @Override
        public double getRenderY(ClaimElement element, MinimapContext context, float partialTicks) {
            return 0;
        }

        @Override
        public double getRenderZ(ClaimElement element, MinimapContext context, float partialTicks) {
            return element.chunkZ() * 16 + 8.0;
        }

        // Our positions are already in the coordinates of the dimension the minimap shows.
        @Override
        public double getCoordinateScale(ClaimElement element, MinimapContext context, MinimapElementRenderInfo renderInfo) {
            return renderInfo.backgroundCoordinateScale;
        }

        @Override
        public int getInteractionBoxLeft(ClaimElement element, MinimapContext context, float partialTicks) {
            return 0;
        }

        @Override
        public int getInteractionBoxRight(ClaimElement element, MinimapContext context, float partialTicks) {
            return 0;
        }

        @Override
        public int getInteractionBoxTop(ClaimElement element, MinimapContext context, float partialTicks) {
            return 0;
        }

        @Override
        public int getInteractionBoxBottom(ClaimElement element, MinimapContext context, float partialTicks) {
            return 0;
        }

        @Override
        public int getRenderBoxLeft(ClaimElement element, MinimapContext context, float partialTicks) {
            return 0;
        }

        @Override
        public int getRenderBoxRight(ClaimElement element, MinimapContext context, float partialTicks) {
            return 0;
        }

        @Override
        public int getRenderBoxTop(ClaimElement element, MinimapContext context, float partialTicks) {
            return 0;
        }

        @Override
        public int getRenderBoxBottom(ClaimElement element, MinimapContext context, float partialTicks) {
            return 0;
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
    }

    /** Hands the claimed chunks near the minimap centre to the minimap, once per frame. */
    static class Provider extends MinimapElementRenderProvider<ClaimElement, MinimapContext> {
        private List<ClaimElement> elements = List.of();
        private int index = 0;

        @Override
        public void begin(MinimapElementRenderLocation location, MinimapContext context) {
            index = 0;
            if (!context.transformKnown || context.dimension == null || !ClientConfig.showClaims()) {
                elements = List.of();
                return;
            }
            elements = MinimapSources.claims(context.dimension, context.playerX, context.playerZ);
        }

        @Override
        public boolean hasNext(MinimapElementRenderLocation location, MinimapContext context) {
            return index < elements.size();
        }

        @Override
        public ClaimElement getNext(MinimapElementRenderLocation location, MinimapContext context) {
            return elements.get(index++);
        }

        @Override
        public void end(MinimapElementRenderLocation location, MinimapContext context) {
            elements = List.of();
        }
    }
}
