package com.hazebyte.crate.cratereloaded.component.impl;

import com.hazebyte.crate.api.CrateAPI;
import com.hazebyte.crate.api.claim.Claim;
import com.hazebyte.crate.api.crate.Crate;
import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.api.event.ClaimGiveEvent;
import com.hazebyte.crate.api.event.CrateGiveEvent;
import com.hazebyte.crate.api.util.Messenger;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.claim.ClaimExecutor;
import com.hazebyte.crate.cratereloaded.claim.ClaimManager;
import com.hazebyte.crate.cratereloaded.claim.ClaimMessageConsumer;
import com.hazebyte.crate.cratereloaded.claim.CrateClaim;
import com.hazebyte.crate.cratereloaded.component.GiveCrateComponent;
import com.hazebyte.crate.cratereloaded.component.GivePlayerItemsComponent;
import com.hazebyte.crate.cratereloaded.component.PluginSettingComponent;
import com.hazebyte.crate.cratereloaded.model.GiveItemExecutorResult;
import com.hazebyte.crate.cratereloaded.util.PlayerUtil;
import com.hazebyte.crate.cratereloaded.util.RewardFactory;
import com.hazebyte.crate.cratereloaded.util.format.CustomFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCrateComponentImpl implements GiveCrateComponent {

    private final CorePlugin plugin;
    private final GivePlayerItemsComponent givePlayerItemsComponent;
    private final ClaimExecutor claimExecutor;
    private final PluginSettingComponent settings;

    @Inject
    public GiveCrateComponentImpl(
            CorePlugin plugin,
            GivePlayerItemsComponent givePlayerItemsComponent,
            ClaimExecutor claimExecutor,
            PluginSettingComponent settings) {
        this.plugin = plugin;
        this.givePlayerItemsComponent = givePlayerItemsComponent;
        this.claimExecutor = claimExecutor;
        this.settings = settings;
    }

    @Override
    public void giveCrate(
            CommandSender sender,
            OfflinePlayer offlinePlayer,
            Crate crate,
            Integer amount,
            boolean sendToClaim) {
        Player player = offlinePlayer.getPlayer();

        CrateGiveEvent event = new CrateGiveEvent(sender, offlinePlayer, crate, amount, false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        StringBuilder status = new StringBuilder("&5STATUS: ");
        boolean isInventoryFull = offlinePlayer.isOnline() && PlayerUtil.isInventoryFull(offlinePlayer.getPlayer());
        boolean isHandlingClaims = settings.isHandlingClaims()
                && (!offlinePlayer.isOnline() || sendToClaim || isInventoryFull);
        if (isHandlingClaims) {
            Optional<CompletableFuture<Claim>> optional = this.giveCrateToOfflinePlayer(offlinePlayer, crate, amount);
            if (!optional.isPresent()) {
                return;
            }
            CompletableFuture<Claim> future = optional.get();
            future.thenAccept(claim -> {
                if (!offlinePlayer.isOnline()) {
                    String message = CrateAPI.getMessage("core.claim_offline_player");
                    Messenger.tell(sender, CustomFormat.format(message, offlinePlayer));
                } else {
                    plugin.getClaimManager()
                            .getClaims(player)
                            .thenAccept(new ClaimMessageConsumer(player))
                            .exceptionally(throwable -> {
                                plugin.getLogger()
                                        .log(
                                                Level.SEVERE,
                                                "Failed to fetch claims for claim notification",
                                                throwable);
                                return null;
                            });
                    String message = CrateAPI.getMessage("core.claim_online_player");
                    Messenger.tell(sender, CustomFormat.format(message, player));
                }
            })
            .exceptionally(throwable -> {
                plugin.getLogger()
                        .log(
                                Level.SEVERE,
                                String.format("Failed to save claim for player %s", offlinePlayer.getName()),
                                throwable);
                Messenger.tell(
                        sender,
                        String.format("&cFailed to save claim for %s. Please try again.", offlinePlayer.getName()));
                return null;
            });

            status.append("&aSENT TO CLAIM.&r");
        } else {
            Set<GiveItemExecutorResult> results = this.giveCrateToOnlinePlayer(player, crate, amount);
            if (results.contains(GiveItemExecutorResult.PUT_INTO_PLAYER_INVENTORY)
                    || results.contains(GiveItemExecutorResult.DROPPED_TO_WORLD)) {
                String message = CrateAPI.getMessage("core.player_given_crate");
                Messenger.tell(player, CustomFormat.format(message, crate, player, amount));
            } else if (results.contains(GiveItemExecutorResult.PUT_INTO_PLAYER_CLAIM)) {
                Messenger.tell(player, CrateAPI.getMessage("core.claim_inventory_full"));
            }
            status.append(
                    results.size() > 0
                            ? "&aSUCCESS.&r"
                            : "&4UNSUCCESSFUL"
                                    + (settings.isHandlingClaims()
                                            ? "(CLAIM)"
                                            : "(DROPPED TO GROUND)"));
        }

        if (!(sender.equals(player))) {
            // Tell the sender that we gave the crate
            Messenger.tell(
                    sender,
                    CustomFormat.format(
                            status.insert(0, "{p} You have given a {crate} to {player}. ")
                                    .toString(),
                            crate,
                            player,
                            offlinePlayer));
        }
    }

    private Optional<CompletableFuture<Claim>> giveCrateToOfflinePlayer(
            OfflinePlayer offlinePlayer, Crate crate, int amount) {
        ClaimManager claimManager = plugin.getClaimManager();
        Reward crateAsReward = RewardFactory.createReward(crate, offlinePlayer, amount);
        CrateClaim claim = CrateClaim.builder()
                .owner(offlinePlayer)
                .rewards(Collections.singletonList(crateAsReward))
                .executor(claimExecutor)
                .build();
        ClaimGiveEvent claimGiveEvent = new ClaimGiveEvent(claim);
        Bukkit.getPluginManager().callEvent(claimGiveEvent);

        if (claimGiveEvent.isCancelled()) {
            return Optional.empty();
        }
        return Optional.of(claimManager.addClaim(claim));
    }

    @Override
    public Set<GiveItemExecutorResult> giveCrateToOnlinePlayer(
            Player player, Crate crate, int amount) {
        ItemStack item = crate.getItem().clone();
        item.setAmount(amount);
        List<ItemStack> items = Arrays.asList(item);
        return givePlayerItemsComponent.giveItems(items, player);
    }

    @Override
    public void giveCrateToAllOnlinePlayers(CommandSender sender, Crate crate, Integer amount) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            CrateGiveEvent event = new CrateGiveEvent(sender, player, crate, amount, true);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            plugin.getCrateRegistrar().giveCrate(crate, player, amount);
        }
    }
}
