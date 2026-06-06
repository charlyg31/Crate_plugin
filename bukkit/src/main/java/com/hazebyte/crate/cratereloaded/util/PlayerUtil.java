package com.hazebyte.crate.cratereloaded.util;

import com.hazebyte.crate.api.ServerVersion;
import com.hazebyte.crate.api.crate.Crate;
import com.hazebyte.crate.api.effect.Category;
import com.hazebyte.crate.api.event.CratePushbackEvent;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.util.item.ItemUtil;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

/** Various tools to assist and simplify code with Players. */
public class PlayerUtil {

    private static final Map<String, Class<?>> classCache = new HashMap<>();
    private static final Map<String, Method> methodCache = new HashMap<>();

    static {
        classCache.put("Inventory", Inventory.class);
        classCache.put("PlayerInventory", PlayerInventory.class);
        classCache.put("PlayerInteractEvent", PlayerInteractEvent.class);

        if (CorePlugin.getPlugin().getServerVersion().gte(ServerVersion.v1_9_R1)) {
            try {
                methodCache.put(
                        "getStorageContents", classCache.get("Inventory").getMethod("getStorageContents"));
                methodCache.put(
                        "getItemInMainHand", classCache.get("PlayerInventory").getMethod("getItemInMainHand"));
                methodCache.put(
                        "getItemInOffHand", classCache.get("PlayerInventory").getMethod("getItemInOffHand"));
                methodCache.put("getHand", classCache.get("PlayerInteractEvent").getMethod("getHand"));
            } catch (NoSuchMethodException e) {
                CorePlugin.getPlugin().getLogger().log(Level.SEVERE, "Failed to initialize reflection methods for 1.9+ inventory support", e);
            }
        }
    }

    // Max number of items in an inventory.
    private static final int MAX_IN_INVENTORY = 36 * 64;

    // Max number of items in a slot.
    private static final int MAX_IN_STACK = 64;

    /** PlayerUtil cannot be initialized. */
    private PlayerUtil() {}

