package com.arpatsche.coloniexaerocompat.map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;


public class ClaimRenderer extends ElementRenderer<ClaimElement, ClaimContext, ClaimRenderer> {

    private static final int BORDER_THICKNESS = 3;

    private static final int[] RGB = {
            0xFF5555,
            0x5599FF,
            0x55DD55,
            0xFFCC33,
            0xCC66FF,
            0x33DDDD
    };

    private static final int FILL_ALPHA = 0x55 << 24;
    private static final int FILL_ALPHA_HOVER = 0x99 << 24;
    private static final int BORDER_ALPHA = 0xFF << 24;

    public ClaimRenderer() {
        super(new ClaimContext(), new ClaimProvider(), new ClaimReader());
    }

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
    public void renderElementShadow(ClaimElement element, boolean highlighted, float optionalScale,
                                    double partialX, double partialY, ElementRenderInfo renderInfo,
                                    GuiGraphics guiGraphics, MultiBufferSource.BufferSource bufferSource,
                                    MultiTextureRenderTypeRendererProvider rendererProvider) {
    }

    @Override
    public boolean renderElement(ClaimElement element, boolean highlighted, double optionalDepth,
                                 float optionalScale, double partialX, double partialY,
                                 ElementRenderInfo renderInfo, GuiGraphics guiGraphics,
                                 MultiBufferSource.BufferSource bufferSource,
                                 MultiTextureRenderTypeRendererProvider rendererProvider) {
        int size = (int) Math.round(16 * context.scale);
        int rgb = RGB[Math.floorMod(element.colonyId(), RGB.length)];

        int fillColor = (highlighted ? FILL_ALPHA_HOVER : FILL_ALPHA) | rgb;
        guiGraphics.fill(0, 0, size, size, fillColor);

        int t = Math.clamp(size / 2, 1, BORDER_THICKNESS);
        int borderColor = BORDER_ALPHA | rgb;
        if (element.openNorth()) {
            guiGraphics.fill(0, 0, size, t, borderColor);
        }
        if (element.openSouth()) {
            guiGraphics.fill(0, size - t, size, size, borderColor);
        }
        if (element.openWest()) {
            guiGraphics.fill(0, 0, t, size, borderColor);
        }
        if (element.openEast()) {
            guiGraphics.fill(size - t, 0, size, size, borderColor);
        }
        return true;
    }

    @Override
    public boolean shouldRender(ElementRenderLocation location, boolean pre) {
        return !pre && location == ElementRenderLocation.WORLD_MAP;
    }

    @Override
    public boolean shouldBeDimScaled() {
        return false;
    }
}