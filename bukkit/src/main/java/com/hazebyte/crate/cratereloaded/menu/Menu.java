package com.hazebyte.crate.cratereloaded.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class Menu {

    protected JavaPlugin plugin;
    protected String name;
    protected Size size;
    private Grid items;
    private Menu parent, child;

    public Menu(JavaPlugin plugin, String name, Size size) {
        this.plugin = plugin;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.size = size;
        this.items = new Grid(size.getSize());
    }

    public String getName() {
        return name;
    }

    public Size getSize() {
        return size;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public Menu getParent() {
        return parent;
    }

    public void setParent(Menu parent) {
        this.parent = parent;
    }

    public boolean hasChild() {
        return child != null;
    }

    public Menu getChild() {
        return child;
    }

    public void setChild(Menu child) {
        this.child = child;
    }

    public Grid getGrid() {
        return items;
    }

    public Menu setItem(int position, Button button) {
        items.setItem(position, button);
        return this;
    }

    public Menu setEmptySlots(Button button) {
        java.util.stream.IntStream.range(0, items.length())
                .filter(i -> items.get(i) == null)
                .forEach(i -> items.setItem(i, button));
        return this;
    }

    public void open(Player player) {
        if (!MenuListener.getInstance().isRegistered(plugin)) {
            MenuListener.getInstance().register(plugin);
        }
        Inventory inventory = Bukkit.createInventory(
                new MenuHolder(this, Bukkit.createInventory(player, size.getSize())), size.getSize(), name);
        apply(inventory, player);
        player.openInventory(inventory);
    }

    public void close(Player player) {
        if (player == null) {
            return;
        }

        if (player.getOpenInventory().getTopInventory() == null) {
            return;
        }

        InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();

        if (holder instanceof MenuHolder) {
            MenuHolder menuHolder = (MenuHolder) holder;
            if (menuHolder.getMenu().equals(this)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, player::closeInventory, 1);
            }
        }
    }

    public void update(Player player) {
        if (player.getOpenInventory() != null) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            if (inventory.getHolder() instanceof MenuHolder
                    && ((MenuHolder) inventory.getHolder()).getMenu().equals(this)) {
                apply(inventory, player);
                // updateInventory() removed - deprecated in Paper 26.x
            }
        }
    }

    protected void apply(Inventory inventory, Player player) {
        java.util.stream.IntStream.range(0, items.length()).forEach(i -> {
            if (items.get(i) != null) {
                inventory.setItem(i, items.get(i).getFinalIcon(player));
            } else {
                inventory.setItem(i, null);
            }
        });
    }

    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.LEFT) {
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size.getSize() && items.get(slot) != null) {
                Player player = (Player) event.getWhoClicked();
                ItemClickEvent itemClickEvent = new ItemClickEvent(player);
                items.get(slot).onItemClick(itemClickEvent);
                if (itemClickEvent.willUpdate()) {
                    update(player);
                } else {
                    // updateInventory() removed - deprecated in Paper 26.x
                    if (itemClickEvent.willClose()) {
                        close(player);
                    } else if (itemClickEvent.willGoBack()) {
                        Bukkit.getScheduler()
                                .scheduleSyncDelayedTask(
                                        plugin,
                                        () -> {
                                            Player p = Bukkit.getPlayerExact(player.getName());
                                            parent.open(p);
                                        },
                                        3);
                    } else if (itemClickEvent.willGoForward()) {
                        Bukkit.getScheduler()
                                .scheduleSyncDelayedTask(
                                        plugin,
                                        () -> {
                                            Player p = Bukkit.getPlayerExact(player.getName());
                                            child.open(p);
                                        },
                                        3);
                    }
                }
            }
        }
    }

    public void destroy() {
        plugin = null;
        name = null;
        size = null;
        items = null;
        parent = null;
        child = null;
    }
}
