package com.hazebyte.crate.cratereloaded.listener.original;

import com.hazebyte.crate.api.crate.Crate;
import com.hazebyte.crate.api.crate.CrateType;
import com.hazebyte.crate.api.event.CratePlaceEvent;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.util.Mat;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class SupplyCrateListener implements Listener {

    public SupplyCrateListener() {}

    // Must be highest since WG can cancel the event.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        ItemStack itemInHand = event.getItemInHand();

        if (Mat.from(block.getType()) == Mat.CHEST) {
            Crate crate = CorePlugin.getPlugin().getCrateRegistrar().getCrate(itemInHand);

            if (crate != null && crate.getType() == CrateType.SUPPLY && !event.isCancelled()) {
                CratePlaceEvent newEvent = new CratePlaceEvent(crate, player, block);
                Bukkit.getPluginManager().callEvent(newEvent);

                if (newEvent.isCancelled()) {
                    return;
                }

                Map<String, Object> settings = new HashMap<String, Object>() {
                    {
                        put("shouldRemoveItem", Boolean.FALSE);
                    }
                };
                CorePlugin.getPlugin().getCrateRegistrar().open(crate, player, location, settings);
            }
        }
    }
}
