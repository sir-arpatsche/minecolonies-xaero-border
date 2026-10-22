package com.arpatsche.coloniexaerocompat.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import xaero.lib.client.gui.widget.Tooltip;

import java.util.List;

public class MultiLineTooltip extends Tooltip {

    private static final int BACKGROUND_COLOR = -939524096; // gleiche Farbe wie Xaero
    private final List<Component> lines;

    public MultiLineTooltip(List<Component> lines) {
        super(Component.literal(""));
        this.lines = lines;
    }

    @Override
    public void drawBox(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        Font font = Minecraft.getInstance().font;

        int maxTextWidth = 0;
        for (Component line : lines) {
            maxTextWidth = Math.max(maxTextWidth, font.width(line));
        }
        int boxWidth = maxTextWidth + 20;
        int boxHeight = 5 + lines.size() * 10 + 5;

        int drawX = x + 12;
        int drawY = y + 10;

        int overEdgeX = drawX + boxWidth - width;
        if (overEdgeX > 9) {
            drawX = x - 12 - boxWidth;
        } else if (overEdgeX > 0) {
            drawX -= overEdgeX;
        }
        if (drawX < 0) {
            drawX = 0;
        }

        int overEdgeY = drawY + boxHeight - height;
        if (overEdgeY > boxHeight / 2) {
            drawY = y - 10 - boxHeight;
        } else if (overEdgeY > 0) {
            drawY -= overEdgeY;
        }
        if (drawY < 0) {
            drawY = 0;
        }

        guiGraphics.fill(drawX, drawY, drawX + boxWidth, drawY + boxHeight, BACKGROUND_COLOR);
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(font, lines.get(i), drawX + 10, drawY + 6 + 10 * i, 0xFFFFFF);
        }
    }
}
