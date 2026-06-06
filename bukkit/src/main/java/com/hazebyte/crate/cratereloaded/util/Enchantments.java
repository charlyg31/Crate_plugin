package com.hazebyte.crate.cratereloaded.util;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

public class Enchantments {

    private static final Map<String, Enchantment> BY_NAME = new HashMap<>();

    private static Enchantment get(String key) {
        return BY_NAME.computeIfAbsent(key, k ->
            Registry.ENCHANTMENT.get(NamespacedKey.minecraft(k)));
    }

    public static Enchantment getByName(String name) {
        if (name == null) return null;
        // Try direct minecraft key lookup
        String key = name.toLowerCase().replace(" ", "_");
        Enchantment e = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key));
        if (e != null) return e;
        // Try legacy name mappings
        switch (key) {
            case "protection_environmental": return get("protection");
            case "protection_fire": return get("fire_protection");
            case "protection_fall": return get("feather_falling");
            case "protection_explosions": return get("blast_protection");
            case "protection_projectile": return get("projectile_protection");
            case "oxygen": return get("respiration");
            case "water_worker": return get("aqua_affinity");
            case "damage_all": return get("sharpness");
            case "damage_undead": return get("smite");
            case "damage_arthropods": return get("bane_of_arthropods");
            case "loot_bonus_mobs": return get("looting");
            case "dig_speed": return get("efficiency");
            case "durability": return get("unbreaking");
            case "loot_bonus_blocks": return get("fortune");
            case "arrow_damage": return get("power");
            case "arrow_knockback": return get("punch");
            case "arrow_fire": return get("flame");
            case "arrow_infinite": return get("infinity");
            case "luck": return get("luck_of_the_sea");
            default: return null;
        }
    }

    public static Enchantment PROTECTION() { return get("protection"); }
    public static Enchantment FIRE_PROTECTION() { return get("fire_protection"); }
    public static Enchantment FEATHER_FALLING() { return get("feather_falling"); }
    public static Enchantment BLAST_PROTECTION() { return get("blast_protection"); }
    public static Enchantment PROJECTILE_PROTECTION() { return get("projectile_protection"); }
    public static Enchantment RESPIRATION() { return get("respiration"); }
    public static Enchantment AQUA_AFFINITY() { return get("aqua_affinity"); }
    public static Enchantment SHARPNESS() { return get("sharpness"); }
    public static Enchantment SMITE() { return get("smite"); }
    public static Enchantment BANE_OF_ARTHROPODS() { return get("bane_of_arthropods"); }
    public static Enchantment LOOTING() { return get("looting"); }
    public static Enchantment EFFICIENCY() { return get("efficiency"); }
    public static Enchantment UNBREAKING() { return get("unbreaking"); }
    public static Enchantment FORTUNE() { return get("fortune"); }
    public static Enchantment POWER() { return get("power"); }
    public static Enchantment PUNCH() { return get("punch"); }
    public static Enchantment FLAME() { return get("flame"); }
    public static Enchantment INFINITY() { return get("infinity"); }
    public static Enchantment LUCK_OF_THE_SEA() { return get("luck_of_the_sea"); }
}
