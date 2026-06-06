package com.hazebyte.crate.cratereloaded.component;

import org.bukkit.entity.Player;

public interface ExportComponent {

    void exportItem(Player player);

    void exportCrate(Player player, String fileName, String sort);
}
