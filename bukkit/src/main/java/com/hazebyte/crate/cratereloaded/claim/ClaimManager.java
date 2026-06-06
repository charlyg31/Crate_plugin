package com.hazebyte.crate.cratereloaded.claim;

import com.hazebyte.crate.api.claim.Claim;
import com.hazebyte.crate.api.claim.ClaimRegistrar;
import com.hazebyte.crate.api.crate.Crate;
import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.claim.storage.ClaimStorage;
import com.hazebyte.crate.cratereloaded.claim.storage.yaml.YamlClaimStorage;
import com.hazebyte.crate.cratereloaded.component.PluginSettingComponent;
import com.hazebyte.crate.cratereloaded.menu.Size;
import com.hazebyte.crate.cratereloaded.menu.pages.ClaimPage;
import com.hazebyte.crate.cratereloaded.util.RewardFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClaimManager implements ClaimRegistrar {

    private final JavaPlugin plugin;
    private final ClaimStorage claimStorage;
    private final ClaimExecutor claimExecutor;
    private final PluginSettingComponent settings;

    public ClaimManager(JavaPlugin plugin, ClaimExecutor claimExecutor, PluginSettingComponent settings) {
        this.plugin = plugin;
        this.claimStorage = new YamlClaimStorage(plugin, claimExecutor);
        this.claimExecutor = claimExecutor;
        this.settings = settings;
    }

    public CompletableFuture<Claim> addClaim(Claim claim) {
        plugin.getLogger().finer("Claim: add " + claim.getOwner().getUniqueId());
        return claimStorage.saveClaim(claim).thenApply(v -> claim);
    }

    @Override
    public CompletableFuture<Claim> addClaim(OfflinePlayer player, List<Reward> rewards) {
        CrateClaim claim = CrateClaim.builder()
                .owner(player)
                .rewards(rewards)
                .executor(claimExecutor)
                .build();
        return addClaim(claim);
    }

    @Override
    public CompletableFuture<Claim> addClaim(OfflinePlayer player, Crate crate, int amount) {
        Reward reward = RewardFactory.createReward(crate, player, amount);
        return addClaim(player, Collections.singletonList(reward));
    }

    @Override
    public CompletableFuture<Void> removeClaim(Claim claim) {
        return claimStorage.deleteClaim(claim);
    }

    @Override
    public CompletableFuture<Optional<Claim>> getClaim(OfflinePlayer player, UUID uuid) {
        return getClaims(player).thenApply(claims -> claims.stream()
                .filter(claim -> uuid.equals(claim.getId()))
                .findFirst());
    }

    @Override
    public CompletableFuture<Collection<Claim>> getClaim(OfflinePlayer player, long timestamp) {
        return getClaims(player).thenApply(claims -> claims.stream()
                .filter(claim -> claim.getTimestamp() == timestamp)
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Collection<Claim>> getClaims(OfflinePlayer player) {
        return claimStorage.getClaims(player);
    }

    @Override
    public void openInventory(Player player) {
        plugin.getLogger().finer("Claim: open " + player.getUniqueId());
        spoofInventory(player, player);
    }

    @Override
    public void spoofInventory(OfflinePlayer player, Player viewer) {
        getClaims(player)
                .thenAccept(claims -> {
                    plugin.getLogger().finer("Claim: spoof " + player.getUniqueId());
                    boolean buttonsEnabled = settings.isMenuInteractionEnabled();

                    // Calculate max slots: 54 max, minus 9 if buttons enabled
                    int maxSlots = buttonsEnabled ? 45 : 54;
                    // Cap claims to max displayable slots, pagination handles overflow
                    int displaySlots = Math.min(claims.size(), maxSlots);
                    // Add button row back if enabled for size calculation
                    int totalSlots = buttonsEnabled ? displaySlots + 9 : displaySlots;

                    Size size = Size.fit(totalSlots);
                    ClaimPage page = new ClaimPage(claims, size, settings);
                    Bukkit.getScheduler().runTask(plugin, () -> page.open(viewer));
                })
                .exceptionally(throwable -> {
                    plugin.getLogger().severe("Failed to spoof claim inventory: " + throwable.getMessage());
                    if (throwable.getCause() != null) {
                        plugin.getLogger().severe("Caused by: " + throwable.getCause().getMessage());
                    }
                    return null;
                });
    }
}
