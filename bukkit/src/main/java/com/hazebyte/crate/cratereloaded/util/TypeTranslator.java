package com.hazebyte.crate.cratereloaded.util;

import com.hazebyte.crate.api.crate.AnimationType;
import com.hazebyte.crate.api.crate.CrateType;
import com.hazebyte.crate.api.crate.EndAnimationType;
import java.util.HashMap;
import java.util.Map;

public class TypeTranslator {

    private static final Map<String, CrateType> CRATE_TYPE_MAPPING = new HashMap<>();
    private static final Map<String, AnimationType> ANIMATION_TYPE_MAP = new HashMap<>();
    private static final Map<String, EndAnimationType> END_ANIMATION_TYPE_MAP = new HashMap<>();

    static {
        CRATE_TYPE_MAPPING.put("SUPPLY", CrateType.SUPPLY);
        CRATE_TYPE_MAPPING.put("MYSTERY", CrateType.MYSTERY);
        CRATE_TYPE_MAPPING.put("KEY", CrateType.KEY);

        ANIMATION_TYPE_MAP.put("CSGO_REVERSE", AnimationType.CSGO_REVERSE);
        ANIMATION_TYPE_MAP.put("CSGO", AnimationType.CSGO);
        ANIMATION_TYPE_MAP.put("ROULETTE", AnimationType.ROULETTE);
        ANIMATION_TYPE_MAP.put("RECTANGLE_REVERSE", AnimationType.RECTANGLE_REVERSE);
        ANIMATION_TYPE_MAP.put("RECTANGLE", AnimationType.RECTANGLE);
        ANIMATION_TYPE_MAP.put("HEART", AnimationType.HEART);
        ANIMATION_TYPE_MAP.put("NONE", AnimationType.NONE);

        END_ANIMATION_TYPE_MAP.put("RANDOM", EndAnimationType.RANDOM);
        END_ANIMATION_TYPE_MAP.put("BLANK", EndAnimationType.BLANK);
    }

    public static CrateType translateCrate(final String name) {
        if (CRATE_TYPE_MAPPING.containsKey(name.toUpperCase())) {
            return CRATE_TYPE_MAPPING.get(name.toUpperCase());
        }

        for (Map.Entry<String, CrateType> entry : CRATE_TYPE_MAPPING.entrySet()) {
            if (name.toUpperCase().contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        throw new IllegalArgumentException(String.format("Unable to find crateType, [%s]", name));
    }

    public static AnimationType translateAnimation(final String name) {
        try {
            return AnimationType.valueOf(name.toUpperCase());
        } catch (Exception ignored) {
        }

        return ANIMATION_TYPE_MAP.get(name.toUpperCase());
    }

    public static EndAnimationType translateEndAnimation(String name) {
        return END_ANIMATION_TYPE_MAP.get(name.toUpperCase());
    }
}
