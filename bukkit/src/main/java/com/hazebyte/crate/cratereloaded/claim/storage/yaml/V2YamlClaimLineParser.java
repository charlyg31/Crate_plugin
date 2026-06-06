package com.hazebyte.crate.cratereloaded.claim.storage.yaml;

import static com.hazebyte.crate.cratereloaded.claim.storage.yaml.YamlClaimConstants.CONFIG_PLAYER_KEY;
import static com.hazebyte.crate.cratereloaded.claim.storage.yaml.YamlClaimConstants.CONFIG_REWARDS_KEY;
import static com.hazebyte.crate.cratereloaded.claim.storage.yaml.YamlClaimConstants.CONFIG_TIMESTAMP_KEY;

import com.google.common.base.Strings;
import com.hazebyte.crate.api.claim.Claim;
import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.claim.ClaimExecutor;
import com.hazebyte.crate.cratereloaded.claim.CrateClaim;
import com.hazebyte.crate.cratereloaded.model.Config;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class V2YamlClaimLineParser implements YamlClaimLineParser {

    private final Pattern UUID_REGEX = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    private final ClaimExecutor claimExecutor;

    public V2YamlClaimLineParser(ClaimExecutor claimExecutor) {
        this.claimExecutor = claimExecutor;
    }

    @Override
    public Claim apply(OfflinePlayer offlinePlayer, Config config, String key) {
        validateInput(offlinePlayer, config, key);
        if (!UUID_REGEX.matcher(key).matches()) {
            throw new IllegalArgumentException("uuid expected for key");
        }

        String timestampString = config.getConfig().getString(String.format(CONFIG_TIMESTAMP_KEY, key));
        List<String> rewardStrings = config.getConfig().getStringList(String.format(CONFIG_REWARDS_KEY, key));
        String uuidString = config.getConfig().getString(String.format(CONFIG_PLAYER_KEY, key));

        try {
            validateConfigInput(timestampString, uuidString);
        } catch (NullPointerException e) {
            return null;
        }

        long timestamp = Long.parseLong(timestampString);
        List<Reward> rewards = rewardStrings.stream()
                .map(CorePlugin.getPlugin().getCrateRegistrar()::createReward)
                .collect(Collectors.toList());
        rewards.remove(null);

        UUID claimUUID = UUID.fromString(key);
        UUID playerUUID = UUID.fromString(uuidString);
        Player player = Bukkit.getPlayer(playerUUID);
        CrateClaim claim = CrateClaim.builder()
                .owner(player)
                .rewards(rewards)
                .timestamp(timestamp)
                .id(claimUUID)
                .executor(claimExecutor)
                .build();
        return claim;
    }

    private void validateConfigInput(String timestampString, String uuidString) {
        if (Strings.isNullOrEmpty(timestampString)) {
            throw new NullPointerException("Invalid timestamp");
        }
        if (Strings.isNullOrEmpty(uuidString)) {
            throw new NullPointerException("Invalid UUID");
        }
    }
}
