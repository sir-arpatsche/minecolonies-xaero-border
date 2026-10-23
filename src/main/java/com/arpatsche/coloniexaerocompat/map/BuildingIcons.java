package com.arpatsche.coloniexaerocompat.map;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Finds the icon texture for a MineColonies building type.
 * Icons live in assets/minecoloniexaeroscompat/textures/gui/buildings/ as 48x48 PNGs.
 * Building types without an icon fall back to a coloured letter square.
 */
public final class BuildingIcons {

    /** Size of the icon PNG files in pixels. */
    public static final int TEXTURE_SIZE = 48;

    // building type id -> icon file name (without ".png")
    private static final Map<String, String> FILE_BY_TYPE = new HashMap<>();
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();
    private static final Set<String> REPORTED = new HashSet<>();

    static {
        // add(iconFile, typeIds...): several type ids may share one icon (alternative spellings)
        add("townhall", "townhall");
        add("builder", "builder");
        add("warehouse", "warehouse");
        add("courier", "courier", "deliveryman");
        add("tavern", "tavern");
        add("residence", "residence", "home", "citizen");
        add("hospital", "hospital");
        add("restaurant", "restaurant", "cook", "dininghall");
        add("lumberjack", "lumberjack", "forester");
        add("miner", "miner", "mine");
        add("netherworker", "netherworker", "nethermine");
        add("farmer", "farmer");
        add("fisherman", "fisherman", "fisher");
        add("cowboy", "cowboy", "cowhand");
        add("chickenherder", "chickenherder", "chickenfarmer");
        add("shepherd", "shepherd");
        add("rabbithutch", "rabbithutch", "rabbitherder");
        add("beekeeper", "beekeeper", "apiary");
        add("composter", "composter");
        add("quarrier", "quarrier", "quarry");
        add("blacksmith", "blacksmith");
        add("smeltery", "smeltery");
        add("baker", "baker", "bakery");
        add("kitchen", "kitchen", "cookery", "chef");
        add("sawmill", "sawmill");
        add("glassblower", "glassblower");
        add("dyer", "dyer");
        add("fletcher", "fletcher");
        add("concretemixer", "concretemixer");
        add("crusher", "crusher");
        add("sifter", "sifter");
        add("enchanter", "enchanter");
        add("guardtower", "guardtower");
        add("barracks", "barracks");
        add("barrackstower", "barrackstower");
        add("combatacademy", "combatacademy");
        add("archery", "archery");
        add("gatehouse", "gatehouse");
        add("postbox", "postbox");
        add("stash", "stash");
        add("supplycamp", "supplycamp", "supplychest");
        add("graveyard", "graveyard");
        add("library", "library");
        add("school", "school");
        add("university", "university");
        add("stable", "stable");
    }

    private BuildingIcons() {
    }

    private static void add(String file, String... typeIds) {
        for (String id : typeIds) {
            FILE_BY_TYPE.put(id, file);
        }
    }

    /** The icon texture for a building type, or null if there is none. */
    public static ResourceLocation getIcon(String typeId) {
        if (CACHE.containsKey(typeId)) {
            return CACHE.get(typeId);
        }

        ResourceLocation result = null;
        ResourceLocation location = null;
        String file = FILE_BY_TYPE.get(typeId);
        if (file != null) {
            location = ResourceLocation.fromNamespaceAndPath(
                    ColonyXaeroCompat.MODID, "textures/gui/buildings/" + file + ".png");
            if (Minecraft.getInstance().getResourceManager().getResource(location).isPresent()) {
                result = location;
            }
        }

        if (result == null && REPORTED.add(typeId)) {
            ColonyXaeroCompat.LOGGER.info("No icon for building type '{}' (looked for {})", typeId, location);
        }
        CACHE.put(typeId, result);
        return result;
    }
}
