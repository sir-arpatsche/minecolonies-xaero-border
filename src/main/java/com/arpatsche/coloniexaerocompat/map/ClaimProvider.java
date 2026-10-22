package com.arpatsche.coloniexaerocompat.map;

import com.arpatsche.coloniexaerocompat.cache.ClaimCache;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClaimProvider extends ElementRenderProvider<ClaimElement, ClaimContext> {

    private final List<ClaimElement> elements = new ArrayList<>();
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

        for (int colonyId : ClaimCache.getColonyIds(dimension)) {
            IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dimension);
            if (colony == null && playerDimension) {
                continue; // the colony no longer exists
            }
            String name = colony != null ? colony.getName() : "Colony " + colonyId;

            Set<ChunkPos> claims = ClaimCache.getClaims(dimension, colonyId);
            for (ChunkPos chunk : claims) {
                elements.add(new ClaimElement(
                        colonyId, name, chunk.x, chunk.z,
                        !claims.contains(new ChunkPos(chunk.x, chunk.z - 1)),  // north = -Z
                        !claims.contains(new ChunkPos(chunk.x, chunk.z + 1)),  // south = +Z
                        !claims.contains(new ChunkPos(chunk.x - 1, chunk.z)),  // west  = -X
                        !claims.contains(new ChunkPos(chunk.x + 1, chunk.z))   // east  = +X
                ));
            }
        }
    }

    @Override
    public boolean hasNext(ElementRenderLocation location, ClaimContext context) {
        return index < elements.size();
    }

    @Override
    public ClaimElement getNext(ElementRenderLocation location, ClaimContext context) {
        return elements.get(index++);
    }

    @Override
    public void end(ElementRenderLocation location, ClaimContext context) {
        elements.clear();
    }
}