package com.arpatsche.coloniexaerocompat.map;

import com.arpatsche.coloniexaerocompat.cache.BuildingCache;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Feeds Xaero all remembered buildings of the dimension the map is showing.
 */
public class BuildingProvider extends ElementRenderProvider<BuildingElement, ClaimContext> {

    private final List<BuildingElement> elements = new ArrayList<>();
    private int index = 0;

    @Override
    public void begin(ElementRenderLocation location, ClaimContext context) {
        elements.clear();
        index = 0;

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        // Show the dimension the map is displaying, not the one the player is standing in.
        ResourceKey<Level> dimension = context.dimension != null ? context.dimension : level.dimension();
        boolean playerDimension = dimension.equals(level.dimension());

        for (int colonyId : BuildingCache.getColonyIds(dimension)) {
            IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dimension);
            if (colony == null && playerDimension) {
                continue; // the colony no longer exists
            }
            String name = colony != null ? colony.getName() : "Colony " + colonyId;

            for (BuildingCache.Info info : BuildingCache.getBuildings(dimension, colonyId)) {
                elements.add(new BuildingElement(colonyId, name, info.typeKey(), info.pos()));
            }
        }
    }

    @Override
    public boolean hasNext(ElementRenderLocation location, ClaimContext context) {
        return index < elements.size();
    }

    @Override
    public BuildingElement getNext(ElementRenderLocation location, ClaimContext context) {
        return elements.get(index++);
    }

    @Override
    public void end(ElementRenderLocation location, ClaimContext context) {
        elements.clear();
    }
}