    /**
     * Give an amount of items to a player. If the player does not have enough space in their
     * inventory, the items will be dropped on the ground.
     *
     * <p>This rate limits the amount of items to max number an inventory can hold.
     *
     * @param player
     * @param item
     * @param amount
     */
    public static void giveItemOrDrop(Player player, ItemStack item, int amount) {
        // An item can be a series of multiple
        int total = amount * item.getAmount();

        // This is rate limited. Should not be able to physically give more than this as a safety.
        // If the amount given is more than the maximum an inventory can hold, just give x amount.
        if (total > MAX_IN_INVENTORY) {
            total = getSlotsLeft(player) * MAX_IN_STACK;
        }

        while (total > 0) {
            ItemStack cloned = item.clone();
            int min = Math.min(total, MAX_IN_STACK);
            cloned.setAmount(min);
            total -= min;

            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), cloned);
            } else {
                player.getInventory().addItem(cloned);
            }
        }
    }

    /**
     * Returns the number of empty slots.
     *
     * @param player The player to check for.
     * @return the number of empty slot
     */
    public static int getSlotsLeft(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        // TODO: Should we check storage contents? Check if off handing is an extra slot.
        if (CorePlugin.getPlugin().getServerVersion().gte(ServerVersion.v1_9_R1)) {
            try {
                contents = (ItemStack[]) methodCache.get("getStorageContents").invoke(inventory);
                return (int) Arrays.stream(contents).filter(e -> e == null).count();
            } catch (Exception e) {
                CorePlugin.getPlugin().getLogger().log(Level.WARNING, "Failed to get storage contents for player inventory", e);
                return -1;
            }
        }

        int count = (int) Arrays.stream(contents).filter(e -> e == null).count();

        return count;
    }

    /**
     * Closes the inventory after some delay
     *
     * @param player
     * @param delay
     */
    public static void closeInventoryLater(final Player player, long delay) {
        Bukkit.getScheduler().runTaskLater(CorePlugin.getPlugin(), () -> player.closeInventory(), delay);
    }

    /**
     * Updates a player's inventory.
     *
     * @param player
     */
    public static void refreshInventory(final Player player) {
        // updateInventory() removed - deprecated in Paper 26.x
    }

    /**
     * Returns the item in hand.
     *
     * @param player
     * @return the item in hand and null otherwise
     */
    public static ItemStack getItemInHand(Player player) {
        return player.getItemInHand();
    }

    /**
     * Returns the item in offhand. If the version is below 1.9, this returns the item in main hand
     *
     * @param player
     * @return the item in offhand and null otherwise
     */
    public static ItemStack getItemInOffHand(Player player) {
        if (CorePlugin.getPlugin().getServerVersion().lt(ServerVersion.v1_9_R1)) {
            return getItemInHand(player);
        }

        try {
            Method method = methodCache.get("getItemInOffHand");
            Object object = method.invoke(player.getInventory());
            if (object instanceof ItemStack) {
                return (ItemStack) object;
            }
        } catch (Exception e) {
            CorePlugin.getPlugin().getLogger().log(Level.WARNING, "Failed to get item in off hand", e);
        }
        return null;
    }

    /**
     * Returns the item in main hand
     *
     * @param player
     * @return the item in hand
     */
    public static ItemStack getItemInMainHand(Player player) {
        if (CorePlugin.getPlugin().getServerVersion().lt(ServerVersion.v1_9_R1)) {
            return getItemInHand(player);
        }

        try {
            Method method = methodCache.get("getItemInMainHand");
            Object object = method.invoke(player.getInventory());
            if (object instanceof ItemStack) {
                return (ItemStack) object;
            }
        } catch (Exception e) {
            CorePlugin.getPlugin().getLogger().log(Level.WARNING, "Failed to get item in main hand", e);
        }
        return null;
    }

    /**
     * Returns the whether this is offhand or on hand.
     *
     * <p>If this is 1.8, this will return null
     *
     * @param listener
     * @return the hand
     */
    public static EquipmentSlot getHand(PlayerInteractEvent listener) {
        if (CorePlugin.getPlugin().getServerVersion().lt(ServerVersion.v1_9_R1)) {
            return null;
        }

        try {
            Method method = methodCache.get("getHand");
            Object object = method.invoke(listener);
            if (object instanceof EquipmentSlot) {
                return (EquipmentSlot) object;
            }
        } catch (Exception e) {
            CorePlugin.getPlugin().getLogger().log(Level.WARNING, "Failed to get hand from PlayerInteractEvent", e);
        }
        return null;
    }

    /**
     * Removes a single item from the player's hand. TODO: Rename to removeOneFromHand
     *
     * @param player
     */
    public static void removeOneFromHand(Player player) {
        ItemStack item = getItemInHand(player);
        int slot = player.getInventory().getHeldItemSlot();

        if (item.getAmount() == 1) {
            player.getInventory().setItem(slot, null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
        refreshInventory(player);
    }

    /**
     * This will attempt to remove one of the specific item from hand. If the item is not in hand,
     * it'll remove it from the inventory.
     *
     * @param player
     */
    public static void removeOneFromHandOrInv(Player player, ItemStack item) {
        ItemStack hand = getItemInHand(player);

        // If the item is in hand
        if (ItemUtil.compare(hand, item)) {
            removeOneFromHand(player);
            return;
        }

        // Remove any single one from the inventory.

        // If there is one item, remove it from hand
        if (item.getAmount() == 1) {
            player.getInventory().removeItem(item);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
        refreshInventory(player);
    }

    // TODO: Remove 2.1.0
    @Deprecated
    public static boolean isInventoryFull(Player p) {
        return p.getInventory().firstEmpty() == -1;
    }

    // TODO: Remove 2.1.0
    @Deprecated
    public static int getEmptySlot(Player p) {
        return p.getInventory().firstEmpty();
    }

    // TODO: Move vector stuff to it's own class
    @Deprecated
    public static void pushPlayerBack(Player player, Location blockLocation, Crate crate) {
        if (!(CorePlugin.getPlugin().getSettings().isPushbackInteractionEnabled())) {
            return;
        }

        Location difference = player.getLocation().subtract(blockLocation);

        Vector vector = difference.getDirection().multiply(-1);

        vector.setX(vector.getX() * CorePlugin.getPlugin().getSettings().getPushbackXModifier());
        vector.setY(vector.getY() * CorePlugin.getPlugin().getSettings().getPushbackYModifier());
        vector.setZ(vector.getZ() * CorePlugin.getPlugin().getSettings().getPushbackZModifier());

        CratePushbackEvent event = new CratePushbackEvent(player, vector);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            if (crate != null) crate.runEffect(blockLocation, Category.PUSHBACK, player);
            player.setVelocity(vector);
        }
    }
    // endregion MISC
}
