package com.hazebyte.crate.cratereloaded.util.item;

import com.google.common.base.Strings;
import com.hazebyte.crate.api.ServerVersion;
import com.hazebyte.crate.api.util.ItemHelper;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.util.Mat;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtil extends ItemHelper {

    private static final Map<String, Method> methodCache = new HashMap<>();

    private static final String IS_UNBREAKABLE = "isUnbreakable";

    private static final String GET_KEY = "getKey";

    static {
        try {
            if (CorePlugin.getPlugin().getServerVersion().gte(ServerVersion.v1_10_R1)) {
                methodCache.put(IS_UNBREAKABLE, ItemMeta.class.getMethod(IS_UNBREAKABLE));
            }
            if (CorePlugin.getPlugin().getServerVersion().gte(ServerVersion.v1_13_R1)) {
                methodCache.put(GET_KEY, Material.class.getMethod(GET_KEY));
            }
        } catch (NoSuchMethodException e) {
            // Expected on older Minecraft versions - methods not available
        }
    }

    private ItemUtil() {}

    public static Optional<Object> getType(Material material) {
        if (methodCache.containsKey(GET_KEY)) {
            Method method = methodCache.get(GET_KEY);

            CorePlugin.getPlugin().getLogger().finest(String.format("Attempting to getKey"));
            try {
                Object key = method.invoke(material);
                CorePlugin.getPlugin().getLogger().finest(String.format("getKey Result: [%s]", key));
                return Optional.of(key);
            } catch (Exception ignored) {
                CorePlugin.getPlugin()
                        .getLogger()
                        .finest(String.format(
                                "An error has occurred when attempting to getKey: [%s]", ignored.getMessage()));
            }
        }
        return Optional.empty();
    }

    public static boolean isUnbreakable(ItemMeta meta) {
        if (CorePlugin.getPlugin().getServerVersion().gte(ServerVersion.v1_10_R1)) {
            try {
                Object object = methodCache.get(IS_UNBREAKABLE).invoke(meta);
                return (boolean) object;
            } catch (Exception e) {
                CorePlugin.getPlugin()
                        .getLogger()
                        .log(java.util.logging.Level.FINE, "Failed to check unbreakable status via reflection", e);
            }
        }
        return false;
    }

    // ItemID:Durability
    public static ItemStack get(String id) {
        if (CorePlugin.getPlugin().getServerVersion().gte(ServerVersion.v1_13_R1)) {
            Material material;
            Mat mat = Mat.from(id);
            if (mat != null) {
                material = mat.toMaterial();
            } else {
                material = Material.matchMaterial(id);
            }
            if (material == null) {
                return null;
            }

            ItemStack item = new ItemStack(material);
            return item;
        } else {
            Mat mat = Mat.from(id);
            if (mat == null) {
                return null;
            }
            ItemStack item = mat.toItemStack();
            return item;
        }
    }

    public static boolean isNull(ItemStack item) {
        return (item == null || item.getType() == null || item.getType() == Mat.AIR.toMaterial());
    }

    public static String toKeyString(ItemStack item) {
        StringBuilder toString =
                (new StringBuilder("ItemStack{")).append(item.getType().name());
        if (item.hasItemMeta()) {
            toString.append(", ").append(item.getItemMeta());
        }

        return toString.append('}').toString();
    }

    public static boolean compare(ItemStack itemA, ItemStack itemB) {
        return compare(itemA, itemB, CorePlugin.getPlugin().getSettings().getCrateComparisonLevel());
    }

    public static boolean compare(ItemStack itemA, ItemStack itemB, int level) {
        if (level > 2 || level < 0) {
            level = 2;
        }

        if (isNull(itemA) || isNull(itemB)) {
            return false;
        }

        switch (level) {
            case 2:
            case 1:
                Map<Enchantment, Integer> mapA = itemA.getEnchantments();
                Map<Enchantment, Integer> mapB = itemB.getEnchantments();

                if (!(mapA.equals(mapB))) {
                    return false;
                }

                Set<ItemFlag> flagsA = itemA.getItemMeta().getItemFlags();
                Set<ItemFlag> flagsB = itemB.getItemMeta().getItemFlags();

                if (!(flagsA.equals(flagsB))) {
                    return false;
                }
            case 0:
                String nameA = getName(itemA);
                String nameB = getName(itemB);

                // If name exists, and they are not the same
                if (!Strings.isNullOrEmpty(nameA) && !Strings.isNullOrEmpty(nameB) && !(nameA.equals(nameB))) {
                    return false;
                }

                // If name exists, and the other doesn't
                if ((Strings.isNullOrEmpty(nameA) && !Strings.isNullOrEmpty(nameB))
                        || (!Strings.isNullOrEmpty(nameA) && Strings.isNullOrEmpty(nameB))) {
                    return false;
                }

                if (itemA.getType() != itemB.getType()) {
                    return false;
                }

                // If lore exists, and they are not the same.
                if ((getLore(itemA) != null && getLore(itemB) != null)
                        && !(getLore(itemA).equals(getLore(itemB)))) {
                    return false;
                }

                // If one lore exist, and the other doesn't
                if ((getLore(itemA) == null && getLore(itemB) != null)
                        || (getLore(itemA) != null && getLore(itemB) == null)) {
                    return false;
                }
        }
        return true;
    }
}
