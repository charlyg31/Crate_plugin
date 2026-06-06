package com.hazebyte.crate.cratereloaded.component;

import com.hazebyte.crate.api.crate.Crate;
import com.hazebyte.crate.cratereloaded.model.GiveItemExecutorResult;
import java.util.Set;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface GiveCrateComponent {

    void giveCrate(
            CommandSender sender,
            OfflinePlayer offlinePlayer,
            Crate crate,
            Integer amount,
            boolean sendToClaim);

    Set<GiveItemExecutorResult> giveCrateToOnlinePlayer(
            Player player, Crate crate, int amount);

    void giveCrateToAllOnlinePlayers(CommandSender sender, Crate crate, Integer amount);
}
