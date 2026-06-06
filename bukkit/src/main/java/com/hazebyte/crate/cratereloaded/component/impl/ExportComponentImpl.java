package com.hazebyte.crate.cratereloaded.component.impl;

import static com.hazebyte.crate.cratereloaded.util.ConfigConstants.PLUGIN_EXPORT_FEATURE_NAME;

import com.google.common.base.Strings;
import com.hazebyte.crate.api.util.Messenger;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.component.ExportComponent;
import com.hazebyte.crate.cratereloaded.component.impl.FilePluginSettingComponentImpl;
import com.hazebyte.crate.cratereloaded.util.Camera;
import com.hazebyte.crate.cratereloaded.util.PlayerUtil;
import com.hazebyte.crate.cratereloaded.util.item.ItemParser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ExportComponentImpl implements ExportComponent {

    private final CorePlugin plugin;
    private final FilePluginSettingComponentImpl settings;

    @Inject
    public ExportComponentImpl(CorePlugin plugin, FilePluginSettingComponentImpl settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void exportItem(Player player) {
        ItemStack item = PlayerUtil.getItemInHand(player);
        String string = ItemParser.serialize(item);
        Messenger.tell(player, "This information has been copied over to the console.");
        Messenger.tell(player, string);
        Messenger.out(string);
    }

    @Override
    public void exportCrate(Player player, String fileName, String sort) {
        if (!settings.isPremiumUserOtherwiseLog(java.util.Optional.of(player), PLUGIN_EXPORT_FEATURE_NAME)) {
            return;
        }
        Location loc = Camera.getTargetBlockLocation(player);
        if (loc != null) {
            Block block = loc.getBlock();
            if (block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                Inventory inventory = chest.getInventory();
                List<String> items = new ArrayList<>();
                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        items.add(String.format("item:(%s)", ItemParser.serialize(item)));
                    }
                }
                if (!items.isEmpty()) {
                    boolean sorted = false;
                    if (fileName == null || fileName.isEmpty() || fileName.equalsIgnoreCase("-sort")) {
                        sort = fileName;
                        fileName = String.format(
                                "%s_%d_%d_%d",
                                loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    }
                    if (!Strings.isNullOrEmpty(sort) && sort.equalsIgnoreCase("-sort")) {
                        items.sort(Comparator.comparingInt(String::length));
                        sorted = true;
                    }
                    File folder = new File(plugin.getDataFolder(), "exports");
                    folder.mkdirs(); // Creates directory if needed (idempotent, thread-safe)
                    File export = new File(folder, fileName + "_" + System.currentTimeMillis() + ".yml");
                    try {
                        export.createNewFile();
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(export);
                        config.set(String.format("%s.reward.rewards", fileName), items);
                        config.save(export);
                        Messenger.tell(
                                player,
                                String.format(
                                        "Successfully created exports/%s.yml and saved the%scontents of the chest.",
                                        fileName, sorted ? " sorted " : " "));
                    } catch (IOException e) {
                        plugin.getLogger()
                                .log(
                                        java.util.logging.Level.SEVERE,
                                        String.format("Failed to export chest contents to %s", fileName),
                                        e);
                        Messenger.tell(player, "Error: Failed to create export file. Check server logs.");
                    }
                } else {
                    Messenger.tell(player, "Error: Empty chest, ensure the chest contains items and try again.");
                }
            } else {
                Messenger.tell(player, "Error: Please look at a chest block and try again.");
            }
        }
    }
}
